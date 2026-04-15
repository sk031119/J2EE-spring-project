// Requirements: REST API for user management (admin operations)
// Endpoints: GET /api/users, GET /api/users/{id}, PUT /api/users/{id}/toggle
package com.samuellaw.quick_job_system.controller.api;

import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserApiController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.debug("API: GET /api/users");
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.debug("API: GET /api/users/{}", id);
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, String>> toggleUser(@PathVariable Long id) {
        log.debug("API: PUT /api/users/{}/toggle", id);
        userService.toggleEnabled(id);
        return ResponseEntity.ok(Map.of("message", "User status toggled", "userId", id.toString()));
    }
}
