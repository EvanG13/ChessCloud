package org.example.handlers.websocket.resign;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.exceptions.StatusCodeException;
import org.example.models.requests.ResignRequest;
import org.example.utils.ValidateObject;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

public class ResignGameHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final ResignGameService resignGameService;
  private final SocketMessenger messenger;

  public ResignGameHandler() {
    resignGameService = new ResignGameService();
    messenger = new SocketEmitter();
  }

  public ResignGameHandler(ResignGameService resignGameService, SocketMessenger messenger) {
    this.resignGameService = resignGameService;
    this.messenger = messenger;
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {

    String connectedId = event.getRequestContext().getConnectionId();

    Map<String, String> pathParams = event.getPathParameters();
    if (pathParams == null) {
      messenger.sendMessage(connectedId, "Not path params");
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Not path params");
    }

    String gameId = pathParams.get("gameId");
    if (gameId == null) {
      messenger.sendMessage(connectedId, "Missing gameId as a path param");
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Missing gameId as a path param");
    }

    ResignRequest request = (new Gson()).fromJson(event.getBody(), ResignRequest.class);
    try {
      ValidateObject.requireNonNull(request);
    } catch (NullPointerException e) {
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Missing argument(s)");
    }

    try {
      resignGameService.resign(gameId, request.playerId(), messenger);
    } catch (StatusCodeException e) {
      return e.makeWebsocketResponse();
    }

    return makeWebsocketResponse(StatusCodes.OK, "Successfully Resigned");
  }
}
