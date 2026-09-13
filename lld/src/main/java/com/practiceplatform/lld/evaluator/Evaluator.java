package com.practiceplatform.lld.evaluator;

import com.practiceplatform.lld.model.dto.AiEvaluationResult;
import com.practiceplatform.lld.model.entity.Problem;
import com.practiceplatform.lld.model.entity.Submission;

public interface Evaluator {

    /**
     * Core evaluation method decoupled from execution style.
     */
    AiEvaluationResult evaluate(Submission submission, Problem problem, String rubric);
}