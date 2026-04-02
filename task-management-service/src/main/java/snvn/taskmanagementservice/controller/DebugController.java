package snvn.taskmanagementservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import snvn.taskmanagementservice.repository.AppUserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final AppUserRepository appUserRepository;

    public DebugController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/admin-exists")
    public Map<String, Object> checkAdminExists() {
        Map<String, Object> response = new HashMap<>();
        var admin = appUserRepository.findByUsername("admin");
        response.put("adminExists", admin.isPresent());
        if (admin.isPresent()) {
            response.put("username", admin.get().getUsername());
            response.put("role", admin.get().getRole().name());
            response.put("active", admin.get().isActive());
        }
        return response;
    }

    @GetMapping("/user-count")
    public Map<String, Object> getUserCount() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", appUserRepository.count());
        return response;
    }
}

