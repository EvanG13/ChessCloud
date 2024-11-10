package org.example.handlers.websocket.offerDraw;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.player.Player;
import org.example.enums.OfferDrawAction;
import org.example.enums.ResultReason;
import org.example.enums.WebsocketResponseAction;
import org.example.exceptions.BadRequest;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.exceptions.Unauthorized;
import org.example.handlers.websocket.gameOver.GameOverService;
import org.example.models.responses.websocket.SocketResponseBody;
import org.example.models.responses.websocket.draw.CancelDrawMessageData;
import org.example.models.responses.websocket.draw.DenyDrawMessageData;
import org.example.models.responses.websocket.draw.OfferDrawMessageData;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferDrawService {
  @Builder.Default private final GameDbService gameDbService = new GameDbService();
  @Builder.Default private final SocketMessenger messenger = new SocketEmitter();

  private void offerDraw(Game game, String playerOfferingDrawConnectionId) throws BadRequest {
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
    gameDbService.put(game.getId(), game);

    // Send draw offer to other player
    OfferDrawMessageData messageData = new OfferDrawMessageData();
    SocketResponseBody<OfferDrawMessageData> responseBody =
        new SocketResponseBody<>(WebsocketResponseAction.DRAW_OFFER, messageData);
    messenger.sendMessage(opponentConnectionId, responseBody.toJSON());
  }

  private void cancelDraw(Game game, String playerCancelConnectionId) throws BadRequest {
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
    gameDbService.put(game.getId(), game);

    // Inform the other person offer was canceled
    CancelDrawMessageData messageData = new CancelDrawMessageData();
    SocketResponseBody<CancelDrawMessageData> responseBody =
        new SocketResponseBody<>(WebsocketResponseAction.DRAW_CANCEL, messageData);
    messenger.sendMessage(opponentConnectionId, responseBody.toJSON());
  }

  private void denyDraw(Game game, String playerDeniedDrawConnectionId) throws BadRequest {
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
    gameDbService.put(game.getId(), game);

    // Send draw deny response to other player
    DenyDrawMessageData messageData = new DenyDrawMessageData();
    SocketResponseBody<DenyDrawMessageData> responseBody =
        new SocketResponseBody<>(WebsocketResponseAction.DRAW_DENY, messageData);
    messenger.sendMessage(opponentConnectionId, responseBody.toJSON());
  }

  private void acceptDraw(Game game, String playerAcceptDrawConnectionId)
      throws InternalServerError, BadRequest {
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

  public String performDrawAction(OfferDrawAction action, String gameId, String connectionId)
      throws BadRequest, NotFound, InternalServerError, Unauthorized {
    String responseMessage;

    // Check game with ID=gameId exists
    Game game = gameDbService.get(gameId);

    // Check connection ID is part of the game
    if (!game.containsConnectionId(connectionId))
      throw new Unauthorized("Your connection ID is not bound to this game");

    switch (action) {
      case OfferDrawAction.OFFER -> {
        offerDraw(game, connectionId);
        responseMessage = "Successfully offered a draw";
      }
      case OfferDrawAction.CANCEL -> {
        cancelDraw(game, connectionId);
        responseMessage = "Cancelled draw offer";
      }
      case OfferDrawAction.DENY -> {
        denyDraw(game, connectionId);
        responseMessage = "Draw denied";
      }
      case OfferDrawAction.ACCEPT -> {
        acceptDraw(game, connectionId);
        responseMessage = "Draw accepted";
      }
      // Unknown action
      default -> throw new BadRequest("Invalid value for argument 'drawAction'");
    }

    return responseMessage;
  }
}
