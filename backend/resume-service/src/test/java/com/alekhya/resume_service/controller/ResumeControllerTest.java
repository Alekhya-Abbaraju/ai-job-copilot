
package com.alekhya.resume_service.controller;

import com.alekhya.resume_service.config.SecurityConfig;
import com.alekhya.resume_service.entity.Resume;
import com.alekhya.resume_service.service.ResumeAnalysisService;
import com.alekhya.resume_service.service.ResumeService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import java.time.Instant;
import java.util.List;
import static org.mockito.Mockito.doNothing;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.alekhya.resume_service.dto.ResumeAnalysisRequestResponse;
import com.alekhya.resume_service.dto.JobDescriptionMatchRequest;
import com.alekhya.resume_service.dto.JobDescriptionMatchResponse;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ResumeController.class,
        properties = "jwt.secret=test-secret-key-for-unit-tests-123456"
)
@Import({
        SecurityConfig.class,
        ResumeControllerTest.TestSecurityConfiguration.class
})
class ResumeControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfiguration {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumeService resumeService;

    @MockitoBean
    private ResumeAnalysisService resumeAnalysisService;

    @Test
    void shouldReturnEmptyResumeList() throws Exception {

        when(resumeService.getMyResumes(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/resumes").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnResumeList() throws Exception {

        Resume resume = mock(Resume.class);
        Instant now = Instant.parse("2026-11-21T10:00:00Z");

        when(resume.getId()).thenReturn(1L);
        when(resume.getOriginalFilename()).thenReturn("resume.pdf");
        when(resume.getContentType()).thenReturn("application/pdf");
        when(resume.getFileSize()).thenReturn(1024L);
        when(resume.getStatus()).thenReturn("UPLOADED");
        when(resume.getCreatedAt()).thenReturn(now);
        when(resume.getUpdatedAt()).thenReturn(now);

        when(resumeService.getMyResumes(any()))
                .thenReturn(List.of(resume));

        mockMvc.perform(get("/api/resumes").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].originalFilename").value("resume.pdf"))
                .andExpect(jsonPath("$[0].contentType").value("application/pdf"))
                .andExpect(jsonPath("$[0].fileSize").value(1024))
                .andExpect(jsonPath("$[0].status").value("UPLOADED"));
    }
    @Test
    void shouldReturnResumeById() throws Exception {

        Resume resume = mock(Resume.class);
        Instant now = Instant.parse("2026-11-21T10:00:00Z");

        when(resume.getId()).thenReturn(1L);
        when(resume.getOriginalFilename()).thenReturn("resume.pdf");
        when(resume.getContentType()).thenReturn("application/pdf");
        when(resume.getFileSize()).thenReturn(1024L);
        when(resume.getStatus()).thenReturn("UPLOADED");
        when(resume.getCreatedAt()).thenReturn(now);
        when(resume.getUpdatedAt()).thenReturn(now);
        when(resumeService.getMyResume(eq(1L), any()))
                .thenReturn(resume);
        mockMvc.perform(get("/api/resumes/1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originalFilename").value("resume.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").value(1024))
                .andExpect(jsonPath("$.status").value("UPLOADED"));
    }
    @Test
    void shouldReturn404WhenResumeNotFound() throws Exception {

        when(resumeService.getMyResume(eq(99L), any()))
                .thenThrow(new ResumeService.ResumeNotFoundException(
                        "Resume not found"
                ));

        mockMvc.perform(get("/api/resumes/99").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resume not found"));
    }
    @Test
    void shouldDeleteResumeSuccessfully() throws Exception {

        doNothing().when(resumeService).deleteResume(eq(1L), any());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/resumes/1")
                                .with(jwt())
                )
                .andExpect(status().isNoContent());
    }
    @Test
    void shouldReExtractResumeTextSuccessfully() throws Exception {

        Resume resume = mock(Resume.class);
        Instant now = Instant.parse("2026-11-21T10:00:00Z");

        when(resume.getId()).thenReturn(1L);
        when(resume.getOriginalFilename()).thenReturn("resume.pdf");
        when(resume.getContentType()).thenReturn("application/pdf");
        when(resume.getFileSize()).thenReturn(1024L);
        when(resume.getStatus()).thenReturn("UPLOADED");
        when(resume.getCreatedAt()).thenReturn(now);
        when(resume.getUpdatedAt()).thenReturn(now);

        when(resumeService.reExtractText(eq(1L), any()))
                .thenReturn(resume);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/resumes/1/extract")
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originalFilename").value("resume.pdf"))
                .andExpect(jsonPath("$.status").value("UPLOADED"));
    }
    @Test
    void shouldUploadResumeSuccessfully() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "sample resume content".getBytes()
        );

        Resume resume = mock(Resume.class);
        Instant now = Instant.parse("2026-11-21T10:00:00Z");

        when(resume.getId()).thenReturn(1L);
        when(resume.getOriginalFilename()).thenReturn("resume.pdf");
        when(resume.getContentType()).thenReturn("application/pdf");
        when(resume.getFileSize()).thenReturn(1024L);
        when(resume.getStatus()).thenReturn("UPLOADED");
        when(resume.getCreatedAt()).thenReturn(now);
        when(resume.getUpdatedAt()).thenReturn(now);

        when(resumeService.upload(
                any(MultipartFile.class),
                any(Jwt.class)
        )).thenReturn(resume);

        mockMvc.perform(multipart("/api/resumes")
                        .file(file)
                        .with(jwt()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originalFilename").value("resume.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").value(1024))
                .andExpect(jsonPath("$.status").value("UPLOADED"));
    }
    @Test
    void shouldReturn400WhenFileIsInvalid() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                "text/plain",
                "invalid file".getBytes()
        );

        when(resumeService.upload(
                any(MultipartFile.class),
                any(Jwt.class)
        )).thenThrow(new IllegalArgumentException("Invalid file type."));

        mockMvc.perform(multipart("/api/resumes")
                        .file(file)
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid file type."));
    }
    @Test
    void shouldReturn413WhenFileExceedsUploadLimit() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-resume.pdf",
                "application/pdf",
                "sample content".getBytes()
        );

        when(resumeService.upload(
                any(MultipartFile.class),
                any(Jwt.class)
        )).thenThrow(
                new MaxUploadSizeExceededException(5L * 1024 * 1024)
        );

        mockMvc.perform(multipart("/api/resumes")
                        .file(file)
                        .with(jwt()))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error")
                        .value("Resume file must not exceed 5 MB."));
    }
    @Test
    void shouldPreviewResumeSuccessfully() throws Exception {

        byte[] pdfContent = "%PDF-1.4 sample content".getBytes();

        ResumeService.ResumeFile resumeFile =
                new ResumeService.ResumeFile(
                        pdfContent,
                        "resume.pdf",
                        "application/pdf"
                );

        when(resumeService.getResumeFile(eq(1L), any()))
                .thenReturn(resumeFile);

        mockMvc.perform(get("/api/resumes/1/preview").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(pdfContent))
                .andExpect(header().string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString("inline")
                ))
                .andExpect(header().string(
                        "X-Content-Type-Options",
                        "nosniff"
                ))
                .andExpect(header().string(
                        "Cache-Control",
                        "private, no-store"
                ));
    }
    @Test
    void shouldReturn404WhenPreviewResumeNotFound() throws Exception {

        when(resumeService.getResumeFile(eq(1L), any()))
                .thenThrow(new ResumeService.ResumeNotFoundException("Resume not found"));

        mockMvc.perform(get("/api/resumes/1/preview").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resume not found"));
    }
    @Test
    void shouldAnalyzeResumeSuccessfully() throws Exception {

        ResumeAnalysisRequestResponse response =
                new ResumeAnalysisRequestResponse(
                        100L,
                        1L,
                        "PENDING",
                        Instant.parse("2026-11-24T10:00:00Z"),
                        "Resume analysis has been queued."
                );

        when(resumeAnalysisService.analyzeResume(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/resumes/1/analyze").with(jwt()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.analysisId").value(100))
                .andExpect(jsonPath("$.resumeId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message")
                        .value("Resume analysis has been queued."));
    }
    @Test
    void shouldReturn500WhenAnalysisFails() throws Exception {
        when(resumeAnalysisService.analyzeResume(eq(1L), any()))
                .thenThrow(new RuntimeException("AI service unavailable"));

        mockMvc.perform(post("/api/resumes/1/analyze").with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error")
                        .value("An unexpected error occurred."));
    }
    @Test
    void shouldMatchResumeWithJobSuccessfully() throws Exception {
        JobDescriptionMatchRequest request =
                new JobDescriptionMatchRequest("Java Spring Boot Developer");

        JobDescriptionMatchResponse response = new JobDescriptionMatchResponse(
                1L,
                "resume.pdf",
                85,
                List.of("Java", "Spring Boot"),
                List.of("AWS"),
                List.of("REST API development"),
                List.of("Limited cloud experience"),
                List.of("Improve AWS knowledge")
        );

        when(resumeAnalysisService.matchResumeWithJob(eq(1L), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/resumes/1/match")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "jobDescription": "Java Spring Boot Developer"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeId").value(1))
                .andExpect(jsonPath("$.matchScore").value(85))
                .andExpect(jsonPath("$.matchingSkills[0]").value("Java"))
                .andExpect(jsonPath("$.missingSkills[0]").value("AWS"));
    }
    @Test
    void shouldReturn400WhenJobDescriptionIsBlank() throws Exception {
        mockMvc.perform(post("/api/resumes/1/match")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "jobDescription": "   "
                            }
                            """))
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldReturn400WhenJobDescriptionExceedsLimit() throws Exception {
        String longDescription = "a".repeat(15001);
        mockMvc.perform(post("/api/resumes/1/match")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new JobDescriptionMatchRequest(longDescription))))
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldReturn404WhenMatchingResumeNotFound() throws Exception {
        when(resumeAnalysisService.matchResumeWithJob(
                eq(1L), any(JobDescriptionMatchRequest.class), any()))
                .thenThrow(new ResumeService.ResumeNotFoundException("Resume not found"));

        mockMvc.perform(post("/api/resumes/1/match")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "jobDescription": "Java Spring Boot Developer"
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resume not found"));
    }
    @Test
    void shouldReturn500WhenJobMatchingFails() throws Exception {
        when(resumeAnalysisService.matchResumeWithJob(
                eq(1L), any(JobDescriptionMatchRequest.class), any()))
                .thenThrow(new RuntimeException("AI service unavailable"));

        mockMvc.perform(post("/api/resumes/1/match")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "jobDescription": "Java Spring Boot Developer"
                            }
                            """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error")
                        .value("An unexpected error occurred."));
    }
}
