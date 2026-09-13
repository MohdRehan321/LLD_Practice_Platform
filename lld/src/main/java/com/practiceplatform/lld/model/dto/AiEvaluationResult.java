package com.practiceplatform.lld.model.dto;
import java.util.List;

public record AiEvaluationResult(
        boolean success,
        List<FeedbackItem> feedbackItems,
        String errorMessage
) {}