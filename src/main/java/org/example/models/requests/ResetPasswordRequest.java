package org.example.models.requests;

public record ResetPasswordRequest(String token, String email, String newPassword) {}
