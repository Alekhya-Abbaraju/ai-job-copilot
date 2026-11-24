package com.alekhya.resume_service.repository;

import com.alekhya.resume_service.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeAnalysisRepository
        extends JpaRepository<ResumeAnalysis, Long> {

    List<ResumeAnalysis> findAllByResumeIdAndOwnerIdOrderByCreatedAtDesc(
            Long resumeId, Long ownerId);

    Optional<ResumeAnalysis> findByIdAndOwnerId(
            Long id, Long ownerId);
}