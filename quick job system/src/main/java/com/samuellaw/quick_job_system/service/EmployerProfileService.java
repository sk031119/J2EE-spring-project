// Requirements: EmployerProfile business logic - get profile by user
package com.samuellaw.quick_job_system.service;

import com.samuellaw.quick_job_system.entity.EmployerProfile;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.exception.ResourceNotFoundException;
import com.samuellaw.quick_job_system.repository.EmployerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployerProfileService {

    private final EmployerProfileRepository employerProfileRepository;

    public EmployerProfile getByUser(User user) {
        return employerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user: " + user.getEmail()));
    }
}
