package org.example.handlers.joinGame;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.List;
import org.example.constants.ChessConstants;
import org.example.constants.StatusCodes;
import org.example.entities.Player;
import org.example.entities.game.Game;
import org.example.entities.game.GameService;
import org.example.entities.stats.Stats;
import org.example.entities.user.User;
import org.example.enums.GameStatus;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;
import org.example.handlers.websocket.JoinGameHandler;
import org.example.models.requests.JoinGameRequest;
import org.example.services.JoinGameService;
import org.example.utils.MockContext;
import org.example.utils.MongoDBUtility;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JoinGameHandlerTest {
  public static Game game;
  public static boolean previousTestPassed = true;
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
  public static GameService gameUtility;
  public static MongoDBUtility<User> userUtility;
  public static MongoDBUtility<Stats> statsUtility;

  public static SocketSystemLogger socketLogger;

  @BeforeEach
  public void isPreviousTestPassed() {
    if (!previousTestPassed) throw new AssertionError("Previous Test Failed");
  }

  @AfterEach
  public void setPreviousTestPassed() {}

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
    gameUtility = new GameService();
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
    gameUtility.deleteGame(gameId);

    userUtility.delete(userId);
    userUtility.delete(userId2);
    statsUtility.delete(userId);
    statsUtility.delete(userId2);
  }

  @DisplayName("CREATED ✅")
  @Test
  @Order(1)
  public void playerOneCreatesNewGame() {
    JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("joinGame");

    JoinGameRequest request = new JoinGameRequest("joinGame", userId, timeControl);
    event.setBody(gson.toJson(request));
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);
    assertEquals(StatusCodes.CREATED, response.getStatusCode());
    String gameJson = response.getBody();

    gameId = gson.fromJson(gameJson, Game.class).getId();
    Player newPlayer =
        Player.builder()
            .playerId(userId)
            .connectionId(connectId)
            .username(username)
            .rating(ChessConstants.BASE_RATING) // new player default rating
            .build();

    Game expected = new Game(timeControl, newPlayer);
    // since calling the constructor will autoincrement the id from the last game
    expected.setId(gameId);
    try {
      game = gameUtility.get(gameId);
    } catch (NotFound e) {
      assertFalse(false, "Game was never created");
      previousTestPassed = false;
      return;
    }

    assertEquals(expected, game);
  }

  @Test
  @DisplayName("OK")
  @Order(2)
  public void playerTwoJoinsGame() {
    assumeTrue(previousTestPassed);
    JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId2);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);
    JoinGameRequest request = new JoinGameRequest("joinGame", userId2, timeControl);
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);
    assertEquals(StatusCodes.OK, response.getStatusCode());

    Game game;
    try {
      game = gameUtility.get(gameId);
    } catch (NotFound e) {
      assertFalse(false, "Game does not exist");
      return;
    }

    assertEquals(GameStatus.ONGOING, game.getGameStatus());

    List<Player> playerList = game.getPlayers();
    assertEquals(2, playerList.size());

    Player player1 = playerList.get(0);
    Player player2 = playerList.get(1);
    assertNotSame(player1.getIsWhite(), player2.getIsWhite());

    if (player1.getIsWhite()) {
      assertEquals(connectId, player1.getConnectionId());
    } else {
      assertEquals(connectId2, player2.getConnectionId());
    }
  }

  @Test
  @DisplayName("UNAUTHORIZED")
  @Order(3)
  public void returnUnauthorized() {
    JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId2);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);

    JoinGameRequest request =
        new JoinGameRequest("joinGame", "nonexistentUserId12321321", timeControl);
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);
    assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  @DisplayName("FORBIDDEN")
  @Order(4)
  public void returnForbidden() {
    JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);

    JoinGameRequest request = new JoinGameRequest("joinGame", userId2, timeControl);
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);
    assertEquals(StatusCodes.FORBIDDEN, response.getStatusCode());
  }
}
