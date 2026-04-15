// Requirements: Employer web controller
// - Dashboard with own job listings
// - Job CRUD forms (create, edit, delete, close)
// - View applicants for a job and approve/reject
package com.samuellaw.quick_job_system.controller;

import com.samuellaw.quick_job_system.dto.JobPostDto;
import com.samuellaw.quick_job_system.entity.EmployerProfile;
import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.enums.JobStatus;
import com.samuellaw.quick_job_system.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employer")
@RequiredArgsConstructor
@Slf4j
public class EmployerController {

    private final UserService userService;
    private final EmployerProfileService employerProfileService;
    private final JobPostService jobPostService;
    private final JobApplicationService jobApplicationService;

    private EmployerProfile getEmployerProfile(Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        return employerProfileService.getByUser(user);
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        EmployerProfile profile = getEmployerProfile(auth);
        List<JobPost> jobs = jobPostService.findByEmployer(profile);
        model.addAttribute("jobs", jobs);
        model.addAttribute("profile", profile);
        return "employer/dashboard";
    }

    @GetMapping("/jobs/new")
    public String newJobForm(Model model) {
        model.addAttribute("jobPostDto", new JobPostDto());
        return "employer/job-form";
    }

    @PostMapping("/jobs/new")
    public String createJob(@Valid @ModelAttribute("jobPostDto") JobPostDto dto,
                            BindingResult result, Authentication auth,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "employer/job-form";
        }

        EmployerProfile profile = getEmployerProfile(auth);
        jobPostService.create(dto, profile);
        redirectAttributes.addFlashAttribute("successMessage", "Job post created successfully");
        return "redirect:/employer/dashboard";
    }

    @GetMapping("/jobs/{id}/edit")
    public String editJobForm(@PathVariable Long id, Authentication auth, Model model) {
        EmployerProfile profile = getEmployerProfile(auth);
        JobPost job = jobPostService.findById(id);

        if (!job.getEmployerProfile().getId().equals(profile.getId())) {
            return "redirect:/employer/dashboard";
        }

        JobPostDto dto = JobPostDto.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .payRate(job.getPayRate())
                .startTime(job.getStartTime())
                .endTime(job.getEndTime())
                .requiredSkills(job.getRequiredSkills())
                .workersNeeded(job.getWorkersNeeded())
                .build();

        model.addAttribute("jobPostDto", dto);
        return "employer/job-form";
    }

    @PostMapping("/jobs/{id}/edit")
    public String updateJob(@PathVariable Long id,
                            @Valid @ModelAttribute("jobPostDto") JobPostDto dto,
                            BindingResult result, Authentication auth,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            dto.setId(id);
            return "employer/job-form";
        }

        EmployerProfile profile = getEmployerProfile(auth);
        jobPostService.update(id, dto, profile);
        redirectAttributes.addFlashAttribute("successMessage", "Job post updated successfully");
        return "redirect:/employer/dashboard";
    }

    @PostMapping("/jobs/{id}/delete")
    public String deleteJob(@PathVariable Long id, Authentication auth,
                            RedirectAttributes redirectAttributes) {
        EmployerProfile profile = getEmployerProfile(auth);
        jobPostService.delete(id, profile);
        redirectAttributes.addFlashAttribute("successMessage", "Job post deleted successfully");
        return "redirect:/employer/dashboard";
    }

    @PostMapping("/jobs/{id}/close")
    public String closeJob(@PathVariable Long id, Authentication auth,
                           RedirectAttributes redirectAttributes) {
        EmployerProfile profile = getEmployerProfile(auth);
        JobPost job = jobPostService.findById(id);

        if (job.getEmployerProfile().getId().equals(profile.getId())) {
            jobPostService.updateStatus(id, JobStatus.CLOSED);
            redirectAttributes.addFlashAttribute("successMessage", "Job post closed");
        }
        return "redirect:/employer/dashboard";
    }

    @GetMapping("/jobs/{id}")
    public String viewJob(@PathVariable Long id, Authentication auth, Model model) {
        EmployerProfile profile = getEmployerProfile(auth);
        JobPost job = jobPostService.findById(id);

        if (!job.getEmployerProfile().getId().equals(profile.getId())) {
            return "redirect:/employer/dashboard";
        }

        List<JobApplication> applications = jobApplicationService.findByJobPost(job);
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        return "employer/job-detail";
    }

    @PostMapping("/applications/{id}/approve")
    public String approveApplication(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        JobApplication app = jobApplicationService.approve(id);
        redirectAttributes.addFlashAttribute("successMessage", "Application approved");
        return "redirect:/employer/jobs/" + app.getJobPost().getId();
    }

    @PostMapping("/applications/{id}/reject")
    public String rejectApplication(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        JobApplication app = jobApplicationService.reject(id);
        redirectAttributes.addFlashAttribute("successMessage", "Application rejected");
        return "redirect:/employer/jobs/" + app.getJobPost().getId();
    }
}
