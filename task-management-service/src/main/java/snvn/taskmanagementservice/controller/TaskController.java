package snvn.taskmanagementservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import snvn.taskmanagementservice.domain.TaskStatus;
import snvn.taskmanagementservice.dto.common.PagedResponse;
import snvn.taskmanagementservice.dto.task.CommentRequest;
import snvn.taskmanagementservice.dto.task.CommentResponse;
import snvn.taskmanagementservice.dto.task.TaskCreateRequest;
import snvn.taskmanagementservice.dto.task.TaskResponse;
import snvn.taskmanagementservice.dto.task.TaskStatusUpdateRequest;
import snvn.taskmanagementservice.dto.task.TaskUpdateRequest;
import snvn.taskmanagementservice.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody TaskCreateRequest request) {
        return taskService.createTask(request);
    }

    @GetMapping
    public PagedResponse<TaskResponse> getTasks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "status", required = false) TaskStatus status) {
        return taskService.getTasks(page, size, status);
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable("id") Long id) {
        return taskService.getTask(id);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable("id") Long id, @Valid @RequestBody TaskUpdateRequest request) {
        return taskService.updateTask(id, request);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(@PathVariable("id") Long id, @Valid @RequestBody TaskStatusUpdateRequest request) {
        return taskService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.softDeleteTask(id);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@PathVariable("id") Long id, @Valid @RequestBody CommentRequest request) {
        return taskService.addComment(id, request);
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable("id") Long id) {
        return taskService.getComments(id);
    }
}

