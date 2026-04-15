// Requirements: User business logic - registration, find all, find by id, toggle enabled
package com.samuellaw.quick_job_system.service;

import com.samuellaw.quick_job_system.dto.RegistrationDto;
import com.samuellaw.quick_job_system.entity.EmployerProfile;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.enums.Role;
import com.samuellaw.quick_job_system.exception.ResourceNotFoundException;
import com.samuellaw.quick_job_system.repository.EmployerProfileRepository;
import com.samuellaw.quick_job_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegistrationDto dto) {
        log.debug("Registering user with email: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .enabled(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered: {} with role: {}", user.getEmail(), user.getRole());

        // Create employer profile if role is EMPLOYER
        if (dto.getRole() == Role.EMPLOYER) {
            EmployerProfile profile = EmployerProfile.builder()
                    .user(user)
                    .companyName(dto.getCompanyName() != null ? dto.getCompanyName() : "")
                    .companyDescription(dto.getCompanyDescription() != null ? dto.getCompanyDescription() : "")
                    .contactName(dto.getContactName() != null ? dto.getContactName() : user.getFullName())
                    .verified(false)
                    .build();
            employerProfileRepository.save(profile);
            log.info("Employer profile created for user: {}", user.getEmail());
        }

        return user;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public void toggleEnabled(Long userId) {
        User user = findById(userId);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        log.info("User {} enabled status toggled to: {}", user.getEmail(), user.isEnabled());
    }
}
