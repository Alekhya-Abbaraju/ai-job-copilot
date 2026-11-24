package com.alekhya.resume_service.service;

import com.alekhya.resume_service.entity.Resume;
import com.alekhya.resume_service.repository.ResumeRepository;
import com.alekhya.resume_service.security.CurrentUser;
import com.alekhya.resume_service.storage.FileStorage;
import com.alekhya.resume_service.validation.FileValidationService;
import com.alekhya.resume_service.entity.Resume;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final FileStorage fileStorage;
    private final FileValidationService fileValidationService;
    private final CurrentUser currentUser;
    private final String storageType;
    private final ResumeTextExtractionService textExtractionService;

    public ResumeService(
            ResumeRepository resumeRepository,
            FileStorage fileStorage,
            FileValidationService fileValidationService,
            CurrentUser currentUser,
            ResumeTextExtractionService textExtractionService,
            @Value("${app.resume.storage.type:local}") String storageType) {
        this.resumeRepository = resumeRepository;
        this.fileStorage = fileStorage;
        this.fileValidationService = fileValidationService;
        this.currentUser = currentUser;
        this.storageType = storageType;
        this.textExtractionService = textExtractionService;
    }

    @Transactional
    public Resume upload(MultipartFile file, Jwt jwt) throws IOException {

        fileValidationService.validate(file);
        String extractedText =
                textExtractionService.extractText(file.getInputStream());
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(
                originalFilename.lastIndexOf('.') + 1
        ).toLowerCase(Locale.ROOT);

        String storageKey = UUID.randomUUID() + "." + extension;
        Long ownerId = currentUser.getUserId(jwt);

        String contentType = "pdf".equals(extension)
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

        // Save the file first.
        fileStorage.store(file.getInputStream(), storageKey);

        try {
            Resume resume = new Resume();
            resume.setOwnerId(ownerId);
            resume.setExtractedText(extractedText);
            resume.setOriginalFilename(originalFilename);
            resume.setStorageKey(storageKey);
            resume.setStorageType(storageType);
            resume.setContentType(contentType);
            resume.setFileSize(file.getSize());
            resume.setStatus("UPLOADED");

            return resumeRepository.save(resume);
        } catch (RuntimeException exception) {
            // Avoid leaving a file behind if the database save fails.
            try {
                fileStorage.delete(storageKey);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<Resume> getMyResumes(Jwt jwt) {
        Long ownerId = currentUser.getUserId(jwt);
        return resumeRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }
    @Transactional
    public void deleteResume(Long resumeId, Jwt jwt) throws IOException{
        Resume resume=getMyResume(resumeId,jwt);
        fileStorage.delete(resume.getStorageKey());
        resumeRepository.delete(resume);
    }

    @Transactional(readOnly = true)
    public Resume getMyResume(Long resumeId, Jwt jwt) {
        Long ownerId = currentUser.getUserId(jwt);

        return resumeRepository.findByIdAndOwnerId(resumeId, ownerId)
                .orElseThrow(() ->
                        new ResumeNotFoundException("Resume not found."));
    }

    @Transactional
    public Resume reExtractText(Long resumeId, Jwt jwt) throws IOException {
        Long ownerId = currentUser.getUserId(jwt);
        // Find the resume belonging to the logged-in user.
        Resume resume = resumeRepository
                .findByIdAndOwnerId(resumeId, ownerId)
                .orElseThrow(() ->
                        new ResumeNotFoundException("Resume not found."));
        // Load the previously uploaded file.
        try (InputStream inputStream =
                     fileStorage.load(resume.getStorageKey())) {
            // Extract text from the existing file.
            String extractedText =
                    textExtractionService.extractText(inputStream);
            if (extractedText == null || extractedText.isBlank()) {
                throw new IOException("No readable text found in the resume.");
            }
            // Update the existing resume record.
            resume.setExtractedText(extractedText);

            return resumeRepository.save(resume);
        }
    }

    @Transactional(readOnly = true)
    public ResumeFile getResumeFile(Long resumeId, Jwt jwt) throws IOException {
        // Verify that the resume belongs to the logged-in user.
        Resume resume = getMyResume(resumeId, jwt);
        // Load the original PDF from storage.
        try (InputStream inputStream =
                     fileStorage.load(resume.getStorageKey())) {
            byte[] fileContent = inputStream.readAllBytes();
            return new ResumeFile(
                    fileContent,
                    resume.getOriginalFilename(),
                    resume.getContentType()
            );
        }
    }
    // Holds the original file and its metadata.
    public record ResumeFile(
            byte[] content,
            String filename,
            String contentType
    ) {}
    public Resume getResumeById(Long resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resume not found: " + resumeId
                ));
    }

    public static class ResumeNotFoundException extends RuntimeException {
        public ResumeNotFoundException(String message) {
            super(message);
        }
    }
}