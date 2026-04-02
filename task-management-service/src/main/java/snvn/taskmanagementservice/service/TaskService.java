package snvn.taskmanagementservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snvn.taskmanagementservice.domain.AppUser;
import snvn.taskmanagementservice.domain.Role;
import snvn.taskmanagementservice.domain.Task;
import snvn.taskmanagementservice.domain.TaskComment;
import snvn.taskmanagementservice.domain.TaskStatus;
import snvn.taskmanagementservice.dto.common.PagedResponse;
import snvn.taskmanagementservice.dto.task.CommentRequest;
import snvn.taskmanagementservice.dto.task.CommentResponse;
import snvn.taskmanagementservice.dto.task.TaskCreateRequest;
import snvn.taskmanagementservice.dto.task.TaskResponse;
import snvn.taskmanagementservice.dto.task.TaskStatusUpdateRequest;
import snvn.taskmanagementservice.dto.task.TaskUpdateRequest;
import snvn.taskmanagementservice.exception.ForbiddenOperationException;
import snvn.taskmanagementservice.exception.InvalidTaskTransitionException;
import snvn.taskmanagementservice.exception.ResourceNotFoundException;
import snvn.taskmanagementservice.repository.TaskCommentRepository;
import snvn.taskmanagementservice.repository.TaskRepository;

import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final AppUserService appUserService;

    public TaskService(TaskRepository taskRepository,
                       TaskCommentRepository taskCommentRepository,
                       AppUserService appUserService) {
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.appUserService = appUserService;
    }

    public TaskResponse createTask(TaskCreateRequest request) {
        AppUser currentUser = appUserService.getCurrentUser();

        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description().trim());
        task.setOwner(currentUser);
        task.setStatus(TaskStatus.PENDING);

        return toResponse(taskRepository.save(task));
    }

    public TaskResponse getTask(Long taskId) {
        AppUser currentUser = appUserService.getCurrentUser();
        Task task = getActiveTask(taskId);
        enforceTaskVisibility(task, currentUser);
        return toResponse(task);
    }

    public PagedResponse<TaskResponse> getTasks(int page, int size, TaskStatus status) {
        AppUser currentUser = appUserService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<Task> taskPage;
        if (isAdmin(currentUser)) {
            taskPage = status == null
                    ? taskRepository.findByDeletedFalse(pageable)
                    : taskRepository.findByDeletedFalseAndStatus(status, pageable);
        } else {
            taskPage = status == null
                    ? taskRepository.findByDeletedFalseAndOwner(currentUser, pageable)
                    : taskRepository.findByDeletedFalseAndOwnerAndStatus(currentUser, status, pageable);
        }

        return new PagedResponse<>(
                taskPage.getContent().stream().map(this::toResponse).toList(),
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages()
        );
    }

    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request) {
        AppUser currentUser = appUserService.getCurrentUser();
        Task task = getActiveTask(taskId);

        if (!isAdmin(currentUser) && !task.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only update your own tasks");
        }

        if (task.getStatus() == TaskStatus.APPROVED || task.getStatus() == TaskStatus.REJECTED) {
            throw new InvalidTaskTransitionException("Approved or rejected tasks cannot be modified");
        }

        task.setTitle(request.title().trim());
        task.setDescription(request.description().trim());
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse updateStatus(Long taskId, TaskStatusUpdateRequest request) {
        AppUser currentUser = appUserService.getCurrentUser();
        Task task = getActiveTask(taskId);

        TaskStatus from = task.getStatus();
        TaskStatus to = request.status();

        if (from == to) {
            return toResponse(task);
        }

        validateTransition(task, currentUser, from, to);
        task.setStatus(to);
        return toResponse(taskRepository.save(task));
    }

    public void softDeleteTask(Long taskId) {
        AppUser currentUser = appUserService.getCurrentUser();
        Task task = getActiveTask(taskId);

        if (!isAdmin(currentUser) && !task.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only delete your own tasks");
        }

        task.setDeleted(true);
        taskRepository.save(task);
    }

    public CommentResponse addComment(Long taskId, CommentRequest request) {
        AppUser currentUser = appUserService.getCurrentUser();
        Task task = getActiveTask(taskId);
        enforceTaskVisibility(task, currentUser);

        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setAuthor(currentUser);
        comment.setContent(request.content().trim());
        TaskComment saved = taskCommentRepository.save(comment);

        return new CommentResponse(saved.getId(), saved.getAuthor().getUsername(), saved.getContent(), saved.getCreatedAt());
    }

    public List<CommentResponse> getComments(Long taskId) {
        AppUser currentUser = appUserService.getCurrentUser();
        Task task = getActiveTask(taskId);
        enforceTaskVisibility(task, currentUser);

        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(c -> new CommentResponse(c.getId(), c.getAuthor().getUsername(), c.getContent(), c.getCreatedAt()))
                .toList();
    }

    private void validateTransition(Task task, AppUser currentUser, TaskStatus from, TaskStatus to) {
        boolean admin = isAdmin(currentUser);
        boolean owner = task.getOwner().getId().equals(currentUser.getId());

        if (admin) {
            if (from == TaskStatus.COMPLETED && (to == TaskStatus.APPROVED || to == TaskStatus.REJECTED)) {
                return;
            }
            throw new InvalidTaskTransitionException("ADMIN can only approve/reject completed tasks");
        }

        if (!owner) {
            throw new ForbiddenOperationException("You can only change status on your own tasks");
        }

        boolean validUserTransition =
                (from == TaskStatus.PENDING && to == TaskStatus.IN_PROGRESS) ||
                (from == TaskStatus.IN_PROGRESS && to == TaskStatus.COMPLETED);

        if (!validUserTransition) {
            throw new InvalidTaskTransitionException("Invalid transition for USER: " + from + " -> " + to);
        }
    }

    private Task getActiveTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        if (task.isDeleted()) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return task;
    }

    private void enforceTaskVisibility(Task task, AppUser currentUser) {
        if (!isAdmin(currentUser) && !task.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You cannot access this task");
        }
    }

    private boolean isAdmin(AppUser user) {
        return user.getRole() == Role.ROLE_ADMIN;
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getOwner().getUsername(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getCreatedBy(),
                task.getUpdatedBy()
        );
    }
}

