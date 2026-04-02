package snvn.taskmanagementservice.dto.task;

import snvn.taskmanagementservice.domain.TaskStatus;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        String owner,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}

