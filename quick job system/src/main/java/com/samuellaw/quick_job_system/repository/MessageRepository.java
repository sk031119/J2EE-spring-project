// Requirements: Message data access - find messages for an application ordered by time
package com.samuellaw.quick_job_system.repository;

import com.samuellaw.quick_job_system.entity.JobApplication;
import com.samuellaw.quick_job_system.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByJobApplicationOrderByCreatedAtAsc(JobApplication jobApplication);

    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.jobApplication = :app ORDER BY m.createdAt ASC")
    List<Message> findByJobApplicationWithSenderOrderByCreatedAt(@Param("app") JobApplication app);
}
