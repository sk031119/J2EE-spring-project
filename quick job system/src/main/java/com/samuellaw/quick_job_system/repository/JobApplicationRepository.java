// Requirements: JobApplication data access - find by worker, find by job post,
//   check for duplicate applications
package com.samuellaw.quick_job_system.repository;

import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByWorker(User worker);

    @Query("SELECT ja FROM JobApplication ja JOIN FETCH ja.jobPost jp JOIN FETCH jp.employerProfile WHERE ja.worker = :worker")
    List<JobApplication> findByWorkerWithJobAndEmployer(@Param("worker") User worker);

    List<JobApplication> findByJobPost(JobPost jobPost);

    @Query("SELECT ja FROM JobApplication ja JOIN FETCH ja.worker WHERE ja.jobPost = :jobPost")
    List<JobApplication> findByJobPostWithWorker(@Param("jobPost") JobPost jobPost);

    boolean existsByJobPostAndWorker(JobPost jobPost, User worker);

    @Query("SELECT ja FROM JobApplication ja JOIN FETCH ja.worker JOIN FETCH ja.jobPost jp JOIN FETCH jp.employerProfile WHERE ja.id = :id")
    Optional<JobApplication> findByIdWithParticipantDetails(@Param("id") Long id);
}
