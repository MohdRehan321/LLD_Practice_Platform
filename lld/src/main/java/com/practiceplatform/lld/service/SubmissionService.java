package com.practiceplatform.lld.service;

import com.practiceplatform.lld.enums.SubmissionFormat;
import com.practiceplatform.lld.enums.SubmissionStatus;
import com.practiceplatform.lld.exception.DuplicateSubmissionException;
import com.practiceplatform.lld.exception.ResourceNotFoundException;
import com.practiceplatform.lld.model.entity.Attempt;
import com.practiceplatform.lld.model.entity.Evaluation;
import com.practiceplatform.lld.model.entity.Submission;
import com.practiceplatform.lld.repository.AttemptRepository;
import com.practiceplatform.lld.repository.EvaluationRepository;
import com.practiceplatform.lld.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final AttemptRepository attemptRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
    private final EvaluationService evaluationService;

    @Transactional
    public Submission submit(UUID attemptId, String content, SubmissionFormat format) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        // 1. Idempotency Check: Don't process if an evaluation is already running
        boolean isAlreadyProcessing = evaluationRepository.existsBySubmission_AttemptAndStatusIn(
                attempt,
                java.util.List.of(SubmissionStatus.PENDING, SubmissionStatus.EVALUATING)
        );
        if (isAlreadyProcessing) {
            throw new DuplicateSubmissionException("An evaluation is already in progress for this attempt.");
        }

        // 2. Create Submission
        Submission submission = new Submission();
        submission.setAttempt(attempt);
        submission.setContent(content);
        submission.setFormat(format);
        submissionRepository.save(submission);

        // 3. Create initial Evaluation state (PENDING)
        Evaluation evaluation = new Evaluation();
        evaluation.setSubmission(submission);
        evaluation.setStatus(SubmissionStatus.PENDING);
        evaluationRepository.save(evaluation);

        // 4. Trigger Async processing (passing ID, not the entity!)
        evaluationService.runEvaluationAsync(evaluation.getId());

        return submission;
    }
}