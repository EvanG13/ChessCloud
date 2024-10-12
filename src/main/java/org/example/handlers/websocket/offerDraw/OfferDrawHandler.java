package org.example.handlers.websocket.offerDraw;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.exceptions.NotFound;
import org.example.models.requests.OfferDrawRequest;
import org.example.utils.ValidateObject;
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

    OfferDrawRequest request = (new Gson()).fromJson(event.getBody(), OfferDrawRequest.class);
    try {
      ValidateObject.requireNonNull(request);
    } catch (NullPointerException e) {
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Missing argument(s)");
    }

    try {
      offerDrawService.offerDraw(request.gameId(), playerOfferingDrawConnectionId);
    } catch (NotFound e) {
      messenger.sendMessage(playerOfferingDrawConnectionId, e.getMessage());
      return e.makeWebsocketResponse();
    }

    return makeWebsocketResponse(StatusCodes.OK, "Successfully offered a draw");
  }
}
