package org.example.entities.player;

import org.example.entities.user.User;

public class PlayerDbService {

  public Player toPlayer(User user, int rating, String connectionId, Boolean isWhite) {
    return Player.builder()
        .playerId(user.getId())
        .username(user.getUsername())
        .remainingTime(1)
        .rating(rating)
        .connectionId(connectionId)
        .isWhite(isWhite)
        .build();
  }
}
