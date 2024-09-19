package org.example.models.requests;

public record MakeMoveRequest(String action, String gameId, String playerId, String move) {}
