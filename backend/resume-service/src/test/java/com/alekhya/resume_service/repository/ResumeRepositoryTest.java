
package com.alekhya.resume_service.repository;

import com.alekhya.resume_service.entity.Resume;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(
        showSql = false,
        properties = "spring.jpa.hibernate.ddl-auto=create-drop"
)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Testcontainers
class ResumeRepositoryTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("resume_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add(
                "spring.datasource.driver-class-name",
                postgres::getDriverClassName
        );
    }

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. Save and retrieve a resume

    @Test
    void shouldSaveAndRetrieveResumeSuccessfully() {

        Resume resume = createResume(100L, "resume-1.pdf");

        Resume saved = resumeRepository.saveAndFlush(resume);

        assertThat(saved.getId()).isNotNull();

        Resume retrieved = resumeRepository.findById(saved.getId())
                .orElseThrow();

        assertThat(retrieved.getOwnerId()).isEqualTo(100L);
        assertThat(retrieved.getOriginalFilename())
                .isEqualTo("resume-1.pdf");
        assertThat(retrieved.getStorageKey())
                .isEqualTo("resume-1.pdf");
        assertThat(retrieved.getExtractedText())
                .isEqualTo("Java Spring Boot developer");

        assertThat(retrieved.getCreatedAt()).isNotNull();
        assertThat(retrieved.getUpdatedAt()).isNotNull();
        assertThat(retrieved.getCreatedAt())
                .isEqualTo(retrieved.getUpdatedAt());
    }

    // 2. Verify update timestamp

    @Test
    void shouldUpdateUpdatedAtWhenResumeIsModified() {

        Resume saved = resumeRepository.saveAndFlush(
                createResume(100L, "resume-update.pdf")
        );

        Instant originalCreatedAt = saved.getCreatedAt();
        Instant originalUpdatedAt = saved.getUpdatedAt();

        saved.setStatus("ANALYZED");

        Resume updated = resumeRepository.saveAndFlush(saved);

        assertThat(updated.getCreatedAt())
                .isEqualTo(originalCreatedAt);

        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt())
                .isAfterOrEqualTo(originalUpdatedAt);

        assertThat(updated.getStatus()).isEqualTo("ANALYZED");
    }

    // 3. Find all resumes for a user, newest first

    @Test
    void shouldFindAllResumesForOwnerInDescendingCreatedAtOrder() {

        Resume older = resumeRepository.saveAndFlush(
                createResume(100L, "older.pdf")
        );

        Resume newer = resumeRepository.saveAndFlush(
                createResume(100L, "newer.pdf")
        );

        resumeRepository.saveAndFlush(
                createResume(200L, "another-user.pdf")
        );

        // Set deterministic timestamps to verify sorting.
        setCreatedAt(
                older,
                "2026-01-01T10:00:00Z"
        );

        setCreatedAt(
                newer,
                "2026-02-01T10:00:00Z"
        );

        List<Resume> results =
                resumeRepository.findAllByOwnerIdOrderByCreatedAtDesc(100L);

        assertThat(results).hasSize(2);

        assertThat(results)
                .extracting(Resume::getId)
                .containsExactly(newer.getId(), older.getId());

        assertThat(results)
                .allMatch(resume -> resume.getOwnerId().equals(100L));
    }

    // 4. Return empty list when user has no resumes

    @Test
    void shouldReturnEmptyListWhenOwnerHasNoResumes() {

        List<Resume> results =
                resumeRepository.findAllByOwnerIdOrderByCreatedAtDesc(999L);

        assertThat(results).isEmpty();
    }

    // 5. Find resume only when it belongs to the requested owner

    @Test
    void shouldFindResumeOnlyForItsOwner() {

        Resume saved = resumeRepository.saveAndFlush(
                createResume(100L, "owned-resume.pdf")
        );

        assertThat(
                resumeRepository.findByIdAndOwnerId(saved.getId(), 100L)
        ).isPresent();

        assertThat(
                resumeRepository.findByIdAndOwnerId(saved.getId(), 200L)
        ).isEmpty();

        assertThat(
                resumeRepository.findByIdAndOwnerId(999999L, 100L)
        ).isEmpty();
    }

    // 6. Check resume existence for a specific owner

    @Test
    void shouldCheckResumeExistenceForSpecificOwner() {

        Resume saved = resumeRepository.saveAndFlush(
                createResume(100L, "exists-resume.pdf")
        );

        assertThat(
                resumeRepository.existsByIdAndOwnerId(saved.getId(), 100L)
        ).isTrue();

        assertThat(
                resumeRepository.existsByIdAndOwnerId(saved.getId(), 200L)
        ).isFalse();

        assertThat(
                resumeRepository.existsByIdAndOwnerId(999999L, 100L)
        ).isFalse();
    }

    private Resume createResume(Long ownerId, String storageKey) {

        Resume resume = new Resume();

        resume.setOwnerId(ownerId);
        resume.setOriginalFilename(storageKey);
        resume.setStorageKey(storageKey);
        resume.setStorageType("local");
        resume.setContentType("application/pdf");
        resume.setFileSize(1024L);
        resume.setStatus("UPLOADED");
        resume.setExtractedText("Java Spring Boot developer");

        return resume;
    }

    private void setCreatedAt(Resume resume, String timestamp) {

        jdbcTemplate.update(
                "UPDATE resumes SET created_at = ? WHERE id = ?",
                Timestamp.from(Instant.parse(timestamp)),
                resume.getId()
        );
    }
}
