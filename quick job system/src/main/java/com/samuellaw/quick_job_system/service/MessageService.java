// Requirements: Message business logic - send message, get messages by application
package com.samuellaw.quick_job_system.service;

import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.Message;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;

    @Transactional
    public Message sendMessage(JobApplication application, User sender, String content) {
        log.debug("User {} sending message on application {}", sender.getEmail(), application.getId());

        Message message = Message.builder()
                .jobApplication(application)
                .sender(sender)
                .content(content)
                .build();

        message = messageRepository.save(message);
        log.info("Message sent with id: {} on application: {}", message.getId(), application.getId());
        return message;
    }

    public List<Message> getMessagesByApplication(JobApplication application) {
        return messageRepository.findByJobApplicationOrderByCreatedAtAsc(application);
    }
}
