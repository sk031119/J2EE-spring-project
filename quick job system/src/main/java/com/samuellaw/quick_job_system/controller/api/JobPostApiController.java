// Requirements: REST API for job posts
// Endpoints: GET /api/jobs (paginated, public), GET /api/jobs/{id} (public),
//   POST /api/jobs, PUT /api/jobs/{id}, DELETE /api/jobs/{id}
package com.samuellaw.quick_job_system.controller.api;

import com.samuellaw.quick_job_system.dto.JobPostDto;
import com.samuellaw.quick_job_system.entity.EmployerProfile;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.service.EmployerProfileService;
import com.samuellaw.quick_job_system.service.JobPostService;
import com.samuellaw.quick_job_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobPostApiController {

    private final JobPostService jobPostService;
    private final UserService userService;
    private final EmployerProfileService employerProfileService;

    @GetMapping
    public ResponseEntity<Page<JobPost>> getOpenJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("API: GET /api/jobs page={} size={}", page, size);
        return ResponseEntity.ok(jobPostService.findOpenJobs(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPost> getJobById(@PathVariable Long id) {
        log.debug("API: GET /api/jobs/{}", id);
        return ResponseEntity.ok(jobPostService.findById(id));
    }

    @PostMapping
    public ResponseEntity<JobPost> createJob(@Valid @RequestBody JobPostDto dto,
                                             Authentication auth) {
        log.debug("API: POST /api/jobs");
        User user = userService.findByEmail(auth.getName());
        EmployerProfile profile = employerProfileService.getByUser(user);
        JobPost created = jobPostService.create(dto, profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPost> updateJob(@PathVariable Long id,
                                             @Valid @RequestBody JobPostDto dto,
                                             Authentication auth) {
        log.debug("API: PUT /api/jobs/{}", id);
        User user = userService.findByEmail(auth.getName());
        EmployerProfile profile = employerProfileService.getByUser(user);
        return ResponseEntity.ok(jobPostService.update(id, dto, profile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable Long id,
                                                          Authentication auth) {
        log.debug("API: DELETE /api/jobs/{}", id);
        User user = userService.findByEmail(auth.getName());
        EmployerProfile profile = employerProfileService.getByUser(user);
        jobPostService.delete(id, profile);
        return ResponseEntity.ok(Map.of("message", "Job post deleted", "jobId", id.toString()));
    }
}
