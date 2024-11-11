package org.example.handlers.websocket.disconnect;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import org.example.constants.StatusCodes;
import org.example.entities.connection.Connection;
import org.example.utils.MongoDBUtility;

public class DisconnectHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  MongoDBUtility<Connection> utility;

  public DisconnectHandler() {
    this.utility = new MongoDBUtility<>("connections", Connection.class);
  }

  public DisconnectHandler(MongoDBUtility<Connection> utility) {
    this.utility = utility;
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {
    String connectionId = event.getRequestContext().getConnectionId();

    utility.delete(connectionId);

    return makeWebsocketResponse(StatusCodes.OK, "Successfully disconnected");
  }
}
