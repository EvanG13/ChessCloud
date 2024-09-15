package org.example.handlers.makeMove;

import static org.example.handlers.Responses.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import org.example.requestRecords.MakeMoveRequest;
import org.example.statusCodes.StatusCodes;

public class MakeMoveHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final MakeMoveService service;

  public MakeMoveHandler(MakeMoveService service) {
    this.service = service;
  }

  public MakeMoveHandler() {
    this.service = new MakeMoveService();
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {

    MakeMoveRequest requestData = (new Gson()).fromJson(event.getBody(), MakeMoveRequest.class);

    if (service.doesGameMatchUser(requestData.gameId(), requestData.connectionId(), requestData.playerId())) {
      return makeWebsocketResponse(StatusCodes.UNAUTHORIZED, )
    }

    String boardState = service.getBoardState(requestData.gameId());
    if (boardState == null) {
      return makeWebsocketResponse(StatusCodes.INTERNAL_SERVER_ERROR, "Game is Missing Game State");
    }


    if (service.makeMove(requestData.move(), boardState, requestData.gameId())) {
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Invalid move");
    }

    return makeWebsocketResponse(StatusCodes.OK, "Move made");
  }
}
