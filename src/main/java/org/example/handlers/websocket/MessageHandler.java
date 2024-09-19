package org.example.handlers.websocket;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.models.requests.MessageRequest;
import org.example.utils.socketMessenger.SocketEmitter;

public class MessageHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {
  SocketEmitter emitter;

  public MessageHandler() {
    emitter = new SocketEmitter();
  }

  public MessageHandler(SocketEmitter emitter) {
    this.emitter = emitter;
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {

    // TODO eventually try fetching all the players in the game room's socket ids and

    APIGatewayV2WebSocketEvent.RequestContext requestContext = event.getRequestContext();

    if (requestContext == null || requestContext.getConnectionId() == null) {
      return makeWebsocketResponse(
          StatusCodes.BAD_REQUEST, "Invalid event: missing requestContext or connectionId");
    }

    String connectionId = requestContext.getConnectionId();
    try {
      MessageRequest message = (new Gson()).fromJson(event.getBody(), MessageRequest.class);
      emitter.sendMessage(connectionId, message.data());
    } catch (Exception e) {
      LambdaLogger logger = context.getLogger();
      logger.log(e.getMessage());
      return makeWebsocketResponse(StatusCodes.INTERNAL_SERVER_ERROR, "Internal Server Error");
    }

    return makeWebsocketResponse(StatusCodes.OK, "OK");
  }
}
