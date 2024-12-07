package org.example.handlers.websocket.connect;

import com.mongodb.client.model.Filters;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.bson.conversions.Bson;
import org.example.entities.game.Game;
import org.example.entities.game.GameUtility;
import org.example.entities.player.Player;

@AllArgsConstructor
public class ConnectService {
  private final GameUtility gameUtility;

  public ConnectService() {
    this.gameUtility = new GameUtility();
  }

  public void updateConnectionId(String userId, String connectionId) {
    // find out if user is in a game
    Bson filter = Filters.elemMatch("players", Filters.eq("playerId", userId));
    Optional<Game> optionalGame = gameUtility.get(filter);
    if (optionalGame.isEmpty()) {
      return;
    }
    Game game = optionalGame.get();
    List<Player> playerList = game.getPlayers();
    if (playerList.get(0).getPlayerId().equals(userId)) {
      playerList.get(0).setConnectionId(connectionId);
    } else {
      playerList.get(1).setConnectionId(connectionId);
    }
    game.setPlayers(playerList);
    gameUtility.put(game.getId(), game);
  }
}
