package com.practiceplatform.lld.repository;

import com.practiceplatform.lld.model.entity.Attempt;
import com.practiceplatform.lld.model.entity.Evaluation;
import com.practiceplatform.lld.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {
    // Required for the idempotency check in SubmissionService
    boolean existsBySubmission_AttemptAndStatusIn(Attempt attempt, List<SubmissionStatus> statuses);
}