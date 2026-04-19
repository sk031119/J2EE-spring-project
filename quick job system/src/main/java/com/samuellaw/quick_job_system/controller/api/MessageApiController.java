// Requirements: REST API for messages within an application
// Endpoints: GET /api/applications/{id}/messages, POST /api/applications/{id}/messages
package com.samuellaw.quick_job_system.controller.api;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageApiController {

    private final MessageService messageService;
    private final JobApplicationService jobApplicationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long applicationId) {
        log.debug("API: GET /api/applications/{}/messages", applicationId);
        JobApplication application = jobApplicationService.findByIdForMessaging(applicationId);
        return ResponseEntity.ok(messageService.getMessagesByApplication(application));
    }

    @PostMapping
    public ResponseEntity<Message> sendMessage(@PathVariable Long applicationId,
                                                @Valid @RequestBody MessageDto dto,
                                                Authentication auth) {
        log.debug("API: POST /api/applications/{}/messages", applicationId);
        User sender = userService.findByEmail(auth.getName());
        JobApplication application = jobApplicationService.findByIdForMessaging(applicationId);
        Message message = messageService.sendMessage(application, sender, dto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}
