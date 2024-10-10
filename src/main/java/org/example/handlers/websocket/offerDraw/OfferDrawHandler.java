package org.example.handlers.websocket.offerDraw;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.exceptions.NotFound;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

public class OfferDrawHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final OfferDrawService offerDrawService;
  private final SocketMessenger messenger;

  public OfferDrawHandler() {
    offerDrawService = new OfferDrawService();
    messenger = new SocketEmitter();
  }

  public OfferDrawHandler(OfferDrawService offerDrawService, SocketMessenger messenger) {
    this.offerDrawService = offerDrawService;
    this.messenger = messenger;
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {

    String playerOfferingDrawConnectionId = event.getRequestContext().getConnectionId();

    Map<String, String> pathParams = event.getPathParameters();
    if (pathParams == null) {
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Not path params");
    }

    String gameId = pathParams.get("gameId");
    if (gameId == null) {
      messenger.sendMessage(playerOfferingDrawConnectionId, "Missing gameId from path");
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Missing gameId from path");
    }

    try {
      offerDrawService.offerDraw(gameId, playerOfferingDrawConnectionId);
    } catch (NotFound e) {
      messenger.sendMessage(playerOfferingDrawConnectionId, e.getMessage());
      return e.makeWebsocketResponse();
    }

    return makeWebsocketResponse(StatusCodes.OK, "Successfully offered a draw");
  }
}
