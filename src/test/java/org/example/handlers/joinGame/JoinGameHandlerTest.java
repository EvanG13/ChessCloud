package org.example.handlers.joinGame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.List;
import java.util.Optional;
import org.example.databases.MongoDBUtility;
import org.example.entities.Game;
import org.example.entities.Player;
import org.example.entities.Stats;
import org.example.entities.User;
import org.example.statusCodes.StatusCodes;
import org.example.utils.Constants;
import org.example.utils.FakeContext;
import org.example.utils.GameStatus;
import org.example.utils.TimeControl;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JoinGameHandlerTest {
  public static Gson gson;
  public static JoinGameService joinGameService;
  public static String userId;
  public static String userId2;

  public static String connectId;
  public static String connectId2;
  public static String gameId;

  public static TimeControl timeControl;
  public static TimeControl timeControl2;
  public static String username;
  public static String username2;
  public static String email;
  public static String email2;
  public static String password;
  public static String password2;
  public static MongoDBUtility<Game> gameUtility;
  public static MongoDBUtility<User> userUtility;
  public static MongoDBUtility<Stats> statsUtility;

  public static SocketSystemLogger socketLogger;

  @BeforeAll
  public static void setUp() {
    gson = new Gson();
    socketLogger = new SocketSystemLogger();
    userId = "test-Id";
    userId2 = "test-Id2";
    connectId = "fake-connection-id";
    connectId2 = "fake-connection-id2";
    username = "test-username";
    username2 = "test-username2";
    email = "test@gmail.com";
    email2 = "test2@gmail.com";
    password = "1223";
    password2 = "123";
    timeControl = TimeControl.BLITZ_5;
    timeControl2 = TimeControl.BULLET_1;
    gameUtility = new MongoDBUtility<>("games", Game.class);
    userUtility = new MongoDBUtility<>("users", User.class);
    statsUtility = new MongoDBUtility<>("stats", Stats.class);
    joinGameService = new JoinGameService(gameUtility, userUtility, statsUtility);
    User testUser =
        User.builder().id(userId).email(email).password(password).username(username).build();
    User testUser2 =
        User.builder().id(userId2).email(email2).password(password2).username(username2).build();
    userUtility.post(testUser);
    userUtility.post(testUser2);
    Stats testUserStats = new Stats(testUser.getId());
    Stats testUserStats2 = new Stats(testUser2.getId());
    statsUtility.post(testUserStats);
    statsUtility.post(testUserStats2);
  }

  @AfterAll
  public static void tearDown() {
    gameUtility.delete(gameId);
    userUtility.delete(userId);
    userUtility.delete(userId2);
    statsUtility.delete(userId);
    statsUtility.delete(userId2);
  }

  @DisplayName("CREATED ✅")
  @Test
  @Order(1)
  public void returnGameCreated() {
    JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new FakeContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);
    event.setBody(
        "{'action' : 'joinGame', 'timeControl': '"
            + timeControl
            + "', 'userId': '"
            + userId
            + "'}");

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);
    assertEquals(StatusCodes.CREATED, response.getStatusCode());
    String gameJson = response.getBody();

    gameId = gson.fromJson(gameJson, Game.class).getId();
    Player newPlayer =
        Player.builder()
            .playerId(userId)
            .connectionId(connectId)
            .username(username)
            .rating(Constants.BASE_RATING) // new player default rating
            .build();
    Game expected = new Game(timeControl, newPlayer);
    expected.setId(
        gameId); // since calling the constructor will autoincrement the id from the last game
    // created.
    Optional<Game> optionalGame = gameUtility.get(gameId);
    assertEquals(optionalGame.isEmpty(), false);
    Game actual = optionalGame.get();
    assertEquals(expected, actual);
  }

  @Test
  @DisplayName("OK")
  @Order(2)
  public void returnGameStarted() {
    JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new FakeContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId2);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);
    event.setBody(
        "{'action' : 'joinGame', 'timeControl': '"
            + timeControl
            + "', 'userId': '"
            + userId2
            + "'}");

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);

    assertEquals(StatusCodes.OK, response.getStatusCode());
    Optional<Game> optionalGame = gameUtility.get(gameId);
    assertEquals(optionalGame.isEmpty(), false);
    Game actual = optionalGame.get();
    List<Player> actualPlayerList = actual.getPlayers();
    assertEquals(2, actualPlayerList.size());
    boolean player1Color = actualPlayerList.get(0).getIsWhite();
    boolean player2Color = actualPlayerList.get(1).getIsWhite();
    assertNotEquals(player1Color, player2Color);
    String activePlayerConnectionId = actual.getActivePlayerConnectionId();
    if (player1Color) {
      assertEquals(connectId, activePlayerConnectionId);
    } else {
      assertEquals(connectId2, activePlayerConnectionId);
    }
    assertEquals(GameStatus.ONGOING, actual.getGameStatus());
  }

  @Test
  @DisplayName("UNAUTHORIZED")
  @Order(3)
  public void returnUnauthorized() {
    JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new FakeContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId2);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);
    event.setBody(
        "{'action' : 'joinGame', 'timeControl': '"
            + timeControl
            + "', 'userId': 'nonexistentUserId12321321"
            + "'}");

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);
    assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  @DisplayName("FORBIDDEN")
  @Order(4)
  public void returnForbidden() {
    JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new FakeContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);
    event.setBody(
        "{'action' : 'joinGame', 'timeControl': '"
            + timeControl
            + "', 'userId': '"
            + userId2
            + "'}");

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);
    System.out.println(response.getBody());
    assertEquals(StatusCodes.FORBIDDEN, response.getStatusCode());
  }
}
