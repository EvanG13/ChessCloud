package org.example.models.responses.websocket;

import com.google.gson.annotations.Expose;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.example.entities.Player;
import org.example.entities.game.Game;

@Getter
@SuperBuilder
public class GameStartedMessageData extends SocketMessageData {
  @Expose Game game;
  @Expose String gameId;
  @Expose List<Player> players;

  public GameStartedMessageData(Game game) {
    super();
    this.game = game;
    this.gameId = game.getId();
    this.players = game.getPlayers();
  }
}
