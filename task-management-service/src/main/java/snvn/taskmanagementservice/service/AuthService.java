package snvn.taskmanagementservice.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import snvn.taskmanagementservice.domain.AppUser;
import snvn.taskmanagementservice.domain.Role;
import snvn.taskmanagementservice.dto.auth.AuthResponse;
import snvn.taskmanagementservice.dto.auth.LoginRequest;
import snvn.taskmanagementservice.dto.auth.RegisterRequest;
import snvn.taskmanagementservice.exception.ForbiddenOperationException;
import snvn.taskmanagementservice.repository.AppUserRepository;
import snvn.taskmanagementservice.security.AppUserDetailsService;
import snvn.taskmanagementservice.security.JwtService;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       AppUserDetailsService userDetailsService,
                       JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(request.username().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_USER);
        user.setActive(true);
        appUserRepository.save(user);

        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getUsername()));
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isActive()) {
            throw new ForbiddenOperationException("User is inactive");
        }


        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getUsername()));
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}

