package org.example.models.responses.websocket;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.example.entities.game.Game;
import org.example.entities.player.Player;

@Getter
@SuperBuilder
public class GameStartedMessageData extends SocketMessageData {
  Game game;
  String gameId;
  List<Player> players;

  public GameStartedMessageData(Game game) {
    super();
    this.game = game;
    this.gameId = game.getId();
    this.players = game.getPlayers();
  }
}
