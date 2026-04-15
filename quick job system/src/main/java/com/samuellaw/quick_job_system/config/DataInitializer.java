// Requirements: Seed hardcoded admin user on startup
// Admin credentials: username=admin@quickjob.com, password=admin
package com.samuellaw.quick_job_system.config;

import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.enums.Role;
import com.samuellaw.quick_job_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create admin user if not exists
        String adminEmail = "admin@quickjob.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .fullName("System Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();

            userRepository.save(admin);
            log.info("Admin user created: email={}, password=admin", adminEmail);
        } else {
            log.info("Admin user already exists: {}", adminEmail);
        }
    }
}
