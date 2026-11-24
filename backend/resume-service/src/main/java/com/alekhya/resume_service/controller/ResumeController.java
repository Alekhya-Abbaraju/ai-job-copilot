package com.alekhya.resume_service.controller;

import com.alekhya.resume_service.dto.JobDescriptionMatchRequest;
import com.alekhya.resume_service.dto.JobDescriptionMatchResponse;
import com.alekhya.resume_service.dto.ResumeResponse;
import com.alekhya.resume_service.entity.Resume;
import com.alekhya.resume_service.service.ResumeService;
import com.alekhya.resume_service.dto.ResumeAnalysisRequestResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.alekhya.resume_service.dto.ResumeAnalysisResultResponse;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import com.alekhya.resume_service.dto.ResumeAnalysisResponse;
import com.alekhya.resume_service.service.ResumeAnalysisService;
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeAnalysisService resumeAnalysisService;
    public ResumeController(ResumeService resumeService,ResumeAnalysisService resumeAnalysisService) {
        this.resumeService = resumeService;
        this.resumeAnalysisService=resumeAnalysisService;
    }

    // Upload a resume
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {

        Resume resume = resumeService.upload(file, jwt);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResumeResponse.from(resume));
    }

    // Get all resumes belonging to the logged-in user
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getMyResumes(
            @AuthenticationPrincipal Jwt jwt) {

        List<ResumeResponse> resumes = resumeService.getMyResumes(jwt)
                .stream()
                .map(ResumeResponse::from)
                .toList();

        return ResponseEntity.ok(resumes);
    }
    // Get one resume belonging to the logged-in user
    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getMyResume(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Resume resume = resumeService.getMyResume(id, jwt);

        return ResponseEntity.ok(ResumeResponse.from(resume));
    }

    // Re-extract text from an existing resume
    @PostMapping("/{id}/extract")
    public ResponseEntity<ResumeResponse> reExtractText(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {

        Resume resume = resumeService.reExtractText(id, jwt);

        return ResponseEntity.ok(ResumeResponse.from(resume));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException{
        resumeService.deleteResume(id,jwt);
        return ResponseEntity.noContent().build();
    }
    // Preview the original resume PDF in the browser
    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> previewResume(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {
        ResumeService.ResumeFile resumeFile =
                resumeService.getResumeFile(id, jwt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(resumeFile.contentType())
        );
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename(
                                resumeFile.filename(),
                                StandardCharsets.UTF_8
                        )
                        .build()
        );
        headers.setContentLength(resumeFile.content().length);
        headers.setCacheControl("private, no-store");
        headers.set("X-Content-Type-Options", "nosniff");
        return new ResponseEntity<>(
                resumeFile.content(),
                headers,
                HttpStatus.OK
        );
    }
    @PostMapping("/{id}/analyze")
    public ResponseEntity<ResumeAnalysisRequestResponse> analyzeResume(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ResumeAnalysisRequestResponse response =
                resumeAnalysisService.analyzeResume(id, jwt);

        return ResponseEntity.accepted().body(response);
    }
    @PostMapping("/{id}/match")
    public ResponseEntity<JobDescriptionMatchResponse> matchResume(
            @PathVariable Long id,
            @Valid @RequestBody JobDescriptionMatchRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        JobDescriptionMatchResponse response =
                resumeAnalysisService.matchResumeWithJob(id, request, jwt);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/analyses/{analysisId}")
    public ResponseEntity<ResumeAnalysisResultResponse> getAnalysisResult(
            @PathVariable Long analysisId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ResumeAnalysisResultResponse response =
                resumeAnalysisService.getAnalysisResult(analysisId, jwt);

        return ResponseEntity.ok(response);
    }

}