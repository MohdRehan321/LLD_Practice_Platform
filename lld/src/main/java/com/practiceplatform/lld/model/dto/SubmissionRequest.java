package com.practiceplatform.lld.model.dto;

import com.practiceplatform.lld.enums.SubmissionFormat;

public record SubmissionRequest(
        String content,
        SubmissionFormat format
) {}