// Requirements: Worker web controller
// - Browse open jobs with pagination
// - View job detail
// - Apply for jobs
// - View own applications
// - Cancel pending applications
package com.samuellaw.quick_job_system.controller;

import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.service.JobApplicationService;
import com.samuellaw.quick_job_system.service.JobPostService;
import com.samuellaw.quick_job_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/worker")
@RequiredArgsConstructor
@Slf4j
public class WorkerController {

    private final UserService userService;
    private final JobPostService jobPostService;
    private final JobApplicationService jobApplicationService;

    @GetMapping("/jobs")
    public String browseJobs(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Authentication auth, Model model) {
        Page<JobPost> jobPage = jobPostService.findOpenJobs(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("jobs", jobPage);
        return "worker/job-list";
    }

    @GetMapping("/jobs/{id}")
    public String viewJob(@PathVariable Long id, Authentication auth, Model model) {
        JobPost job = jobPostService.findById(id);
        User worker = userService.findByEmail(auth.getName());

        // Check if worker already applied
        boolean alreadyApplied = false;
        try {
            List<JobApplication> workerApps = jobApplicationService.findByWorker(worker);
            alreadyApplied = workerApps.stream()
                    .anyMatch(app -> app.getJobPost().getId().equals(id));
        } catch (Exception e) {
            log.debug("Error checking application status: {}", e.getMessage());
        }

        model.addAttribute("job", job);
        model.addAttribute("alreadyApplied", alreadyApplied);
        return "worker/job-detail";
    }

    @PostMapping("/jobs/{id}/apply")
    public String applyForJob(@PathVariable Long id, Authentication auth,
                              RedirectAttributes redirectAttributes) {
        User worker = userService.findByEmail(auth.getName());
        JobPost job = jobPostService.findById(id);

        try {
            jobApplicationService.apply(job, worker);
            redirectAttributes.addFlashAttribute("successMessage", "Application submitted successfully");
        } catch (Exception e) {
            log.warn("Application failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/worker/jobs/" + id;
    }

    @GetMapping("/applications")
    public String myApplications(Authentication auth, Model model) {
        User worker = userService.findByEmail(auth.getName());
        List<JobApplication> applications = jobApplicationService.findByWorker(worker);
        model.addAttribute("applications", applications);
        return "worker/applications";
    }

    @PostMapping("/applications/{id}/cancel")
    public String cancelApplication(@PathVariable Long id, Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        User worker = userService.findByEmail(auth.getName());

        try {
            jobApplicationService.cancel(id, worker);
            redirectAttributes.addFlashAttribute("successMessage", "Application cancelled");
        } catch (Exception e) {
            log.warn("Cancel failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/worker/applications";
    }
}
