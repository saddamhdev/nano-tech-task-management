package snvn.taskmanagementservice.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskCreateRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 1000) String description
) {
}

