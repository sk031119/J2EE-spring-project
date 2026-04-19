// Requirements: Message web controller
// - View message thread for an application
// - Send message within an application
// Access: Only the employer who owns the job or the worker who applied
package com.samuellaw.quick_job_system.controller;

import com.samuellaw.quick_job_system.dto.MessageDto;
import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.Message;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.service.JobApplicationService;
import com.samuellaw.quick_job_system.service.MessageService;
import com.samuellaw.quick_job_system.service.UserService;
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
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;
    private final JobApplicationService jobApplicationService;
    private final UserService userService;

    @GetMapping("/application/{applicationId}")
    public String viewThread(@PathVariable Long applicationId, Authentication auth, Model model) {
        User currentUser = userService.findByEmail(auth.getName());
        JobApplication application = jobApplicationService.findByIdForMessaging(applicationId);

        // Verify access: only the worker or the employer can view
        boolean isWorker = application.getWorker().getId().equals(currentUser.getId());
        boolean isEmployer = application.getJobPost().getEmployerProfile().getUser().getId().equals(currentUser.getId());

        if (!isWorker && !isEmployer) {
            log.warn("Unauthorized message access attempt by user: {}", currentUser.getEmail());
            return "redirect:/dashboard";
        }

        List<Message> messages = messageService.getMessagesByApplication(application);
        model.addAttribute("messages", messages);
        model.addAttribute("application", application);
        model.addAttribute("messageDto", new MessageDto());
        model.addAttribute("currentUser", currentUser);
        return "messages/thread";
    }

    @PostMapping("/application/{applicationId}")
    public String sendMessage(@PathVariable Long applicationId,
                              @Valid @ModelAttribute("messageDto") MessageDto dto,
                              BindingResult result, Authentication auth,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Message content cannot be empty");
            return "redirect:/messages/application/" + applicationId;
        }

        User currentUser = userService.findByEmail(auth.getName());
        JobApplication application = jobApplicationService.findByIdForMessaging(applicationId);

        // Verify access
        boolean isWorker = application.getWorker().getId().equals(currentUser.getId());
        boolean isEmployer = application.getJobPost().getEmployerProfile().getUser().getId().equals(currentUser.getId());

        if (!isWorker && !isEmployer) {
            return "redirect:/dashboard";
        }

        messageService.sendMessage(application, currentUser, dto.getContent());
        return "redirect:/messages/application/" + applicationId;
    }
}
