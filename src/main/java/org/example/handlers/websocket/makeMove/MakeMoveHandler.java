package org.example.handlers.websocket.makeMove;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.google.gson.Gson;
import java.util.Date;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.Game;
import org.example.enums.WebsocketResponseAction;
import org.example.exceptions.*;
import org.example.models.requests.MakeMoveRequest;
import org.example.models.responses.websocket.MakeMoveMessageData;
import org.example.models.responses.websocket.SocketResponseBody;
import org.example.utils.ValidateObject;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

public class MakeMoveHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final MakeMoveService service;
  private final SocketMessenger socketMessenger;

  public MakeMoveHandler(MakeMoveService service, SocketMessenger messenger) {
    this.service = service;
    this.socketMessenger = messenger;
  }

  public MakeMoveHandler() {
    this.service = new MakeMoveService();
    this.socketMessenger = new SocketEmitter();
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {
    LambdaLogger logger = context.getLogger();
    MakeMoveRequest requestData = (new Gson()).fromJson(event.getBody(), MakeMoveRequest.class);
    try {
      ValidateObject.requireNonNull(requestData);
    } catch (NullPointerException e) {
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Missing argument(s)");
    }

    // TODO: maybe check if the game exists? isUserInGame does this, as well as most of the others
    // maybe like, return the game into this class, or store in the service after checking if it
    // exists
    // because nothing should really happen to the game to make it disappear from here until the end
    // of the function

    String connectionId = event.getRequestContext().getConnectionId();
    String playerId = requestData.getPlayerId();

    String gameId = requestData.getGameId();
    String move = requestData.getMove().toLowerCase();

    Game game;
    try {
      game = service.loadGame(gameId, connectionId, playerId);
    } catch (StatusCodeException e) {
      MakeMoveMessageData data =
          MakeMoveMessageData.builder().isSuccess(false).message(e.getMessage()).build();

      SocketResponseBody<MakeMoveMessageData> responseBody =
          new SocketResponseBody<>(WebsocketResponseAction.MOVE_MADE, data);
      socketMessenger.sendMessage(connectionId, responseBody.toJSON());
      logger.log("error loading game", LogLevel.ERROR);

      return e.makeWebsocketResponse();
    }

    if (!service.isPlayersTurn(game, playerId)) {
      MakeMoveMessageData data =
          MakeMoveMessageData.builder().isSuccess(false).message("It is not your turn.").build();

      SocketResponseBody<MakeMoveMessageData> responseBody =
          new SocketResponseBody<>(WebsocketResponseAction.MOVE_MADE, data);
      socketMessenger.sendMessage(connectionId, responseBody.toJSON());
      logger.log("It is not your turn.", LogLevel.ERROR);
      return makeWebsocketResponse(StatusCodes.FORBIDDEN, "It is not your turn.");
    }

    String boardState = game.getGameStateAsFen();
    if (boardState == null) {
      MakeMoveMessageData data =
          MakeMoveMessageData.builder().isSuccess(false).message("Game missing FEN").build();

      SocketResponseBody<MakeMoveMessageData> responseBody =
          new SocketResponseBody<>(WebsocketResponseAction.MOVE_MADE, data);
      socketMessenger.sendMessage(connectionId, responseBody.toJSON());
      logger.log("Game missing fen", LogLevel.FATAL);
      return makeWebsocketResponse(StatusCodes.INTERNAL_SERVER_ERROR, "Game missing FEN");
    }

    Date date = new Date(event.getRequestContext().getRequestTimeEpoch());
    try {
      game = service.makeMove(move, game, date);
    } catch (BadRequest e) {
      MakeMoveMessageData data =
          MakeMoveMessageData.builder().isSuccess(false).message(e.getMessage()).build();

      SocketResponseBody<MakeMoveMessageData> responseBody =
          new SocketResponseBody<>(WebsocketResponseAction.MOVE_MADE, data);
      socketMessenger.sendMessage(connectionId, responseBody.toJSON());
      logger.log(e.getMessage(), LogLevel.ERROR);
      return e.makeWebsocketResponse();
    }

    Map<String, Integer> remainingTimes = service.getRemainingTimes(game);
    String[] connectionIds = service.getPlayerConnectionIds(game);
    MakeMoveMessageData data =
        new MakeMoveMessageData(
            game.getGameStateAsFen(),
            game.getMoveList(),
            game.getIsWhitesTurn(),
            remainingTimes.get("white"),
            remainingTimes.get("black"));
    SocketResponseBody<MakeMoveMessageData> responseBody =
        new SocketResponseBody<>(WebsocketResponseAction.MOVE_MADE, data);
    socketMessenger.sendMessages(connectionIds[0], connectionIds[1], responseBody.toJSON());

    // check if the game is now over due to the last move delivering checkmate / creating draw
    try {
      if (service.handleCheckmate(game, socketMessenger))
        return makeWebsocketResponse(StatusCodes.OK, "checkmate");
    } catch (Exception e) {
      logger.log("something went wrong when trying to check for checkmate: " + e, LogLevel.ERROR);
    }

    try {
      if (service.handleDraw(game, socketMessenger)) {
        return makeWebsocketResponse(StatusCodes.OK, "draw");
      }
    } catch (Exception e) {
      logger.log("something went wrong when trying to check for draw: " + e, LogLevel.ERROR);
    }

    return makeWebsocketResponse(StatusCodes.OK, responseBody.toJSON());
  }
}
