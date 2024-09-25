package org.example.entities;

import com.google.gson.annotations.Expose;
import java.util.Objects;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.constants.ChessConstants;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Player {
  @Expose private String username;
  private String playerId;
  @Expose private Boolean isWhite;

  @Expose @Builder.Default private Integer rating = ChessConstants.BASE_RATING;
  @Expose private Integer remainingTime;

  private String connectionId;

  @Override
  public String toString() {
    return "Player{"
        + "username='"
        + username
        + '\''
        + ", isWhite="
        + isWhite
        + ", remainingTime="
        + remainingTime
        + ", connectionId='"
        + connectionId
        + '\''
        + ", playerId='"
        + playerId
        + '\''
        + rating
        + "\n"
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Player player = (Player) o;
    return Objects.equals(isWhite, player.isWhite)
        && Objects.equals(remainingTime, player.remainingTime)
        && Objects.equals(username, player.username)
        && Objects.equals(rating, player.rating)
        && Objects.equals(connectionId, player.connectionId);
  }
}
