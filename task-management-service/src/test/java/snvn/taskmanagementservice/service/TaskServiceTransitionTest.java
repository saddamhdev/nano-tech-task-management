package snvn.taskmanagementservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import snvn.taskmanagementservice.domain.AppUser;
import snvn.taskmanagementservice.domain.Role;
import snvn.taskmanagementservice.domain.Task;
import snvn.taskmanagementservice.domain.TaskStatus;
import snvn.taskmanagementservice.dto.task.TaskStatusUpdateRequest;
import snvn.taskmanagementservice.exception.ForbiddenOperationException;
import snvn.taskmanagementservice.exception.InvalidTaskTransitionException;
import snvn.taskmanagementservice.repository.TaskCommentRepository;
import snvn.taskmanagementservice.repository.TaskRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTransitionTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private AppUser user;
    private AppUser admin;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setId(100L);
        user.setUsername("user1");
        user.setRole(Role.ROLE_USER);

        admin = new AppUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ROLE_ADMIN);

        task = new Task();
        task.setId(55L);
        task.setOwner(user);
        task.setStatus(TaskStatus.PENDING);
        task.setTitle("Sample");
        task.setDescription("Desc");
        task.setDeleted(false);
    }

    @Test
    void userCanMovePendingToInProgress() {
        when(appUserService.getCurrentUser()).thenReturn(user);
        when(taskRepository.findById(55L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = taskService.updateStatus(55L, new TaskStatusUpdateRequest(TaskStatus.IN_PROGRESS));
        assertEquals(TaskStatus.IN_PROGRESS, response.status());
    }

    @Test
    void userCannotApproveTask() {
        task.setStatus(TaskStatus.COMPLETED);
        when(appUserService.getCurrentUser()).thenReturn(user);
        when(taskRepository.findById(55L)).thenReturn(Optional.of(task));

        assertThrows(InvalidTaskTransitionException.class,
                () -> taskService.updateStatus(55L, new TaskStatusUpdateRequest(TaskStatus.APPROVED)));
    }

    @Test
    void adminCanApproveCompletedTask() {
        task.setStatus(TaskStatus.COMPLETED);
        when(appUserService.getCurrentUser()).thenReturn(admin);
        when(taskRepository.findById(55L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = taskService.updateStatus(55L, new TaskStatusUpdateRequest(TaskStatus.APPROVED));
        assertEquals(TaskStatus.APPROVED, response.status());
    }

    @Test
    void sameStatusDoesNotPersistAgain() {
        when(appUserService.getCurrentUser()).thenReturn(user);
        when(taskRepository.findById(55L)).thenReturn(Optional.of(task));

        var response = taskService.updateStatus(55L, new TaskStatusUpdateRequest(TaskStatus.PENDING));

        assertEquals(TaskStatus.PENDING, response.status());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void nonOwnerCannotDeleteTask() {
        AppUser anotherUser = new AppUser();
        anotherUser.setId(200L);
        anotherUser.setUsername("other");
        anotherUser.setRole(Role.ROLE_USER);

        when(appUserService.getCurrentUser()).thenReturn(anotherUser);
        when(taskRepository.findById(55L)).thenReturn(Optional.of(task));

        assertThrows(ForbiddenOperationException.class, () -> taskService.softDeleteTask(55L));
    }

    @Test
    void ownerCanSoftDeleteTask() {
        when(appUserService.getCurrentUser()).thenReturn(user);
        when(taskRepository.findById(55L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.softDeleteTask(55L);

        assertTrue(task.isDeleted());
        verify(taskRepository).save(task);
    }
}

