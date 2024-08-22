package org.example.handlers.disconnect;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.Optional;
import org.example.databases.ConnectionMongoDBUtility;
import org.example.entities.Connection;
import org.example.requestRecords.ConnectionRequest;
import org.example.statusCodes.StatusCodes;
import org.example.utils.FakeContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DisconnectHandlerTest {
  public static String username;
  public static String id;
  public static ConnectionMongoDBUtility utility;

  @BeforeAll
  public static void setUp() {
    username = "foo-username";
    id = "connection-id";

    utility = new ConnectionMongoDBUtility();
    utility.post(new ConnectionRequest(username, id));
  }

  @DisplayName("OK ✅")
  @Test
  public void returnSuccess() {
    Optional<Connection> connection = utility.getByUsername(username);
    assertTrue(connection.isPresent());

    assertEquals(connection.get().toString(), id + " " + username);
    DisconnectHandler disconnectHandler = new DisconnectHandler();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new FakeContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(id);

    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = disconnectHandler.handleRequest(event, context);
    assertEquals(response.getStatusCode(), StatusCodes.OK);

    Optional<Connection> previousRecord = utility.getByUsername(username);
    assertTrue(previousRecord.isEmpty());
  }
}
