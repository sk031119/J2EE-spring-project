// Requirements: REST API for job applications
// Endpoints: POST /api/applications, PUT /api/applications/{id}/approve,
//   PUT /api/applications/{id}/reject, PUT /api/applications/{id}/cancel
package com.samuellaw.quick_job_system.controller.api;

import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.service.JobApplicationService;
import com.samuellaw.quick_job_system.service.JobPostService;
import com.samuellaw.quick_job_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class JobApplicationApiController {

    private final JobApplicationService jobApplicationService;
    private final JobPostService jobPostService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<JobApplication> apply(@RequestParam Long jobPostId,
                                                 Authentication auth) {
        log.debug("API: POST /api/applications for job {}", jobPostId);
        User worker = userService.findByEmail(auth.getName());
        JobPost jobPost = jobPostService.findById(jobPostId);
        JobApplication application = jobApplicationService.apply(jobPost, worker);
        return ResponseEntity.status(HttpStatus.CREATED).body(application);
    }

    @GetMapping
    public ResponseEntity<List<JobApplication>> getMyApplications(Authentication auth) {
        log.debug("API: GET /api/applications");
        User worker = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(jobApplicationService.findByWorker(worker));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<JobApplication> approve(@PathVariable Long id) {
        log.debug("API: PUT /api/applications/{}/approve", id);
        return ResponseEntity.ok(jobApplicationService.approve(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<JobApplication> reject(@PathVariable Long id) {
        log.debug("API: PUT /api/applications/{}/reject", id);
        return ResponseEntity.ok(jobApplicationService.reject(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancel(@PathVariable Long id,
                                                       Authentication auth) {
        log.debug("API: PUT /api/applications/{}/cancel", id);
        User worker = userService.findByEmail(auth.getName());
        jobApplicationService.cancel(id, worker);
        return ResponseEntity.ok(Map.of("message", "Application cancelled"));
    }
}
