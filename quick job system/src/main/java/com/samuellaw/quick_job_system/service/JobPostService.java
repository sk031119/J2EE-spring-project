// Requirements: JobPost business logic - CRUD, status transitions, paginated browsing
package com.samuellaw.quick_job_system.service;

import com.samuellaw.quick_job_system.dto.JobPostDto;
import com.samuellaw.quick_job_system.dto.JobSearchParams;
import com.samuellaw.quick_job_system.entity.EmployerProfile;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.enums.JobStatus;
import com.samuellaw.quick_job_system.exception.ResourceNotFoundException;
import com.samuellaw.quick_job_system.repository.JobPostRepository;
import com.samuellaw.quick_job_system.repository.JobPostSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobPostService {

    private final JobPostRepository jobPostRepository;

    @Transactional
    public JobPost create(JobPostDto dto, EmployerProfile employerProfile) {
        log.debug("Creating job post: {} for employer: {}", dto.getTitle(), employerProfile.getCompanyName());

        JobPost jobPost = JobPost.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .payRate(dto.getPayRate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .requiredSkills(dto.getRequiredSkills())
                .jobType(normalizeJobType(dto.getJobType()))
                .workersNeeded(dto.getWorkersNeeded())
                .status(JobStatus.OPEN)
                .employerProfile(employerProfile)
                .build();

        jobPost = jobPostRepository.save(jobPost);
        log.info("Job post created with id: {}", jobPost.getId());
        return jobPost;
    }

    @Transactional
    public JobPost update(Long id, JobPostDto dto, EmployerProfile employerProfile) {
        JobPost jobPost = findById(id);

        // Verify ownership
        if (!jobPost.getEmployerProfile().getId().equals(employerProfile.getId())) {
            throw new SecurityException("You do not own this job post");
        }

        jobPost.setTitle(dto.getTitle());
        jobPost.setDescription(dto.getDescription());
        jobPost.setLocation(dto.getLocation());
        jobPost.setPayRate(dto.getPayRate());
        jobPost.setStartTime(dto.getStartTime());
        jobPost.setEndTime(dto.getEndTime());
        jobPost.setRequiredSkills(dto.getRequiredSkills());
        jobPost.setJobType(normalizeJobType(dto.getJobType()));
        jobPost.setWorkersNeeded(dto.getWorkersNeeded());

        log.info("Job post updated with id: {}", id);
        return jobPostRepository.save(jobPost);
    }

    @Transactional
    public void delete(Long id, EmployerProfile employerProfile) {
        JobPost jobPost = findById(id);

        if (!jobPost.getEmployerProfile().getId().equals(employerProfile.getId())) {
            throw new SecurityException("You do not own this job post");
        }

        jobPostRepository.delete(jobPost);
        log.info("Job post deleted with id: {}", id);
    }

    @Transactional
    public void updateStatus(Long id, JobStatus status) {
        JobPost jobPost = findById(id);
        jobPost.setStatus(status);
        jobPostRepository.save(jobPost);
        log.info("Job post {} status updated to: {}", id, status);
    }

    public JobPost findById(Long id) {
        return jobPostRepository.findWithEmployerProfileById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found with id: " + id));
    }

    public Page<JobPost> findOpenJobs(Pageable pageable) {
        return searchOpenJobs(new JobSearchParams(), pageable);
    }

    public Page<JobPost> searchOpenJobs(JobSearchParams params, Pageable pageable) {
        return jobPostRepository.findAll(JobPostSpecifications.openJobsMatching(params), pageable);
    }

    private static String normalizeJobType(String jobType) {
        if (jobType == null) {
            return null;
        }
        String t = jobType.trim();
        return t.isEmpty() ? null : t.toLowerCase();
    }

    public List<JobPost> findByEmployer(EmployerProfile employerProfile) {
        return jobPostRepository.findByEmployerProfile(employerProfile);
    }

    public List<JobPost> findAll() {
        return jobPostRepository.findAllWithEmployerProfile();
    }

    // Admin: delete any job post
    @Transactional
    public void adminDelete(Long id) {
        JobPost jobPost = findById(id);
        jobPostRepository.delete(jobPost);
        log.info("Admin deleted job post with id: {}", id);
    }
}
