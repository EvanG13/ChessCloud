package org.example.entities.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.annotations.GsonExcludeField;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BasePlayer {
  @GsonExcludeField protected String playerId;
  protected String username;
  protected Integer rating;
  protected Boolean isWhite;
}
