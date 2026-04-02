package snvn.taskmanagementservice.dto.task;

import jakarta.validation.constraints.NotNull;
import snvn.taskmanagementservice.domain.TaskStatus;

public record TaskStatusUpdateRequest(@NotNull TaskStatus status) {
}

