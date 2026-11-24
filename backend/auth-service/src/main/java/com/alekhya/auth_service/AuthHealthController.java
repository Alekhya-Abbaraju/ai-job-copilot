package com.alekhya.auth_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthHealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "Auth Service",
                "status", "UP"
        );
    }
}