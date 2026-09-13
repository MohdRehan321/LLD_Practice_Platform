package com.practiceplatform.lld.TestService;

import com.practiceplatform.lld.enums.*;
import com.practiceplatform.lld.exception.*;
import com.practiceplatform.lld.model.entity.Attempt;
import com.practiceplatform.lld.model.entity.Evaluation;
import com.practiceplatform.lld.model.entity.Submission;
import com.practiceplatform.lld.repository.*;
import com.practiceplatform.lld.service.EvaluationService;
import com.practiceplatform.lld.service.SubmissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock private AttemptRepository attemptRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private EvaluationRepository evaluationRepository;
    @Mock private EvaluationService evaluationService;

    @InjectMocks
    private SubmissionService submissionService;

    @Test
    void submit_Success_CreatesSubmissionAndTriggersEvaluation() {
        // Arrange
        UUID attemptId = UUID.randomUUID();
        Attempt mockAttempt = new Attempt();
        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(mockAttempt));
        when(evaluationRepository.existsBySubmission_AttemptAndStatusIn(
                eq(mockAttempt), anyList())).thenReturn(false);

        // Act
        Submission result = submissionService.submit(attemptId, "My LLD answer", SubmissionFormat.TEXT);

        // Assert
        assertNotNull(result);
        assertEquals("My LLD answer", result.getContent());
        verify(submissionRepository).save(any(Submission.class));
        verify(evaluationRepository).save(any(Evaluation.class));
        verify(evaluationService).runEvaluationAsync(any()); // Verifies the async handoff
    }

    @Test
    void submit_Duplicate_ThrowsDuplicateSubmissionException() {
        // Arrange
        UUID attemptId = UUID.randomUUID();
        Attempt mockAttempt = new Attempt();
        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(mockAttempt));

        // Simulate an evaluation already running
        when(evaluationRepository.existsBySubmission_AttemptAndStatusIn(
                eq(mockAttempt), anyList())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateSubmissionException.class, () ->
                submissionService.submit(attemptId, "My LLD answer", SubmissionFormat.TEXT)
        );

        // Ensure we didn't save anything or trigger the AI
        verify(submissionRepository, never()).save(any());
        verify(evaluationService, never()).runEvaluationAsync(any());
    }

    @Test
    void submit_InvalidAttemptId_ThrowsResourceNotFoundException() {
        UUID attemptId = UUID.randomUUID();
        when(attemptRepository.findById(attemptId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                submissionService.submit(attemptId, "content", SubmissionFormat.TEXT)
        );
    }
}
