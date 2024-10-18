package org.example.handlers.websocket.offerDraw;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.player.Player;
import org.example.enums.Action;
import org.example.enums.ResultReason;
import org.example.exceptions.BadRequest;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.models.responses.websocket.OfferDrawMessageData;
import org.example.models.responses.websocket.SocketResponseBody;
import org.example.services.GameOverService;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferDrawService {
  @Builder.Default private final GameDbService gameDbService = new GameDbService();
  @Builder.Default private final SocketMessenger messenger = new SocketEmitter();

  public boolean isValidConnectionId(String gameId, String connectionId) throws NotFound {
    return gameDbService.isConnectionIdInGame(gameId, connectionId);
  }

  public void offerDraw(String gameId, String playerOfferingDrawConnectionId) throws NotFound {
    Game game = gameDbService.get(gameId);

    List<Player> players = game.getPlayers();
    Player player1 = players.get(0);
    Player player2 = players.get(1);

    // Get other player
    String opponentConnectionId;
    if (player1.getConnectionId().equals(playerOfferingDrawConnectionId)) {
      player1.setWantsDraw(true);
      opponentConnectionId = player2.getConnectionId();
    }
    else {
      player2.setWantsDraw(true);
      opponentConnectionId = player1.getConnectionId();
    }

    // Update game
    gameDbService.put(gameId, game);

    // Send draw offer to other player
    OfferDrawMessageData messageData = new OfferDrawMessageData();
    SocketResponseBody<OfferDrawMessageData> responseBody =
        new SocketResponseBody<>(Action.OFFER_DRAW, messageData);
    messenger.sendMessage(opponentConnectionId, responseBody.toJSON());
  }

  public void cancelDraw(String gameId, String playerCancelConnectionId) throws NotFound {
    Game game = gameDbService.get(gameId);

    Player playerCanceling =
        game.getPlayers().stream()
            .filter(player -> player.getConnectionId().equals(playerCancelConnectionId))
            .findFirst()
            .get();

    // do nothing if no offer exists to cancel
    if (!playerCanceling.getWantsDraw()) return;

    // Update game
    playerCanceling.setWantsDraw(false);
    gameDbService.put(gameId, game);

    // TODO: inform the other person it was canceled
  }

  public void denyDraw(String gameId, String playerDeniedDrawConnectionId) throws NotFound, InternalServerError, BadRequest {
    Game game = gameDbService.get(gameId);

    List<Player> players = game.getPlayers();
    Player player1 = players.get(0);
    Player player2 = players.get(1);

    // Get other player
    String opponentConnectionId;
    if (player1.getConnectionId().equals(playerDeniedDrawConnectionId)) {
      if (!player2.getWantsDraw())
        throw new BadRequest("No offer to deny"); // maybe check if they have a current offer and cancel it

      player1.setWantsDraw(false);
      player2.setWantsDraw(false);
      opponentConnectionId = player2.getConnectionId();
    }
    else {
      if (!player1.getWantsDraw())
        throw new BadRequest("No offer to deny"); // maybe check if they have a current offer and cancel it

      player2.setWantsDraw(false);
      player1.setWantsDraw(false);
      opponentConnectionId = player1.getConnectionId();
    }

    // Update game
    gameDbService.put(gameId, game);

    // Send draw deny response to other player
    // TODO: tell person that offered that their offer was rejected
    OfferDrawMessageData messageData = new OfferDrawMessageData();
    SocketResponseBody<OfferDrawMessageData> responseBody =
        new SocketResponseBody<>(Action.OFFER_DRAW, messageData);
    messenger.sendMessage(opponentConnectionId, responseBody.toJSON());
  }

  public void acceptDraw(String gameId, String playerAcceptDrawConnectionId) throws NotFound, InternalServerError, BadRequest {
    Game game = gameDbService.get(gameId);

    Player playerAccepted =
        game.getPlayers().stream()
            .filter(player -> player.getConnectionId().equals(playerAcceptDrawConnectionId))
            .findFirst()
            .get();

    playerAccepted.setWantsDraw(true);

    if (!game.getPlayers().getFirst().getWantsDraw() || !game.getPlayers().getLast().getWantsDraw())
      throw new BadRequest("You are the only player to want to draw!");

    GameOverService service = new GameOverService(ResultReason.MUTUAL_DRAW, game, playerAccepted.getPlayerId(), messenger);
    service.endGame();
  }
}
