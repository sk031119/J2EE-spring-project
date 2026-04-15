// Requirements: JobPost data access - find by status with pagination, find by employer
package com.samuellaw.quick_job_system.repository;

import com.samuellaw.quick_job_system.entity.EmployerProfile;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    Page<JobPost> findByStatus(JobStatus status, Pageable pageable);

    List<JobPost> findByEmployerProfile(EmployerProfile employerProfile);
}
