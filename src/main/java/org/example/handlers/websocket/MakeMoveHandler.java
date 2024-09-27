package org.example.handlers.websocket;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.entities.Game;
import org.example.exceptions.BadRequest;
import org.example.exceptions.InternalServerError;
import org.example.models.requests.MakeMoveRequest;
import org.example.models.responses.MakeMoveResponseBody;
import org.example.services.MakeMoveService;
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
    String playerId = requestData.playerId();

    if (!service.isUserInGame(requestData.gameId(), connectionId, playerId)) {
      return makeWebsocketResponse(StatusCodes.UNAUTHORIZED, "User is not in this game.");
    }

    String gameId = requestData.gameId();
    String move = requestData.move();

    Game game;
    try {
      game = service.loadGame(gameId);
    } catch (InternalServerError e) {
      return e.makeWebsocketResponse();
    }

    if (!service.isPlayersTurn(game, playerId)) {
      return makeWebsocketResponse(StatusCodes.FORBIDDEN, "It is not your turn.");
    }

    String boardState = game.getGameStateAsFen();
    if (boardState == null) {
      return makeWebsocketResponse(StatusCodes.INTERNAL_SERVER_ERROR, "Game missing FEN");
    }

    String makeMoveResult;
    try {
      makeMoveResult = service.makeMove(move, boardState, gameId);
    } catch (BadRequest e) {
      return e.makeWebsocketResponse();
    }

    // TODO update the clock
    String[] connectionIds = service.getPlayerConnectionIds(game);

    MakeMoveResponseBody res =
        new MakeMoveResponseBody(makeMoveResult, service.getMoveList(gameId));
    socketMessenger.sendMessages(connectionIds[0], connectionIds[1], res.toJSON());
    return makeWebsocketResponse(StatusCodes.OK, res.toJSON());
  }
}
