package org.example.entities.player;

import java.util.Objects;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.annotations.GsonExcludeField;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public abstract class BasePlayer {
  @GsonExcludeField protected String playerId;
  protected String username;
  protected Integer rating;
  protected Boolean isWhite;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BasePlayer that = (BasePlayer) o;
    return Objects.equals(playerId, that.playerId)
        && Objects.equals(username, that.username)
        && Objects.equals(rating, that.rating)
        && Objects.equals(isWhite, that.isWhite);
  }

  @Override
  public int hashCode() {
    return Objects.hash(playerId, username, rating, isWhite);
  }
}
