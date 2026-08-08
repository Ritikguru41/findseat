package com.findseat.controller;

import com.findseat.dto.StatsResponse;
import com.findseat.dto.UserResponse;
import com.findseat.security.RequireAuth;
import com.findseat.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    @RequireAuth(admin = true)
    public StatsResponse getStats() {
        return adminService.getStats();
    }

    @GetMapping("/users")
    @RequireAuth(admin = true)
    public List<UserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @DeleteMapping("/users/{id}")
    @RequireAuth(admin = true)
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id,
                                                          @RequestAttribute("userId") Long currentUserId) {
        adminService.deleteUser(id, currentUserId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
