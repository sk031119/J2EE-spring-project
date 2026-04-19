// Requirements: JobPost data access - find by status with pagination, find by employer
package com.samuellaw.quick_job_system.repository;

import com.samuellaw.quick_job_system.entity.EmployerProfile;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long>, JpaSpecificationExecutor<JobPost> {

    Page<JobPost> findByStatus(JobStatus status, Pageable pageable);

    List<JobPost> findByEmployerProfile(EmployerProfile employerProfile);

    @Query("SELECT jp FROM JobPost jp JOIN FETCH jp.employerProfile WHERE jp.id = :id")
    Optional<JobPost> findWithEmployerProfileById(@Param("id") Long id);

    @Query("SELECT DISTINCT jp FROM JobPost jp LEFT JOIN FETCH jp.employerProfile")
    List<JobPost> findAllWithEmployerProfile();
}
