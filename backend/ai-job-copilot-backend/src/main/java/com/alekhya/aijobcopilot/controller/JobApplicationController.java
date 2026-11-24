package com.alekhya.aijobcopilot.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.alekhya.aijobcopilot.entity.ApplicationStatus;
import com.alekhya.aijobcopilot.entity.JobApplication;
import com.alekhya.aijobcopilot.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = {
        "http://localhost:5143",
        "http://localhost:5174"
})
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplication createJob(
            @Valid @RequestBody JobApplication job,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return service.createJob(job, userEmail);
    }

    @GetMapping
    public List<JobApplication> getAllJobs(Authentication authentication) {
        String userEmail = authentication.getName();
        return service.getJobsByUserEmail(userEmail);
    }

    @GetMapping("/{id}")
    public JobApplication getJobById(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return service.getJobByIdAndUser(id, userEmail);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        service.deleteJobForUser(id, userEmail);
    }

    @PatchMapping("/{id}/status")
    public JobApplication updateJobStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return service.updateJobStatusForUser(id, status, userEmail);
    }
}
