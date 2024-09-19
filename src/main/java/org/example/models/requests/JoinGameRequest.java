package org.example.models.requests;

import org.example.enums.TimeControl;

public record JoinGameRequest(String action, String userId, TimeControl timeControl) {}
