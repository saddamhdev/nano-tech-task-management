package snvn.taskmanagementservice.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import snvn.taskmanagementservice.domain.Role;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 100) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull Role role,
        Boolean active
) {
}

