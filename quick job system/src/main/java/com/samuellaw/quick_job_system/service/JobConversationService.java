package com.samuellaw.quick_job_system.service;

import com.samuellaw.quick_job_system.entity.*;
import com.samuellaw.quick_job_system.enums.JobStatus;
import com.samuellaw.quick_job_system.exception.InvalidStatusException;
import com.samuellaw.quick_job_system.exception.ResourceNotFoundException;
import com.samuellaw.quick_job_system.repository.JobConversationMessageRepository;
import com.samuellaw.quick_job_system.repository.JobConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobConversationService {

    private final JobConversationRepository conversationRepository;
    private final JobConversationMessageRepository messageRepository;
    private final JobPostService jobPostService;

    public Optional<JobConversation> findExistingWorkerConversation(Long jobPostId, User worker) {
        return conversationRepository.findByJobPost_IdAndWorker_Id(jobPostId, worker.getId());
    }

    public List<JobConversation> listForWorker(User worker) {
        return conversationRepository.findByWorkerWithJobAndEmployer(worker);
    }

    public List<JobConversation> listForEmployerProfile(EmployerProfile profile) {
        return conversationRepository.findByJobPostEmployerProfileWithJobAndWorker(profile);
    }

    public List<JobConversation> listForEmployerJob(Long jobPostId, EmployerProfile profile) {
        JobPost job = jobPostService.findById(jobPostId);
        if (!job.getEmployerProfile().getId().equals(profile.getId())) {
            throw new SecurityException("You do not own this job post");
        }
        return conversationRepository.findByJobPostIdWithWorker(jobPostId);
    }

    @Transactional
    public JobConversation findOrCreateForWorker(Long jobPostId, User worker) {
        JobPost job = jobPostService.findById(jobPostId);
        return conversationRepository.findByJobPost_IdAndWorker_Id(jobPostId, worker.getId())
                .map(c -> conversationRepository.findByIdWithParticipants(c.getId()).orElse(c))
                .orElseGet(() -> {
                    if (job.getStatus() != JobStatus.OPEN) {
                        throw new InvalidStatusException("This job is not open for new conversations");
                    }
                    JobConversation c = JobConversation.builder()
                            .jobPost(job)
                            .worker(worker)
                            .build();
                    c = conversationRepository.save(c);
                    return conversationRepository.findByIdWithParticipants(c.getId()).orElse(c);
                });
    }

    public JobConversation getConversationForParticipant(Long conversationId, User user) {
        JobConversation c = conversationRepository.findByIdWithParticipants(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        boolean worker = c.getWorker().getId().equals(user.getId());
        boolean employer = c.getJobPost().getEmployerProfile().getUser().getId().equals(user.getId());
        if (!worker && !employer) {
            throw new SecurityException("You cannot access this conversation");
        }
        return c;
    }

    public List<JobConversationMessage> listMessages(Long conversationId, User user) {
        getConversationForParticipant(conversationId, user);
        return messageRepository.findByConversationIdWithSenderOrdered(conversationId);
    }

    @Transactional
    public JobConversationMessage sendMessage(Long conversationId, User sender, String content) {
        JobConversation conversation = getConversationForParticipant(conversationId, sender);
        JobConversationMessage msg = JobConversationMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content.trim())
                .build();
        msg = messageRepository.save(msg);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        log.info("Job conversation {} message from {}", conversationId, sender.getEmail());
        return msg;
    }
}
