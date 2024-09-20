package org.example.services;

import com.mongodb.client.model.Filters;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.bson.conversions.Bson;
import org.example.entities.Connection;
import org.example.entities.Game;
import org.example.entities.Player;
import org.example.models.requests.ConnectionRequest;
import org.example.utils.MongoDBUtility;

@AllArgsConstructor
public class ConnectService {
  private final MongoDBUtility<Connection> utility;
  private final MongoDBUtility<Game> gameMongoDBUtility;

  public ConnectService() {
    this.utility = new MongoDBUtility<>("connections", Connection.class);
    this.gameMongoDBUtility = new MongoDBUtility<>("games", Game.class);
  }

  public boolean doesConnectionExistByUsername(String username) {
    Optional<Connection> conn = utility.get(Filters.eq("username", username));

    return conn.isPresent();
  }

  public boolean doesConnectionExistById(String connectionId) {
    Optional<Connection> conn = utility.get(connectionId);

    return conn.isPresent();
  }

  public void createConnection(ConnectionRequest data) {

    Connection newConnection =
        Connection.builder().id(data.connectionId()).username(data.username()).build();

    utility.post(newConnection);
  }

  public void updateConnectionId(String userId, String connectionId) {
    // find out if user is in a game
    Bson filter = Filters.elemMatch("players", Filters.eq("playerId", userId));
    Optional<Game> optionalGame = gameMongoDBUtility.get(filter);
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
    gameMongoDBUtility.put(game.getId(), game);
  }
}
