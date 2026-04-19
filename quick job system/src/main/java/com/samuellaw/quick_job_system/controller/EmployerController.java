// Requirements: Employer web controller
// - Dashboard with own job listings
// - Job CRUD forms (create, edit, delete, close)
// - View applicants for a job and approve/reject
package com.samuellaw.quick_job_system.controller;

import com.samuellaw.quick_job_system.dto.JobPostDto;
import com.samuellaw.quick_job_system.dto.MessageDto;
import com.samuellaw.quick_job_system.entity.EmployerProfile;
import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.JobConversation;
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
    private final JobConversationService jobConversationService;

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
                .jobType(job.getJobType())
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
        List<JobConversation> inquiries = jobConversationService.listForEmployerJob(id, profile);
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("jobInquiries", inquiries);
        return "employer/job-detail";
    }

    @GetMapping("/conversations")
    public String employerConversations(Authentication auth, Model model) {
        EmployerProfile profile = getEmployerProfile(auth);
        model.addAttribute("conversations", jobConversationService.listForEmployerProfile(profile));
        return "employer/conversations";
    }

    @GetMapping("/jobs/{jobId}/chats")
    public String jobChatsList(@PathVariable Long jobId, Authentication auth, Model model) {
        EmployerProfile profile = getEmployerProfile(auth);
        JobPost job = jobPostService.findById(jobId);
        if (!job.getEmployerProfile().getId().equals(profile.getId())) {
            return "redirect:/employer/dashboard";
        }
        model.addAttribute("job", job);
        model.addAttribute("conversations", jobConversationService.listForEmployerJob(jobId, profile));
        return "employer/job-chats";
    }

    @GetMapping("/jobs/{jobId}/chats/{conversationId}")
    public String employerJobChat(@PathVariable Long jobId, @PathVariable Long conversationId,
                                  Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        EmployerProfile profile = getEmployerProfile(auth);
        JobPost job = jobPostService.findById(jobId);
        if (!job.getEmployerProfile().getId().equals(profile.getId())) {
            return "redirect:/employer/dashboard";
        }
        JobConversation conv = jobConversationService.getConversationForParticipant(conversationId, user);
        if (!conv.getJobPost().getId().equals(jobId)) {
            return "redirect:/employer/jobs/" + jobId + "/chats";
        }
        populateEmployerJobChat(model, conv, user);
        model.addAttribute("chatKind", "employer");
        model.addAttribute("messageDto", new MessageDto());
        return "messages/job-inquiry";
    }

    @PostMapping("/jobs/{jobId}/chats/{conversationId}")
    public String postEmployerJobChat(@PathVariable Long jobId, @PathVariable Long conversationId,
                                      @Valid @ModelAttribute("messageDto") MessageDto dto,
                                      BindingResult result, Authentication auth,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Message cannot be empty");
            return "redirect:/employer/jobs/" + jobId + "/chats/" + conversationId;
        }
        User user = userService.findByEmail(auth.getName());
        JobConversation conv = jobConversationService.getConversationForParticipant(conversationId, user);
        if (!conv.getJobPost().getId().equals(jobId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid conversation");
            return "redirect:/employer/dashboard";
        }
        jobConversationService.sendMessage(conversationId, user, dto.getContent());
        redirectAttributes.addFlashAttribute("successMessage", "Message sent");
        return "redirect:/employer/jobs/" + jobId + "/chats/" + conversationId;
    }

    private void populateEmployerJobChat(Model model, JobConversation conv, User currentUser) {
        model.addAttribute("conversation", conv);
        model.addAttribute("job", conv.getJobPost());
        model.addAttribute("messages", jobConversationService.listMessages(conv.getId(), currentUser));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherPartyLabel", conv.getWorker().getFullName());
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
