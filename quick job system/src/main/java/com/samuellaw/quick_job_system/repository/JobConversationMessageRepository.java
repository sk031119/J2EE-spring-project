package com.samuellaw.quick_job_system.repository;

import com.samuellaw.quick_job_system.entity.JobConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface JobConversationMessageRepository extends JpaRepository<JobConversationMessage, Long> {

    @Query("SELECT m FROM JobConversationMessage m JOIN FETCH m.sender WHERE m.conversation.id = :cid ORDER BY m.createdAt ASC")
    List<JobConversationMessage> findByConversationIdWithSenderOrdered(@Param("cid") Long conversationId);
}
