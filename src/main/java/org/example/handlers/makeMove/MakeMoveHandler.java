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
    APIGatewayV2WebSocketEvent.RequestContext requestContext = event.getRequestContext();

    if (requestContext == null || requestContext.getConnectionId() == null) {
      System.err.println("Invalid event: missing requestContext or connectionId");
      return makeWebsocketResponse(
          StatusCodes.BAD_REQUEST, "Invalid event: missing requestContext or connectionId");
    }

    String connectionId = requestContext.getConnectionId();

    MakeMoveRequest requestData = (new Gson()).fromJson(event.getBody(), MakeMoveRequest.class);
    requestData.setConnectionId(connectionId);

    if (service.doesGameMatchUser(requestData.getGameId(), connectionId, requestData.getPlayerId())) {
      return makeWebsocketResponse(StatusCodes.UNAUTHORIZED, )
    }

    String gameState = service.getGameState(requestData.getGameId());
    if (gameState == null) {
      return makeWebsocketResponse(StatusCodes.INTERNAL_SERVER_ERROR, "Game is Missing Game State");
    }

    requestData.setBoardState(gameState);

    if (service.makeMove(requestData))
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Invalid move");

    return makeWebsocketResponse(StatusCodes.OK, "Move made");
  }
}
