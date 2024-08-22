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
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConnectHandlerTest {

  public static String username;
  public static String connectId;
  public static ConnectionMongoDBUtility utility;

  @BeforeAll
  public static void setUp() {
    username = "test-connection";
    connectId = "fake-connection-id";
    utility = new ConnectionMongoDBUtility();
  }

  @AfterAll
  public static void tearDown() {
    utility.deleteByUsername(username);
  }

  @DisplayName("OK ✅")
  @Test
  @Order(1)
  public void returnSuccess() {
    ConnectHandler connectHandler = new ConnectHandler();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    Map<String, String> queryStrings = new HashMap<>();
    queryStrings.put("username", username);

    event.setQueryStringParameters(queryStrings);

    Context context = new FakeContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId);

    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = connectHandler.handleRequest(event, context);
    assertEquals(response.getStatusCode(), StatusCodes.OK);
  }

  @DisplayName("Conflict ✅")
  @Order(2)
  @Test
  public void returnConflictId() {
    ConnectHandler connectHandler = new ConnectHandler();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    Map<String, String> queryStrings = new HashMap<>();
    queryStrings.put("username", "otherUser");

    event.setQueryStringParameters(queryStrings);

    Context context = new FakeContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId);

    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = connectHandler.handleRequest(event, context);
    assertEquals(response.getStatusCode(), StatusCodes.CONFLICT);
  }

  @DisplayName("Conflict ✅")
  @Order(3)
  @Test
  public void returnConflictUsername() {
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
    assertEquals(response.getStatusCode(), StatusCodes.CONFLICT);
  }
}
