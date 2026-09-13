package com.practiceplatform.lld.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practiceplatform.lld.model.dto.EvaluationResponse;
import com.practiceplatform.lld.model.entity.Evaluation;
import com.practiceplatform.lld.model.entity.Submission;
import com.practiceplatform.lld.enums.SubmissionStatus;
import com.practiceplatform.lld.model.dto.SubmissionResponse;
import com.practiceplatform.lld.model.dto.SubmissionRequest;
import com.practiceplatform.lld.model.dto.FeedbackItem;
import com.practiceplatform.lld.service.EvaluationService;
import com.practiceplatform.lld.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PracticeController {

    private final SubmissionService submissionService;
    private final EvaluationService evaluationService; // Assuming a getEvaluation method exists
    private final ObjectMapper objectMapper;

    @PostMapping("/attempts/{attemptId}/submissions")
    public ResponseEntity<SubmissionResponse> createSubmission(
            @PathVariable UUID attemptId,
            @RequestBody SubmissionRequest request) {

        // Triggers the DB save and background @Async thread
        Submission submission = submissionService.submit(
                attemptId,
                request.content(),
                request.format()
        );

        SubmissionResponse response = new SubmissionResponse(
                submission.getId(),
                "Submission received. Evaluation is processing in the background.",
                SubmissionStatus.EVALUATING
        );

        // Returning 202 Accepted is the correct REST pattern for async processing
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/evaluations/{evaluationId}")
    public ResponseEntity<EvaluationResponse> getEvaluation(@PathVariable UUID evaluationId) {

        Evaluation evaluation = evaluationService.getEvaluation(evaluationId);
        List<FeedbackItem> feedback = null;

        if (evaluation.getFeedbackPayload() != null) {
            try {
                feedback = objectMapper.readValue(
                        evaluation.getFeedbackPayload(),
                        new TypeReference<List<FeedbackItem>>() {}
                );
            } catch (Exception e) {
                // Log error; handle gracefully
            }
        }

        EvaluationResponse response = new EvaluationResponse(
                evaluation.getId(),
                evaluation.getStatus(),
                feedback,
                evaluation.getErrorMessage()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Exception Handler for Idempotency.
     * Translates the IllegalStateException from SubmissionService into a 409 Conflict.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
