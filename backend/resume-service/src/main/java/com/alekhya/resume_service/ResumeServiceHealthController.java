package com.alekhya.resume_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeServiceHealthController {
    @GetMapping("/health")
    public Map<String,String> health(){
        return Map.of(
                "service", "Job Service",
                "status", "UP"
        );
    }

}