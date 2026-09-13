package com.practiceplatform.lld.model.dto;

import com.practiceplatform.lld.enums.SubmissionStatus;
import java.util.UUID;

public record SubmissionResponse(
        UUID submissionId,
        String message,
        SubmissionStatus status
) {}