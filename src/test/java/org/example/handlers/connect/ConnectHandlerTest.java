package org.example.handlers.connect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.example.constants.StatusCodes;
import org.example.entities.game.Game;
import org.example.entities.player.Player;
import org.example.enums.TimeControl;
import org.example.handlers.websocket.connect.ConnectHandler;
import org.example.utils.MockContext;
import org.example.utils.MongoDBUtility;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConnectHandlerTest {

  public static String userId;
  public static String userId2;
  public static String connectId;
  public static String connectId2;
  public static String newConnId;
  public static String gameId;
  public static MongoDBUtility<Game> utility;

  @BeforeAll
  public static void setUp() {
    userId = "test-connection";
    userId2 = "user2";
    connectId = "connection-id";
    connectId2 = "second-connection-id";
    newConnId = "connection-id2";
    utility = new MongoDBUtility<>("games", Game.class);
    Player player = Player.builder().playerId(userId).connectionId(connectId).build();
    Player player2 = Player.builder().playerId(userId2).connectionId(connectId2).build();
    Game newGame = new Game(TimeControl.BLITZ_5, player);
    try {
      newGame.setup(player2);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    gameId = newGame.getId();
    utility.post(newGame);
  }

  @AfterAll
  public static void tearDown() {
    utility.delete(gameId);
  }

  @DisplayName("OK ✅")
  @Test
  @Order(1)
  public void returnSuccess() {
    ConnectHandler connectHandler = new ConnectHandler();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    Map<String, String> queryStrings = new HashMap<>();
    queryStrings.put("userid", userId);

    event.setQueryStringParameters(queryStrings);

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId);

    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = connectHandler.handleRequest(event, context);
    assertEquals(response.getStatusCode(), StatusCodes.OK);
  }

  @DisplayName("Updated ConnectionId 😘😘")
  @Test
  @Order(2)
  public void updateSuccessful() {
    ConnectHandler connectHandler = new ConnectHandler();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    Map<String, String> queryStrings = new HashMap<>();
    queryStrings.put("userid", userId);

    event.setQueryStringParameters(queryStrings);

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(newConnId);

    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = connectHandler.handleRequest(event, context);
    assertEquals(response.getStatusCode(), StatusCodes.OK);

    Optional<Game> oGame = utility.get(gameId);
    assertFalse(oGame.isEmpty());
    Game game = oGame.get();
    assertEquals(newConnId, game.getPlayers().get(0).getConnectionId());
    assertEquals(connectId2, game.getPlayers().get(1).getConnectionId());
  }

  @DisplayName("Updated Other Player's ConnectionId 😘😘")
  @Test
  @Order(3)
  public void updateSecondPlayerSuccessful() {
    ConnectHandler connectHandler = new ConnectHandler();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    Map<String, String> queryStrings = new HashMap<>();
    queryStrings.put("userid", userId2);

    event.setQueryStringParameters(queryStrings);

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId);

    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = connectHandler.handleRequest(event, context);
    assertEquals(response.getStatusCode(), StatusCodes.OK);

    Optional<Game> oGame = utility.get(gameId);
    assertFalse(oGame.isEmpty());
    Game game = oGame.get();
    assertEquals(newConnId, game.getPlayers().get(0).getConnectionId());
    assertEquals(connectId, game.getPlayers().get(1).getConnectionId());
  }
}
