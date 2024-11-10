package org.example.handlers.websocket.connect;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.Map;
import org.example.constants.StatusCodes;

public class ConnectHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final ConnectService service;

  public ConnectHandler() {
    this.service = new ConnectService();
  }

  public ConnectHandler(ConnectService service) {
    this.service = service;
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {

    Map<String, String> queryParams = event.getQueryStringParameters();
    String userId = queryParams.get("userid");
    String connectionId = event.getRequestContext().getConnectionId();

    service.updateConnectionId(userId, connectionId);

    return makeWebsocketResponse(StatusCodes.OK, connectionId);
  }
}
