
package com.alekhya.resume_service.service;

import com.alekhya.resume_service.dto.*;
import com.alekhya.resume_service.entity.Resume;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.alekhya.resume_service.repository.ResumeAnalysisRepository;
import com.alekhya.resume_service.kafka.producer.ResumeAnalysisProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alekhya.resume_service.entity.ResumeAnalysis;

import java.util.Optional;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResumeAnalysisServiceTest {

    private ResumeService resumeService;
    private ChatClient chatClient;
    private ChatClient.Builder chatClientBuilder;
    private ResumeAnalysisService resumeAnalysisService;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;
    private ResumeAnalysisProducer resumeAnalysisProducer;
    private ResumeAnalysisRepository analysisRepository;
    private ObjectMapper objectMapper;
    private Jwt jwt;
    private Resume resume;
    @BeforeEach
    void setUp() {

        resumeService = mock(ResumeService.class);
        resumeAnalysisProducer = mock(ResumeAnalysisProducer.class);
        analysisRepository = mock(ResumeAnalysisRepository.class);
        chatClient = mock(ChatClient.class);
        chatClientBuilder = mock(ChatClient.Builder.class);
        objectMapper = new ObjectMapper();

        when(chatClientBuilder.build()).thenReturn(chatClient);

        resumeAnalysisService = new ResumeAnalysisService(
                resumeService,
                resumeAnalysisProducer,
                analysisRepository,
                chatClientBuilder,
                objectMapper
        );

        jwt = mock(Jwt.class);

        resume = new Resume();
        resume.setOriginalFilename("test-resume.pdf");
        resume.setExtractedText("Java, Spring Boot, REST APIs");

        try {
            var field = Resume.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(resume, 1L);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        when(resumeService.getMyResume(1L, jwt)).thenReturn(resume);

        // Mock database save and assign an ID to the saved analysis.
        when(analysisRepository.save(any(ResumeAnalysis.class)))
                .thenAnswer(invocation -> {
                    ResumeAnalysis analysis = invocation.getArgument(0);

                    try {
                        var field = ResumeAnalysis.class.getDeclaredField("id");
                        field.setAccessible(true);
                        field.set(analysis, 100L);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }

                    return analysis;
                });
    }
    @Test
    void shouldCreateResumeAnalysisRequestSuccessfully() {

        ResumeAnalysisRequestResponse response =
                resumeAnalysisService.analyzeResume(1L, jwt);

        assertNotNull(response);
        assertEquals(1L, response.resumeId());
        assertEquals("PENDING", response.status());
        assertNotNull(response.analysisId());
        assertNotNull(response.requestedAt());
    }

    @Test
    void shouldRejectResumeWhenExtractedTextIsEmpty() {

        resume.setExtractedText("");

        assertThrows(
                ResponseStatusException.class,
                () -> resumeAnalysisService.analyzeResume(1L, jwt)
        );

        verifyNoInteractions(chatClient);
    }

    @Test
    void shouldRejectJobMatchingWhenJobDescriptionIsEmpty() {

        JobDescriptionMatchRequest request =
                new JobDescriptionMatchRequest("");

        assertThrows(
                ResponseStatusException.class,
                () -> resumeAnalysisService.matchResumeWithJob(
                        1L, request, jwt
                )
        );

        verifyNoInteractions(chatClient);
    }
    @Test
    void shouldMatchResumeWithJobSuccessfully() {

        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        JobMatchInsights insights = new JobMatchInsights(
                85,
                List.of("Java", "Spring Boot", "REST APIs"),
                List.of("Docker"),
                List.of("Backend development", "API development"),
                List.of("Limited cloud experience"),
                List.of("Highlight relevant backend projects")
        );

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(any(java.util.function.Consumer.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(JobMatchInsights.class))
                .thenReturn(insights);

        JobDescriptionMatchRequest request =
                new JobDescriptionMatchRequest(
                        "Looking for a Java developer with Spring Boot and Docker experience."
                );

        JobDescriptionMatchResponse response =
                resumeAnalysisService.matchResumeWithJob(1L, request, jwt);

        assertNotNull(response);
        assertEquals(85, response.matchScore());
        assertEquals("test-resume.pdf", response.filename());

        assertEquals(
                List.of("Java", "Spring Boot", "REST APIs"),
                response.matchingSkills()
        );

        assertEquals(
                List.of("Docker"),
                response.missingSkills()
        );

        verify(chatClient).prompt();
    }
    @Test
    void shouldRejectInvalidJobMatchScore() {

        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        JobMatchInsights insights = new JobMatchInsights(
                120, // Invalid score: greater than 100
                List.of("Java"),
                List.of("Docker"),
                List.of("Backend development"),
                List.of(),
                List.of()
        );

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(any(java.util.function.Consumer.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(JobMatchInsights.class))
                .thenReturn(insights);

        JobDescriptionMatchRequest request =
                new JobDescriptionMatchRequest("Java developer required.");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> resumeAnalysisService.matchResumeWithJob(1L, request, jwt)
        );

        assertEquals(
                "AI returned an invalid job match score.",
                exception.getMessage()
        );
    }
    @Test
    void shouldReturnEmptyListsWhenAIResponseListsAreNull() {

        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        JobMatchInsights insights = new JobMatchInsights(
                75,
                null,
                null,
                null,
                null,
                null
        );

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(any(java.util.function.Consumer.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(JobMatchInsights.class))
                .thenReturn(insights);

        JobDescriptionMatchRequest request =
                new JobDescriptionMatchRequest("Java developer required.");

        JobDescriptionMatchResponse response =
                resumeAnalysisService.matchResumeWithJob(1L, request, jwt);

        assertNotNull(response);
        assertEquals(75, response.matchScore());

        assertNotNull(response.matchingSkills());
        assertNotNull(response.missingSkills());
        assertNotNull(response.matchedRequirements());
        assertNotNull(response.experienceGaps());
        assertNotNull(response.recommendations());

        assertTrue(response.matchingSkills().isEmpty());
        assertTrue(response.missingSkills().isEmpty());
        assertTrue(response.matchedRequirements().isEmpty());
        assertTrue(response.experienceGaps().isEmpty());
        assertTrue(response.recommendations().isEmpty());
    }
    @Test
    void shouldRejectNullJobMatchScore() {

        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        JobMatchInsights insights = new JobMatchInsights(
                null,
                List.of("Java"),
                List.of("Docker"),
                List.of("Backend development"),
                List.of(),
                List.of()
        );

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(any(java.util.function.Consumer.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(JobMatchInsights.class))
                .thenReturn(insights);

        JobDescriptionMatchRequest request =
                new JobDescriptionMatchRequest("Java developer required.");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> resumeAnalysisService.matchResumeWithJob(1L, request, jwt)
        );

        assertEquals(
                "AI returned an invalid job match score.",
                exception.getMessage()
        );
    }
    @Test
    void shouldRejectNullAIResponse() {

        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(any(java.util.function.Consumer.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(JobMatchInsights.class))
                .thenReturn(null);

        JobDescriptionMatchRequest request =
                new JobDescriptionMatchRequest("Java developer required.");

        assertThrows(
                IllegalStateException.class,
                () -> resumeAnalysisService.matchResumeWithJob(1L, request, jwt)
        );
    }
}
