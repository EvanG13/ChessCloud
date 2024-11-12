package org.example.entities.player;

import org.example.entities.user.User;
import org.example.utils.MongoDBUtility;

public class PlayerDbService {

  public Player toPlayer(User user, String connectionId, Boolean isWhite) {
    return Player.builder()
        .playerId(user.getId())
        .username(user.getUsername())
        .remainingTime(1)
        .connectionId(connectionId)
        .isWhite(isWhite)
        .build();
  }
}
