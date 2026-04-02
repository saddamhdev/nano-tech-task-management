package snvn.taskmanagementservice.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import snvn.taskmanagementservice.domain.AppUser;
import snvn.taskmanagementservice.domain.Role;
import snvn.taskmanagementservice.dto.user.CreateUserRequest;
import snvn.taskmanagementservice.dto.user.UpdateUserStatusRequest;
import snvn.taskmanagementservice.dto.user.UserResponse;
import snvn.taskmanagementservice.exception.ResourceNotFoundException;
import snvn.taskmanagementservice.repository.AppUserRepository;

import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(request.username().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role() == null ? Role.ROLE_USER : request.role());
        user.setActive(request.active() == null || request.active());

        return toResponse(appUserRepository.save(user));
    }

    public UserResponse getMyProfile() {
        return toResponse(getCurrentUser());
    }

    public UserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setActive(request.active());
        appUserRepository.save(user);
        return toResponse(user);
    }

    public AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }

        return appUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole().name(), user.isActive());
    }
}

