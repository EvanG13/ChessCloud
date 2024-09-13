package org.example.handlers.connect;

import static org.example.handlers.Responses.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.mongodb.MongoException;
import java.util.Map;
import org.example.requestRecords.ConnectionRequest;
import org.example.statusCodes.StatusCodes;

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
    String username = queryParams.get("username");
    String connectionId = event.getRequestContext().getConnectionId();

    if (service.doesConnectionExistByUsername(username)
        || service.doesConnectionExistById(connectionId)) {
      return makeWebsocketResponse(StatusCodes.CONFLICT, "This connection already exists");
    }

    try {
      service.createConnection(new ConnectionRequest(username, connectionId));
    } catch (MongoException e) {
      LambdaLogger logger = context.getLogger();
      logger.log(e.getMessage());
      throw e;
    }

    return makeWebsocketResponse(StatusCodes.OK, connectionId);
  }
}
