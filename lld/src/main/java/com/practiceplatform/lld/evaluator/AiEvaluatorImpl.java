package com.practiceplatform.lld.evaluator;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practiceplatform.lld.model.dto.AiEvaluationResult;
import com.practiceplatform.lld.model.entity.Problem;
import com.practiceplatform.lld.model.entity.Submission;
import com.practiceplatform.lld.evaluator.prompt.PromptBuilder;
import com.practiceplatform.lld.model.dto.FeedbackItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class AiEvaluatorImpl implements Evaluator {

    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String openAiApiKey;

    public AiEvaluatorImpl(
            PromptBuilder promptBuilder,
            ObjectMapper objectMapper,
            @Value("${openai.api.key}") String openAiApiKey) {
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
        this.openAiApiKey = openAiApiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public AiEvaluationResult evaluate(Submission submission, Problem problem, String rubric) {
        try {
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(problem.getDescription(), rubric, submission.getContent());

            // Build OpenAI JSON Payload
            var requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "gpt-4o-mini"); // Fast and cheap for MVP
            requestBody.putObject("response_format").put("type", "json_object"); // Force JSON

            var messages = requestBody.putArray("messages");
            messages.addObject().put("role", "system").put("content", systemPrompt);
            messages.addObject().put("role", "user").put("content", userPrompt);

            // Execute HTTP Request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("OpenAI API failed: {}", response.body());
                return new AiEvaluationResult(false, null, "AI Provider returned status " + response.statusCode());
            }

            // Parse response
            JsonNode rootNode = objectMapper.readTree(response.body());
            String aiJsonContent = rootNode.path("choices").get(0).path("message").path("content").asText();

            // Extract the feedbackItems array specifically
            JsonNode feedbackItemsNode = objectMapper.readTree(aiJsonContent).path("feedbackItems");
            List<FeedbackItem> feedbackItems = objectMapper.convertValue(
                    feedbackItemsNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, FeedbackItem.class)
            );

            return new AiEvaluationResult(true, feedbackItems, null);

        } catch (Exception e) {
            log.error("Evaluation failed during AI execution", e);
            return new AiEvaluationResult(false, null, "AI evaluation failed: " + e.getMessage());
        }
    }
}