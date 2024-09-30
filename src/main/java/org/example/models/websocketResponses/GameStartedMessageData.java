package org.example.models.websocketResponses;

import com.google.gson.annotations.Expose;
import lombok.experimental.SuperBuilder;
import org.example.entities.Game;

@SuperBuilder
public class GameStartedMessageData extends SocketMessageData {
  @Expose Game game;

  public GameStartedMessageData(Game game) {
    super();
    this.game = game;
  }
}
