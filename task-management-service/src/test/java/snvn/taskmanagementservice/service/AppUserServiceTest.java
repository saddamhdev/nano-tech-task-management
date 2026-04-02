package snvn.taskmanagementservice.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import snvn.taskmanagementservice.domain.AppUser;
import snvn.taskmanagementservice.domain.Role;
import snvn.taskmanagementservice.dto.user.CreateUserRequest;
import snvn.taskmanagementservice.exception.ResourceNotFoundException;
import snvn.taskmanagementservice.repository.AppUserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService appUserService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUserAssignsSelectedRoleAndDefaultsActiveWhenMissing() {
        when(appUserRepository.existsByUsername("bob")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = appUserService.createUser(new CreateUserRequest("bob", "password123", Role.ROLE_ADMIN, null));

        assertEquals("bob", response.username());
        assertEquals("ROLE_ADMIN", response.role());
        assertTrue(response.active());
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        when(appUserRepository.existsByUsername("bob")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appUserService.createUser(new CreateUserRequest("bob", "password123", Role.ROLE_USER, true)));

        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void getCurrentUserFailsWhenAuthenticationMissing() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> appUserService.getCurrentUser());
        assertEquals("Authenticated user not found", ex.getMessage());
    }

    @Test
    void getCurrentUserFailsWhenPrincipalNotInRepository() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ghost", "n/a")
        );
        when(appUserRepository.findByUsername("ghost")).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> appUserService.getCurrentUser());

        assertEquals("User not found: ghost", ex.getMessage());
    }

    @Test
    void getCurrentUserReturnsRepositoryUserForAuthenticatedPrincipal() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole(Role.ROLE_USER);
        user.setActive(true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", "n/a")
        );
        when(appUserRepository.findByUsername("alice")).thenReturn(java.util.Optional.of(user));

        AppUser result = appUserService.getCurrentUser();

        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        verify(appUserRepository).findByUsername("alice");
    }
}

