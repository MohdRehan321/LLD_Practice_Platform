package com.practiceplatform.lld.model.dto;

public record FeedbackItem(
        String criterion,
        int score,
        String evidence,
        String concern,
        String suggestion,
        String confidence
) {}