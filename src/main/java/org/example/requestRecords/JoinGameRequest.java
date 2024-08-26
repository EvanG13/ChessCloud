package org.example.requestRecords;

import org.example.utils.TimeControl;

public record JoinGameRequest(String userId, TimeControl timeControl) {}
// if no gameId is supplied then use our matchmaking algorithm to find a good candidate game for
// them if it exists
// otherwise, create a new game that is pending with them
