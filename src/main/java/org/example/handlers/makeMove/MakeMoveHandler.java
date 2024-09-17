package org.example.handlers.makeMove;

import static org.example.handlers.Responses.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import org.example.requestRecords.MakeMoveRequest;
import org.example.statusCodes.StatusCodes;
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
    if (!service.isUserInGame(requestData.gameId(), connectionId, requestData.playerId())) {
      return makeWebsocketResponse(StatusCodes.UNAUTHORIZED, "User is not in this game.");
    }

    if (service.isMovingOutOfTurn(requestData.gameId(), connectionId)) {
      return makeWebsocketResponse(StatusCodes.UNAUTHORIZED, "It is not your turn.");
    }

    String boardState = service.getBoardState(requestData.gameId());
    if (boardState == null) { // TODO: throw an exception instead of returning null
      return makeWebsocketResponse(StatusCodes.INTERNAL_SERVER_ERROR, "Game is Missing Game State");
    }

    String makeMoveResult = service.makeMove(requestData.move(), boardState, requestData.gameId());
    if (makeMoveResult.equals(
        "INVALID MOVE")) { // TODO: see MakeMoveService; should probably catch exceptions
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Invalid move: " + requestData.move());
    }

    // TODO update the clock
    String[] connectionIds = service.getPlayerConnectionIds(requestData.gameId());
    socketMessenger.sendMessages(connectionIds[0], connectionIds[1], makeMoveResult);
    return makeWebsocketResponse(StatusCodes.OK, makeMoveResult);
  }
}
