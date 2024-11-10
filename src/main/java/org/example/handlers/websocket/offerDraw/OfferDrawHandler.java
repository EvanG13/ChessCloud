package org.example.handlers.websocket.offerDraw;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.exceptions.*;
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
    // Ensure request is valid
    OfferDrawRequest request = (new Gson()).fromJson(event.getBody(), OfferDrawRequest.class);
    try {
      ValidateObject.requireNonNull(request);
    } catch (NullPointerException e) {
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Missing argument(s)");
    }

    String connectionId = event.getRequestContext().getConnectionId();

    // Switch on Draw action
    String responseMessage;
    try {
      responseMessage = offerDrawService.performDrawAction(request.drawAction(), request.gameId(), connectionId);
    } catch (StatusCodeException e) {
      messenger.sendMessage(connectionId, e.getMessage());
      return e.makeWebsocketResponse();
    }

    return makeWebsocketResponse(StatusCodes.OK, responseMessage);
  }
}
