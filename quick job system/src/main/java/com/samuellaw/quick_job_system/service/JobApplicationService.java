// Requirements: JobApplication business logic - apply, approve, reject, cancel
// Rules: no duplicate applications, only OPEN jobs, only PENDING can be approved/rejected
package com.samuellaw.quick_job_system.service;

import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.enums.ApplicationStatus;
import com.samuellaw.quick_job_system.enums.JobStatus;
import com.samuellaw.quick_job_system.exception.DuplicateApplicationException;
import com.samuellaw.quick_job_system.exception.InvalidStatusException;
import com.samuellaw.quick_job_system.exception.ResourceNotFoundException;
import com.samuellaw.quick_job_system.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;

    @Transactional
    public JobApplication apply(JobPost jobPost, User worker) {
        log.debug("Worker {} applying for job {}", worker.getEmail(), jobPost.getId());

        if (jobPost.getStatus() != JobStatus.OPEN) {
            throw new InvalidStatusException("Cannot apply to a job that is not OPEN");
        }

        if (jobApplicationRepository.existsByJobPostAndWorker(jobPost, worker)) {
            throw new DuplicateApplicationException("You have already applied for this job");
        }

        JobApplication application = JobApplication.builder()
                .jobPost(jobPost)
                .worker(worker)
                .status(ApplicationStatus.PENDING)
                .build();

        application = jobApplicationRepository.save(application);
        log.info("Application created with id: {} for job: {}", application.getId(), jobPost.getId());
        return application;
    }

    @Transactional
    public JobApplication approve(Long applicationId) {
        JobApplication application = findById(applicationId);

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidStatusException("Only PENDING applications can be approved");
        }

        application.setStatus(ApplicationStatus.APPROVED);
        log.info("Application {} approved", applicationId);
        return jobApplicationRepository.save(application);
    }

    @Transactional
    public JobApplication reject(Long applicationId) {
        JobApplication application = findById(applicationId);

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidStatusException("Only PENDING applications can be rejected");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        log.info("Application {} rejected", applicationId);
        return jobApplicationRepository.save(application);
    }

    @Transactional
    public JobApplication cancel(Long applicationId, User worker) {
        JobApplication application = findById(applicationId);

        if (!application.getWorker().getId().equals(worker.getId())) {
            throw new SecurityException("You do not own this application");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidStatusException("Only PENDING applications can be cancelled");
        }

        application.setStatus(ApplicationStatus.CANCELLED);
        log.info("Application {} cancelled by worker {}", applicationId, worker.getEmail());
        return jobApplicationRepository.save(application);
    }

    public JobApplication findById(Long id) {
        return jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    public JobApplication findByIdForMessaging(Long id) {
        return jobApplicationRepository.findByIdWithParticipantDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    public List<JobApplication> findByWorker(User worker) {
        return jobApplicationRepository.findByWorkerWithJobAndEmployer(worker);
    }

    public List<JobApplication> findByJobPost(JobPost jobPost) {
        return jobApplicationRepository.findByJobPostWithWorker(jobPost);
    }

    public List<JobApplication> findAll() {
        return jobApplicationRepository.findAll();
    }
}
