// Requirements: Worker web controller
// - Browse open jobs with search filters and pagination
// - View job detail, apply, job-level chat with employer
// - Inbox of job conversations, applications, cancel
package com.samuellaw.quick_job_system.controller;

import com.samuellaw.quick_job_system.dto.JobSearchParams;
import com.samuellaw.quick_job_system.dto.MessageDto;
import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.JobConversation;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.exception.InvalidStatusException;
import com.samuellaw.quick_job_system.service.JobApplicationService;
import com.samuellaw.quick_job_system.service.JobConversationService;
import com.samuellaw.quick_job_system.service.JobPostService;
import com.samuellaw.quick_job_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/worker")
@RequiredArgsConstructor
@Slf4j
public class WorkerController {

    @InitBinder("search")
    public void initSearchBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.isBlank()) {
                    setValue(null);
                } else {
                    setValue(new BigDecimal(text.trim()));
                }
            }
        });
    }

    private final UserService userService;
    private final JobPostService jobPostService;
    private final JobApplicationService jobApplicationService;
    private final JobConversationService jobConversationService;

    @GetMapping("/jobs")
    public String browseJobs(@ModelAttribute("search") JobSearchParams search,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Authentication auth, Model model) {
        Page<JobPost> jobPage = jobPostService.searchOpenJobs(search,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("jobs", jobPage);
        return "worker/job-list";
    }

    @GetMapping("/jobs/{id}")
    public String viewJob(@PathVariable Long id, Authentication auth, Model model) {
        JobPost job = jobPostService.findById(id);
        User worker = userService.findByEmail(auth.getName());

        boolean alreadyApplied = false;
        try {
            List<JobApplication> workerApps = jobApplicationService.findByWorker(worker);
            alreadyApplied = workerApps.stream()
                    .anyMatch(app -> app.getJobPost().getId().equals(id));
        } catch (Exception e) {
            log.debug("Error checking application status: {}", e.getMessage());
        }

        boolean hasJobChat = jobConversationService.findExistingWorkerConversation(id, worker).isPresent();

        model.addAttribute("job", job);
        model.addAttribute("alreadyApplied", alreadyApplied);
        model.addAttribute("hasJobChat", hasJobChat);
        return "worker/job-detail";
    }

    @GetMapping("/jobs/{id}/chat")
    public String workerJobChat(@PathVariable Long id, Authentication auth, Model model,
                                 RedirectAttributes redirectAttributes) {
        User worker = userService.findByEmail(auth.getName());
        try {
            JobConversation conv = jobConversationService.findOrCreateForWorker(id, worker);
            populateJobInquiryChat(model, conv, worker);
            model.addAttribute("chatKind", "worker");
            model.addAttribute("messageDto", new MessageDto());
            return "messages/job-inquiry";
        } catch (InvalidStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/worker/jobs/" + id;
        }
    }

    @PostMapping("/jobs/{id}/chat")
    public String postWorkerJobChat(@PathVariable Long id,
                                    @Valid @ModelAttribute("messageDto") MessageDto dto,
                                    BindingResult result, Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Message cannot be empty");
            return "redirect:/worker/jobs/" + id + "/chat";
        }
        User worker = userService.findByEmail(auth.getName());
        try {
            JobConversation conv = jobConversationService.findOrCreateForWorker(id, worker);
            jobConversationService.sendMessage(conv.getId(), worker, dto.getContent());
            redirectAttributes.addFlashAttribute("successMessage", "Message sent");
        } catch (InvalidStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/worker/jobs/" + id;
        }
        return "redirect:/worker/jobs/" + id + "/chat";
    }

    @GetMapping("/conversations")
    public String workerConversations(Authentication auth, Model model) {
        User worker = userService.findByEmail(auth.getName());
        model.addAttribute("conversations", jobConversationService.listForWorker(worker));
        return "worker/conversations";
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

    private void populateJobInquiryChat(Model model, JobConversation conv, User currentUser) {
        model.addAttribute("conversation", conv);
        model.addAttribute("job", conv.getJobPost());
        model.addAttribute("messages", jobConversationService.listMessages(conv.getId(), currentUser));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherPartyLabel", conv.getJobPost().getEmployerProfile().getCompanyName());
    }
}
