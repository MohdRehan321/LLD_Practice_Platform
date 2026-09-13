package com.practiceplatform.lld.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practiceplatform.lld.enums.SubmissionStatus;
import com.practiceplatform.lld.evaluator.Evaluator;
import com.practiceplatform.lld.model.dto.AiEvaluationResult;
import com.practiceplatform.lld.model.entity.Evaluation;
import com.practiceplatform.lld.model.entity.Problem;
import com.practiceplatform.lld.model.entity.Submission;
import com.practiceplatform.lld.repository.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

        private final EvaluationRepository evaluationRepository;
        private final Evaluator evaluator; // Injected Strategy (AiEvaluatorImpl)
        private final ObjectMapper objectMapper;

        @Async("evaluationTaskExecutor") // Using the bounded executor we discussed
        public void runEvaluationAsync(UUID evaluationId) {
                log.info("Starting background evaluation for ID: {}", evaluationId);

                // 1. Mark as EVALUATING
                Evaluation eval = evaluationRepository.findById(evaluationId).orElseThrow();
                eval.setStatus(SubmissionStatus.EVALUATING);
                evaluationRepository.save(eval);

                try {
                        Submission submission = eval.getSubmission();
                        Problem problem = submission.getAttempt().getProblem();
                        String rubric = problem.getRubricPayload();

                        // 2. Call AI
                        AiEvaluationResult result = evaluator.evaluate(submission, problem, rubric);

                        // 3. Save Result
                        String jsonPayload = objectMapper.writeValueAsString(result.feedbackItems());
                        eval.setStatus(SubmissionStatus.COMPLETED);
                        eval.setFeedbackPayload(jsonPayload);
                        evaluationRepository.save(eval);

                } catch (Exception e) {
                        log.error("Evaluation failed for ID: {}", evaluationId, e);
                        // 4. Fallback to FAILED
                        eval.setStatus(SubmissionStatus.FAILED);
                        eval.setErrorMessage(e.getMessage());
                        evaluationRepository.save(eval);
                }

        }

        /**
         * Helper to update state in short, isolated transactions.
         * REQUIRES_NEW ensures that even if the caller transaction failed, this saves.
         */

        public Evaluation getEvaluation(UUID evaluationId) {
                return evaluationRepository.findById(evaluationId)
                        .orElseThrow(() -> new com.practiceplatform.lld.exception.ResourceNotFoundException("Evaluation not found"));
        }
}