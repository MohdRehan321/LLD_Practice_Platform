package com.practiceplatform.lld.TestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practiceplatform.lld.controller.PracticeController;
import com.practiceplatform.lld.enums.*;
import com.practiceplatform.lld.exception.DuplicateSubmissionException;
import com.practiceplatform.lld.model.entity.Submission;
import com.practiceplatform.lld.model.dto.SubmissionRequest;
import com.practiceplatform.lld.service.SubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PracticeController.class)
class PracticeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private SubmissionService submissionService;
    @Test
    void createSubmission_Returns202Accepted() throws Exception {
        UUID attemptId = UUID.randomUUID();
        SubmissionRequest request = new SubmissionRequest("Code content", SubmissionFormat.TEXT);

        Submission mockSubmission = new Submission();
        mockSubmission.setId(UUID.randomUUID());

        when(submissionService.submit(any(), any(), any())).thenReturn(mockSubmission);

        mockMvc.perform(post("/api/v1/attempts/{attemptId}/submissions", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted()) // Verifies 202 status
                .andExpect(jsonPath("$.submissionId").value(mockSubmission.getId().toString()))
                .andExpect(jsonPath("$.status").value("EVALUATING"));
    }

    @Test
    void createSubmission_WhenDuplicate_Returns409Conflict() throws Exception {
        UUID attemptId = UUID.randomUUID();
        SubmissionRequest request = new SubmissionRequest("Code content", SubmissionFormat.TEXT);

        when(submissionService.submit(any(), any(), any()))
                .thenThrow(new DuplicateSubmissionException("Evaluation in progress"));

        mockMvc.perform(post("/api/v1/attempts/{attemptId}/submissions", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // Verifies the GlobalExceptionHandler works
    }
}
