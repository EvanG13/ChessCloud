package org.example.handlers.logout;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.example.constants.StatusCodes;
import org.example.entities.*;
import org.example.enums.GameStatus;
import org.example.enums.TimeControl;
import org.example.handlers.rest.LogoutHandler;
import org.example.services.GameStateService;
import org.example.services.LogoutService;
import org.example.services.SessionService;
import org.example.utils.MockContext;
import org.example.utils.MongoDBUtility;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogoutHandlerTest {

  private static MongoDBUtility<Session> sessionUtility;
  private static MongoDBUtility<Game> gameUtility;
  private static MongoDBUtility<Stats> statsUtility;
  private static MongoDBUtility<User> usersUtility;

  private static LogoutHandler logoutHandler;

  private static final String gameId = "super-extreme-duper-fake-game-id";

  private static final String sessionToken1 = "pretend-session-token1";
  private static final String sessionToken2 = "pretend-session-token2";

  private static final String userId = "pretend-userId";
  private static final String user2id = "pretend-userid-2";

  @BeforeAll
  public static void setUp() {
    sessionUtility = new MongoDBUtility<>("sessions", Session.class);

    gameUtility = new MongoDBUtility<>("games", Game.class);
    statsUtility = new MongoDBUtility<>("stats", Stats.class);
    usersUtility = new MongoDBUtility<>("users", User.class);

    LogoutService service =
        LogoutService.builder()
            .gameService(new GameStateService(gameUtility))
            .sessionService(new SessionService(sessionUtility))
            .socketMessenger(new SocketSystemLogger())
            .build();

    logoutHandler = new LogoutHandler(service);
  }

  @AfterAll
  public static void tearDown() {
    gameUtility.delete(gameId);

    statsUtility.delete(userId);
    statsUtility.delete(user2id);

    usersUtility.delete(userId);
    usersUtility.delete(user2id);
  }

  @DisplayName("User can logout 🔀")
  @Test
  @Order(1)
  void userCanLogout() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", sessionToken1);
    headers.put("userid", userId);

    Session userOneSession = Session.builder().id(sessionToken1).userId(userId).build();
    sessionUtility.post(userOneSession);

    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(headers);

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.OK, response.getStatusCode());
    Optional<Session> optionalSession = sessionUtility.get(sessionToken1);
    assertTrue(optionalSession.isEmpty());
  }

  @DisplayName("BadRequest - Missing Headers 🔀")
  @Test
  @Order(2)
  void returnBadRequest() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Logout and Forfeit Game 🔀")
  @Test
  @Order(3)
  void successfulLogoutForfeitsGame() {

    Player player = Player.builder().playerId(userId).build();

    Game newGame = new Game(TimeControl.BLITZ_5, player);
    newGame.setId(gameId);

    User user1 = User.builder().id(userId).build();
    User user2 = User.builder().id(user2id).build();
    usersUtility.post(user1);
    usersUtility.post(user2);

    Stats user1Stats = new Stats(userId);
    Stats user2Stats = new Stats(user2id);

    statsUtility.post(user1Stats);
    statsUtility.post(user2Stats);

    try {
      newGame.setup(Player.builder().playerId(user2id).build());
    } catch (Exception e) {
      assertFalse(false, e.getMessage());
      return;
    }
    gameUtility.post(newGame);

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", sessionToken2);
    headers.put("userid", userId);

    Session userOneSession = Session.builder().id(sessionToken2).userId(userId).build();
    sessionUtility.post(userOneSession);

    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(headers);

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, new MockContext());

    Optional<Session> optionalSession = sessionUtility.get(sessionToken2);
    assertTrue(optionalSession.isEmpty());

    assertEquals(StatusCodes.OK, response.getStatusCode());

    Optional<Game> optionalGame = gameUtility.get(gameId);
    assertTrue(optionalGame.isPresent());

    assertEquals(GameStatus.FINISHED, optionalGame.get().getGameStatus());
  }
}
