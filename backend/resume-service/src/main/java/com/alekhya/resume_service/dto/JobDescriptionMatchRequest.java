package com.alekhya.resume_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobDescriptionMatchRequest(

        @NotBlank(message = "Job description cannot be empty")
        @Size(max = 15000, message = "Job description cannot exceed 15000 characters")
        String jobDescription

) {
}