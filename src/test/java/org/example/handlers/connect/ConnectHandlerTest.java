package org.example.handlers.connect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.example.databases.ConnectionMongoDBUtility;
import org.example.statusCodes.StatusCodes;
import org.example.utils.FakeContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConnectHandlerTest {

  public static String username;
  public static ConnectionMongoDBUtility utility;

  @BeforeAll
  public static void setUp() {
    username = "test-connection";
    utility = new ConnectionMongoDBUtility();
  }

  @AfterAll
  public static void tearDown() {
    utility.deleteByUsername(username);
  }

  @DisplayName("OK ✅")
  @Test
  public void returnSuccess() {
    ConnectHandler connectHandler = new ConnectHandler();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    Map<String, String> queryStrings = new HashMap<>();
    queryStrings.put("username", username);

    event.setQueryStringParameters(queryStrings);

    Context context = new FakeContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    String randomConnectionId = UUID.randomUUID().toString();

    requestContext.setConnectionId(randomConnectionId);

    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = connectHandler.handleRequest(event, context);
    assertEquals(response.getStatusCode(), StatusCodes.OK);
  }
}
