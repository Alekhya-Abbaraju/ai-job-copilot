package com.alekhya.resume_service.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public Long getUserId(Jwt jwt) {
        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim == null) {
            throw new IllegalStateException(
                    "JWT does not contain a userId claim. Please log in again."
            );
        }
        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(userIdClaim.toString());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid userId claim in JWT."
            );
        }
    }
    public String getEmail(Jwt jwt) {
        return jwt.getSubject();
    }
}
