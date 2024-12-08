package org.example.entities.player;

import lombok.experimental.UtilityClass;
import org.example.entities.user.User;

@UtilityClass
public class PlayerUtility {
  public static Player toPlayer(User user, int rating, String connectionId, Boolean isWhite) {
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
