package com.samuellaw.quick_job_system.repository;

import com.samuellaw.quick_job_system.entity.EmployerProfile;
import com.samuellaw.quick_job_system.entity.JobConversation;
import com.samuellaw.quick_job_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobConversationRepository extends JpaRepository<JobConversation, Long> {

    Optional<JobConversation> findByJobPost_IdAndWorker_Id(Long jobPostId, Long workerId);

    @Query("SELECT c FROM JobConversation c JOIN FETCH c.jobPost jp JOIN FETCH jp.employerProfile WHERE c.worker = :worker ORDER BY c.updatedAt DESC")
    List<JobConversation> findByWorkerWithJobAndEmployer(@Param("worker") User worker);

    @Query("SELECT c FROM JobConversation c JOIN FETCH c.worker WHERE c.jobPost.id = :jobId ORDER BY c.updatedAt DESC")
    List<JobConversation> findByJobPostIdWithWorker(@Param("jobId") Long jobId);

    @Query("SELECT c FROM JobConversation c JOIN FETCH c.jobPost jp JOIN FETCH c.worker WHERE jp.employerProfile = :profile ORDER BY c.updatedAt DESC")
    List<JobConversation> findByJobPostEmployerProfileWithJobAndWorker(@Param("profile") EmployerProfile profile);

    @Query("SELECT c FROM JobConversation c JOIN FETCH c.jobPost jp JOIN FETCH jp.employerProfile JOIN FETCH c.worker WHERE c.id = :id")
    Optional<JobConversation> findByIdWithParticipants(@Param("id") Long id);
}
