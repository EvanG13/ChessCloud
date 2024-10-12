package org.example.handlers.websocket.resign;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
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

    String connectionId = event.getRequestContext().getConnectionId();

    ResignRequest request = (new Gson()).fromJson(event.getBody(), ResignRequest.class);
    try {
      ValidateObject.requireNonNull(request);
    } catch (NullPointerException e) {
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Missing argument(s)");
    }

    try {
      resignGameService.resign(request.gameId(), connectionId, messenger);
    } catch (StatusCodeException e) {
      return e.makeWebsocketResponse();
    }

    return makeWebsocketResponse(StatusCodes.OK, "Successfully Resigned");
  }
}
