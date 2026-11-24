package com.alekhya.aijobcopilot.service;

import com.alekhya.aijobcopilot.entity.ApplicationStatus;
import com.alekhya.aijobcopilot.entity.JobApplication;
import com.alekhya.aijobcopilot.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;

    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public JobApplication createJob(JobApplication job, String userEmail) {
        job.setUserEmail(userEmail);
        return repository.save(job);
    }

    public List<JobApplication> getJobsByUserEmail(String userEmail) {
        return repository.findByUserEmail(userEmail);
    }

    public JobApplication getJobByIdAndUser(Long id, String userEmail) {
        return repository.findById(id)
                .filter(job -> userEmail.equals(job.getUserEmail()))
                .orElseThrow(() ->
                        new RuntimeException("Job application not found or unauthorized: " + id));
    }

    public void deleteJobForUser(Long id, String userEmail) {
        JobApplication job = getJobByIdAndUser(id, userEmail);
        repository.delete(job);
    }

    public JobApplication updateJobStatusForUser(
            Long id, ApplicationStatus status, String userEmail) {

        JobApplication job = getJobByIdAndUser(id, userEmail);
        job.setStatus(status);

        return repository.save(job);
    }
}