package com.practiceplatform.lld.model.dto;
import com.practiceplatform.lld.enums.SubmissionStatus;
import java.util.List;
import java.util.UUID;

public record EvaluationResponse(
        UUID evaluationId,
        SubmissionStatus status,
        List<FeedbackItem> feedback,
        String errorMessage
) {}