package org.example.models.requests;

public record ResetPasswordRequest(String token, String newPassword) {}
