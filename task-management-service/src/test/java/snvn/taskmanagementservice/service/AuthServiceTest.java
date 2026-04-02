package snvn.taskmanagementservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import snvn.taskmanagementservice.domain.AppUser;
import snvn.taskmanagementservice.domain.Role;
import snvn.taskmanagementservice.dto.auth.LoginRequest;
import snvn.taskmanagementservice.dto.auth.RegisterRequest;
import snvn.taskmanagementservice.exception.ForbiddenOperationException;
import snvn.taskmanagementservice.repository.AppUserRepository;
import snvn.taskmanagementservice.security.AppUserDetailsService;
import snvn.taskmanagementservice.security.JwtService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AppUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerRejectsDuplicateUsername() {
        when(appUserRepository.existsByUsername("bob")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(new RegisterRequest("bob", "password123")));

        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void registerCreatesDefaultUserAndReturnsToken() {
        var userDetails = User.withUsername("bob").password("x").roles("USER").build();

        when(appUserRepository.existsByUsername("  bob  ")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDetailsService.loadUserByUsername("bob")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        var response = authService.register(new RegisterRequest("  bob  ", "password123"));

        assertEquals("jwt-token", response.token());
        assertEquals("bob", response.username());
        assertEquals("ROLE_USER", response.role());
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void loginRejectsInactiveUser() {
        AppUser inactiveUser = new AppUser();
        inactiveUser.setUsername("bob");
        inactiveUser.setActive(false);

        when(appUserRepository.findByUsername("bob")).thenReturn(Optional.of(inactiveUser));

        ForbiddenOperationException ex = assertThrows(ForbiddenOperationException.class,
                () -> authService.login(new LoginRequest("bob", "password123")));

        assertEquals("User is inactive", ex.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void loginReturnsTokenForActiveUser() {
        AppUser activeUser = new AppUser();
        activeUser.setUsername("admin");
        activeUser.setRole(Role.ROLE_ADMIN);
        activeUser.setActive(true);

        var userDetails = User.withUsername("admin").password("x").roles("ADMIN").build();

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(activeUser));
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-admin");

        var response = authService.login(new LoginRequest("admin", "password123"));

        assertEquals("jwt-admin", response.token());
        assertEquals("admin", response.username());
        assertEquals("ROLE_ADMIN", response.role());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}


