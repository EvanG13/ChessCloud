package org.example.handlers.defaultHandler;

import static org.example.handlers.Responses.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import org.example.statusCodes.StatusCodes;

public class DefaultHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {
  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {
    // TODO: save this to the connections mongo table
    return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Route: is not supported");
  }
}
