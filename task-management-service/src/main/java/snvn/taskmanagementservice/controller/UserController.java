package snvn.taskmanagementservice.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import snvn.taskmanagementservice.dto.user.CreateUserRequest;
import snvn.taskmanagementservice.dto.user.UpdateUserStatusRequest;
import snvn.taskmanagementservice.dto.user.UserResponse;
import snvn.taskmanagementservice.service.AppUserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        return appUserService.getMyProfile();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> allUsers() {
        return appUserService.getAllUsers();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return appUserService.createUser(request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateStatus(@PathVariable("id") Long id,
                                     @Valid @RequestBody UpdateUserStatusRequest request) {
        return appUserService.updateUserStatus(id, request);
    }
}

