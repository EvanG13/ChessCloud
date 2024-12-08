package org.example.handlers.joinGame;

import static org.example.utils.WebsocketTestUtils.getResponse;
import static org.example.utils.WebsocketTestUtils.makeRequestContext;
import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.List;
import org.example.constants.StatusCodes;
import org.example.entities.game.Game;
import org.example.entities.game.GameUtility;
import org.example.entities.player.Player;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.enums.GameStatus;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;
import org.example.handlers.websocket.joinGame.JoinGameHandler;
import org.example.handlers.websocket.joinGame.JoinGameService;
import org.example.models.requests.JoinGameRequest;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JoinGameHandlerTest {
  public static Game game;
  public static Gson gson;
  public static JoinGameService joinGameService;
  public static String userId;
  public static String userId2;

  public static String connectId;
  public static String connectId2;
  public static String gameId;

  public static TimeControl timeControl;
  public static String username;
  public static String username2;
  public static String email;
  public static String email2;
  public static String password;
  public static String password2;
  public static GameUtility gameUtility;
  public static UserUtility userUtility;
  public static StatsUtility statsUtility;

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
    gameUtility = new GameUtility();
    userUtility = new UserUtility();
    statsUtility = new StatsUtility();
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

  @DisplayName("Player One creates a new game ✅")
  @Test
  @Order(1)
  public void playerOneCreatesNewGame() {
    APIGatewayV2WebSocketResponse response =
        getResponse(
            new JoinGameHandler(joinGameService, socketLogger),
            new JoinGameRequest(userId, timeControl),
            makeRequestContext("joinGame", connectId));

    assertEquals(StatusCodes.CREATED, response.getStatusCode());
    String gameJson = response.getBody();

    gameId = gson.fromJson(gameJson, Game.class).getId();

    try {
      game = gameUtility.getGame(gameId);
    } catch (NotFound e) {
      fail("Game was never created");
    }
  }

  @Test
  @DisplayName("Player Two joins the game")
  @Order(2)
  public void playerTwoJoinsGame() {
    APIGatewayV2WebSocketResponse response =
        getResponse(
            new JoinGameHandler(joinGameService, socketLogger),
            new JoinGameRequest(userId2, timeControl),
            makeRequestContext("joinGame", connectId2));

    assertEquals(StatusCodes.OK, response.getStatusCode());

    Game game;
    try {
      game = gameUtility.getGame(gameId);
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
  @DisplayName("No user matches userId")
  @Order(3)
  public void returnUnauthorized() {
    APIGatewayV2WebSocketResponse response =
        getResponse(
            new JoinGameHandler(joinGameService, socketLogger),
            new JoinGameRequest("nonexistentUserId12321321", timeControl),
            makeRequestContext("joinGame", connectId2));

    assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  @DisplayName("Can only play one game at a time")
  @Order(4)
  public void returnForbidden() {
    APIGatewayV2WebSocketResponse response =
        getResponse(
            new JoinGameHandler(joinGameService, socketLogger),
            new JoinGameRequest(userId2, timeControl),
            makeRequestContext("joinGame", connectId));

    assertEquals(StatusCodes.FORBIDDEN, response.getStatusCode());
  }
}
