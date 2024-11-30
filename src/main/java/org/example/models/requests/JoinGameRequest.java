package org.example.models.requests;

import org.example.enums.TimeControl;

public record JoinGameRequest(String userId, TimeControl timeControl) {}
