// Requirements: JobApplication data access - find by worker, find by job post,
//   check for duplicate applications
package com.samuellaw.quick_job_system.repository;

import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByWorker(User worker);

    List<JobApplication> findByJobPost(JobPost jobPost);

    boolean existsByJobPostAndWorker(JobPost jobPost, User worker);
}
