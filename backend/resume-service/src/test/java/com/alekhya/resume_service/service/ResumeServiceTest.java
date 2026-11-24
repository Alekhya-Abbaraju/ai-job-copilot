
package com.alekhya.resume_service.service;

import com.alekhya.resume_service.entity.Resume;
import com.alekhya.resume_service.repository.ResumeRepository;
import com.alekhya.resume_service.security.CurrentUser;
import com.alekhya.resume_service.storage.FileStorage;
import com.alekhya.resume_service.validation.FileValidationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private FileStorage fileStorage;

    @Mock
    private FileValidationService fileValidationService;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private ResumeTextExtractionService textExtractionService;

    @Mock
    private Jwt jwt;

    private ResumeService resumeService;

    private final Long ownerId = 100L;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeService(
                resumeRepository,
                fileStorage,
                fileValidationService,
                currentUser,
                textExtractionService,
                "local"
        );

    }
    private void mockCurrentUser() {
        when(currentUser.getUserId(jwt)).thenReturn(ownerId);
    }

    private Resume createResume() {
        Resume resume = new Resume();
        resume.setOwnerId(ownerId);
        resume.setOriginalFilename("resume.pdf");
        resume.setStorageKey("resume-key.pdf");
        resume.setStorageType("local");
        resume.setContentType("application/pdf");
        resume.setExtractedText("Java Spring Boot Developer");
        resume.setFileSize(1024L);
        resume.setStatus("UPLOADED");
        return resume;
    }

    // ---------------- UPLOAD TESTS ----------------

    @Test
    void shouldUploadResumeSuccessfully() throws Exception {
        mockCurrentUser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "sample pdf content".getBytes()
        );

        when(textExtractionService.extractText(any(InputStream.class)))
                .thenReturn("Java Spring Boot Developer");

        when(resumeRepository.save(any(Resume.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Resume result = resumeService.upload(file, jwt);

        assertNotNull(result);
        assertEquals(ownerId, result.getOwnerId());
        assertEquals("resume.pdf", result.getOriginalFilename());
        assertEquals("application/pdf", result.getContentType());
        assertEquals("Java Spring Boot Developer", result.getExtractedText());
        assertEquals("local", result.getStorageType());
        assertEquals("UPLOADED", result.getStatus());
        assertEquals(18L, result.getFileSize());

        verify(fileValidationService).validate(file);
        verify(textExtractionService).extractText(any(InputStream.class));
        verify(fileStorage).store(any(InputStream.class), anyString());
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void shouldThrowExceptionWhenFileValidationFails() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                "text/plain",
                "invalid content".getBytes()
        );

        doThrow(new IllegalArgumentException("Invalid file type"))
                .when(fileValidationService).validate(file);

        assertThrows(
                IllegalArgumentException.class,
                () -> resumeService.upload(file, jwt)
        );

        verify(fileValidationService).validate(file);
        verifyNoInteractions(textExtractionService, fileStorage, resumeRepository);
    }

    @Test
    void shouldThrowIOExceptionWhenTextExtractionFails() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "sample content".getBytes()
        );

        when(textExtractionService.extractText(any(InputStream.class)))
                .thenThrow(new IOException("Extraction failed"));

        assertThrows(
                IOException.class,
                () -> resumeService.upload(file, jwt)
        );

        verify(fileValidationService).validate(file);
        verify(resumeRepository, never()).save(any());
        verify(fileStorage, never()).store(any(), anyString());
    }

    @Test
    void shouldDeleteStoredFileWhenDatabaseSaveFails() throws Exception {
        mockCurrentUser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "sample content".getBytes()
        );

        when(textExtractionService.extractText(any(InputStream.class)))
                .thenReturn("Java Developer");

        when(resumeRepository.save(any(Resume.class)))
                .thenThrow(new RuntimeException("Database failure"));

        assertThrows(
                RuntimeException.class,
                () -> resumeService.upload(file, jwt)
        );

        verify(fileStorage).store(any(InputStream.class), anyString());
        verify(fileStorage).delete(anyString());
    }

    // ---------------- GET RESUMES TESTS ----------------

    @Test
    void shouldReturnAllResumesForCurrentUser() {
        mockCurrentUser();
        List<Resume> resumes = List.of(
                createResume(),
                createResume()
        );

        when(resumeRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId))
                .thenReturn(resumes);

        List<Resume> result = resumeService.getMyResumes(jwt);

        assertEquals(2, result.size());

        verify(currentUser).getUserId(jwt);
        verify(resumeRepository)
                .findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoResumes() {
        mockCurrentUser();
        when(resumeRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId))
                .thenReturn(List.of());

        List<Resume> result = resumeService.getMyResumes(jwt);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---------------- GET RESUME BY ID TESTS ----------------

    @Test
    void shouldReturnResumeWhenItBelongsToCurrentUser() {
        mockCurrentUser();
        Resume resume = createResume();

        when(resumeRepository.findByIdAndOwnerId(1L, ownerId))
                .thenReturn(Optional.of(resume));

        Resume result = resumeService.getMyResume(1L, jwt);

        assertNotNull(result);
        assertEquals("resume.pdf", result.getOriginalFilename());

        verify(resumeRepository).findByIdAndOwnerId(1L, ownerId);
    }

    @Test
    void shouldThrowExceptionWhenResumeDoesNotExistOrBelongToUser() {
        mockCurrentUser();
        when(resumeRepository.findByIdAndOwnerId(1L, ownerId))
                .thenReturn(Optional.empty());

        ResumeService.ResumeNotFoundException exception = assertThrows(
                ResumeService.ResumeNotFoundException.class,
                () -> resumeService.getMyResume(1L, jwt)
        );

        assertEquals("Resume not found.", exception.getMessage());
    }

    // ---------------- DELETE RESUME TESTS ----------------

    @Test
    void shouldDeleteResumeSuccessfully() throws Exception {
        mockCurrentUser();
        Resume resume = createResume();

        when(resumeRepository.findByIdAndOwnerId(1L, ownerId))
                .thenReturn(Optional.of(resume));

        resumeService.deleteResume(1L, jwt);

        verify(fileStorage).delete("resume-key.pdf");
        verify(resumeRepository).delete(resume);
    }

    @Test
    void shouldNotDeleteFileWhenResumeDoesNotExist() throws Exception {
        mockCurrentUser();
        when(resumeRepository.findByIdAndOwnerId(1L, ownerId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResumeService.ResumeNotFoundException.class,
                () -> resumeService.deleteResume(1L, jwt)
        );

        verify(fileStorage, never()).delete(anyString());
        verify(resumeRepository, never()).delete(any(Resume.class));
    }

    // ---------------- RE-EXTRACT TEXT TESTS ----------------

    @Test
    void shouldReExtractResumeTextSuccessfully() throws Exception {
        mockCurrentUser();
        Resume resume = createResume();

        when(resumeRepository.findByIdAndOwnerId(1L, ownerId))
                .thenReturn(Optional.of(resume));

        when(fileStorage.load("resume-key.pdf"))
                .thenReturn(new ByteArrayInputStream("pdf content".getBytes()));

        when(textExtractionService.extractText(any(InputStream.class)))
                .thenReturn("Updated Java Spring Boot experience");

        when(resumeRepository.save(resume)).thenReturn(resume);

        Resume result = resumeService.reExtractText(1L, jwt);

        assertEquals(
                "Updated Java Spring Boot experience",
                result.getExtractedText()
        );

        verify(fileStorage).load("resume-key.pdf");
        verify(resumeRepository).save(resume);
    }

    @Test
    void shouldThrowIOExceptionWhenReExtractedTextIsBlank() throws Exception {
        mockCurrentUser();
        Resume resume = createResume();

        when(resumeRepository.findByIdAndOwnerId(1L, ownerId))
                .thenReturn(Optional.of(resume));

        when(fileStorage.load("resume-key.pdf"))
                .thenReturn(new ByteArrayInputStream("pdf content".getBytes()));

        when(textExtractionService.extractText(any(InputStream.class)))
                .thenReturn("   ");

        assertThrows(
                IOException.class,
                () -> resumeService.reExtractText(1L, jwt)
        );

        verify(resumeRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenReExtractingNonexistentResume() throws Exception{
        mockCurrentUser();
        when(resumeRepository.findByIdAndOwnerId(1L, ownerId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResumeService.ResumeNotFoundException.class,
                () -> resumeService.reExtractText(1L, jwt)
        );

        verify(fileStorage, never()).load(any(String.class));
    }

    // ---------------- FILE PREVIEW TESTS ----------------

    @Test
    void shouldReturnResumeFileSuccessfully() throws Exception {
        mockCurrentUser();
        Resume resume = createResume();

        byte[] expectedContent = "sample pdf content".getBytes();

        when(resumeRepository.findByIdAndOwnerId(1L, ownerId))
                .thenReturn(Optional.of(resume));

        when(fileStorage.load("resume-key.pdf"))
                .thenReturn(new ByteArrayInputStream(expectedContent));

        ResumeService.ResumeFile result =
                resumeService.getResumeFile(1L, jwt);

        assertArrayEquals(expectedContent, result.content());
        assertEquals("resume.pdf", result.filename());
        assertEquals("application/pdf", result.contentType());

        verify(fileStorage).load("resume-key.pdf");
    }

    @Test
    void shouldThrowExceptionWhenPreviewingNonexistentResume() throws Exception {
        mockCurrentUser();
        when(resumeRepository.findByIdAndOwnerId(1L, ownerId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResumeService.ResumeNotFoundException.class,
                () -> resumeService.getResumeFile(1L, jwt)
        );

        verify(fileStorage, never()).load(anyString());
    }
}
