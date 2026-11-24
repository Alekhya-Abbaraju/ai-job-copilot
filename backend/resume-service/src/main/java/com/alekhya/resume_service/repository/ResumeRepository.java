package com.alekhya.resume_service.repository;

import com.alekhya.resume_service.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<Resume> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}