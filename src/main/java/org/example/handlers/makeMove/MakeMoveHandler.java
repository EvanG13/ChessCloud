package org.example.handlers.makeMove;

import static org.example.handlers.Responses.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import org.example.requestRecords.MakeMoveRequest;
import org.example.statusCodes.StatusCodes;
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

    String connectionId = event.getRequestContext().getConnectionId();
    if (!service.doesGameMatchUser(requestData.gameId(), connectionId, requestData.playerId())) {
      return makeWebsocketResponse(StatusCodes.UNAUTHORIZED, "user is not in this game.");
    }

    if (!service.isMovingOutOfTurn(requestData.gameId(), connectionId)) {
      return makeWebsocketResponse(StatusCodes.UNAUTHORIZED, "It is not your turn.");
    }

    String boardState = service.getBoardState(requestData.gameId());
    if (boardState == null) {
      return makeWebsocketResponse(StatusCodes.INTERNAL_SERVER_ERROR, "Game is Missing Game State");
    }

    String makeMoveResult = service.makeMove(requestData.move(), boardState, requestData.gameId());
    if (makeMoveResult == "INVALID MOVE") {
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Invalid move: " + requestData.move());
    }
    //TODO update the clock
    String[] connectionIds = service.getConnectionIds(requestData.gameId());
    socketMessenger.sendMessages(connectionIds[0], connectionIds[1], makeMoveResult);
    return makeWebsocketResponse(StatusCodes.OK, makeMoveResult);
  }
}
