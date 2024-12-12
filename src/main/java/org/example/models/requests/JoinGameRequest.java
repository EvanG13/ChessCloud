package org.example.models.requests;

import org.example.entities.timeControl.TimeControl;

public record JoinGameRequest(String userId, TimeControl timeControl) {}
