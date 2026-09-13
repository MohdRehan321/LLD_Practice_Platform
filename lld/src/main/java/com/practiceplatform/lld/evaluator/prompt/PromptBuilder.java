package com.practiceplatform.lld.evaluator.prompt;


import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildSystemPrompt() {
        return """
            You are an expert Principal Software Engineer evaluating a Low-Level Design (LLD) submission.
            You must evaluate the submission strictly against the provided Rubric.
            
            OUTPUT RULES:
            1. You MUST return ONLY valid JSON. No markdown formatting, no conversational text.
            2. The JSON must contain a single root key called "feedbackItems" which is an array of objects.
            3. Each object must EXACTLY match this structure:
               - criterion (String, e.g. "Coupling & cohesion")
               - score (Integer, 1-5)
               - evidence (String, direct reference to the candidate's text)
               - concern (String, or null if perfect)
               - suggestion (String, or null if perfect)
               - confidence (String, strictly "HIGH", "MEDIUM", or "LOW")
            """;
    }

    public String buildUserPrompt(String problemDescription, String rubric, String candidateSubmission) {
        return """
            ### PROBLEM DESCRIPTION ###
            %s
            
            ### EVALUATION RUBRIC ###
            %s
            
            ### CANDIDATE SUBMISSION ###
            %s
            """.formatted(problemDescription, rubric, candidateSubmission);
    }
}