package org.example.models.requests;

public record MessageRequest(String action, String chatMessage, String userId, String username) {}
