package com.practiceplatform.lld.repository;

import com.practiceplatform.lld.model.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {}
