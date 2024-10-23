package org.example.handlers.websocket.offerDraw;

import java.util.List;
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
import org.example.models.responses.websocket.SocketResponseBody;
import org.example.models.responses.websocket.draw.CancelDrawMessageData;
import org.example.models.responses.websocket.draw.DenyDrawMessageData;
import org.example.models.responses.websocket.draw.OfferDrawMessageData;
import org.example.services.GameOverService;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferDrawService {
  @Builder.Default private final GameDbService gameDbService = new GameDbService();
  @Builder.Default private final SocketMessenger messenger = new SocketEmitter();

  public boolean isValidConnectionId(String gameId, String connectionId) throws NotFound {
    return gameDbService.isConnectionIdInGame(gameId, connectionId);
  }

  public void offerDraw(String gameId, String playerOfferingDrawConnectionId)
      throws NotFound, BadRequest {
    Game game = gameDbService.get(gameId);

    List<Player> players = game.getPlayers();
    Player player1 = players.get(0);
    Player player2 = players.get(1);

    // Get other player
    String opponentConnectionId;
    if (player1.getConnectionId().equals(playerOfferingDrawConnectionId)) {
      if (player2.getWantsDraw()) throw new BadRequest("Opponent already issued a draw offer");

      player1.setWantsDraw(true);
      opponentConnectionId = player2.getConnectionId();
    } else {
      if (player1.getWantsDraw()) throw new BadRequest("Opponent already issued a draw offer");

      player2.setWantsDraw(true);
      opponentConnectionId = player1.getConnectionId();
    }

    // Update game
    gameDbService.put(gameId, game);

    // Send draw offer to other player
    OfferDrawMessageData messageData = new OfferDrawMessageData();
    SocketResponseBody<OfferDrawMessageData> responseBody =
        new SocketResponseBody<>(Action.DRAW_OFFER, messageData);
    messenger.sendMessage(opponentConnectionId, responseBody.toJSON());
  }

  public void cancelDraw(String gameId, String playerCancelConnectionId)
      throws NotFound, BadRequest {
    Game game = gameDbService.get(gameId);

    List<Player> players = game.getPlayers();
    Player player1 = players.get(0);
    Player player2 = players.get(1);

    // Get other player
    Player playerCanceling;
    String opponentConnectionId;
    if (player1.getConnectionId().equals(playerCancelConnectionId)) {
      if (!player1.getWantsDraw()) throw new BadRequest("You didn't make an offer to cancel");

      playerCanceling = player1;
      opponentConnectionId = player2.getConnectionId();
    } else {
      if (!player2.getWantsDraw()) throw new BadRequest("You didn't make an offer to cancel");

      playerCanceling = player2;
      opponentConnectionId = player1.getConnectionId();
    }

    // Update game
    playerCanceling.setWantsDraw(false);
    gameDbService.put(gameId, game);

    // Inform the other person offer was canceled
    CancelDrawMessageData messageData = new CancelDrawMessageData();
    SocketResponseBody<CancelDrawMessageData> responseBody =
        new SocketResponseBody<>(Action.DRAW_CANCEL, messageData);
    messenger.sendMessage(opponentConnectionId, responseBody.toJSON());
  }

  public void denyDraw(String gameId, String playerDeniedDrawConnectionId)
      throws NotFound, InternalServerError, BadRequest {
    Game game = gameDbService.get(gameId);

    List<Player> players = game.getPlayers();
    Player player1 = players.get(0);
    Player player2 = players.get(1);

    // Get other player
    String opponentConnectionId;
    if (player1.getConnectionId().equals(playerDeniedDrawConnectionId)) {
      if (!player2.getWantsDraw()) throw new BadRequest("Opponent has not offered a draw");

      player1.setWantsDraw(false);
      player2.setWantsDraw(false);
      opponentConnectionId = player2.getConnectionId();
    } else {
      if (!player1.getWantsDraw()) throw new BadRequest("Opponent has not offered a draw");

      player2.setWantsDraw(false);
      player1.setWantsDraw(false);
      opponentConnectionId = player1.getConnectionId();
    }

    // Update game
    gameDbService.put(gameId, game);

    // Send draw deny response to other player
    DenyDrawMessageData messageData = new DenyDrawMessageData();
    SocketResponseBody<DenyDrawMessageData> responseBody =
        new SocketResponseBody<>(Action.DRAW_DENY, messageData);
    messenger.sendMessage(opponentConnectionId, responseBody.toJSON());
  }

  public void acceptDraw(String gameId, String playerAcceptDrawConnectionId)
      throws NotFound, InternalServerError, BadRequest {
    Game game = gameDbService.get(gameId);

    List<Player> players = game.getPlayers();
    Player player1 = players.get(0);
    Player player2 = players.get(1);

    if (!player1.getWantsDraw() && !player2.getWantsDraw())
      throw new BadRequest("No draw offer to accept");

    if (player1.getConnectionId().equals(playerAcceptDrawConnectionId)) {
      if (player1.getWantsDraw()) throw new BadRequest("You cannot accept your own draw offer");
    } else {
      if (player2.getWantsDraw()) throw new BadRequest("You cannot accept your own draw offer");
    }

    // Draw offer accepted, initiate game over
    GameOverService service =
        new GameOverService(ResultReason.MUTUAL_DRAW, game, player1.getPlayerId(), messenger);
    service.endGame();
  }
}
