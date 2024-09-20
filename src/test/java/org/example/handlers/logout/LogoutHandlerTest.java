package org.example.handlers.logout;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.example.constants.StatusCodes;
import org.example.entities.Game;
import org.example.entities.Player;
import org.example.entities.Session;
import org.example.enums.GameStatus;
import org.example.enums.TimeControl;
import org.example.handlers.rest.LogoutHandler;
import org.example.services.GameStateService;
import org.example.services.LogoutService;
import org.example.services.SessionService;
import org.example.utils.MockContext;
import org.example.utils.MongoDBUtility;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogoutHandlerTest {

  private static MongoDBUtility<Session> sessionUtility;
  private static MongoDBUtility<Game> gameUtility;
  private static LogoutHandler logoutHandler;
  private static final String sessionToken = "pretend-session-token";
  private static final String userId = "pretend-userId";
  private static String gameId;

  @BeforeAll
  public static void setUp() {
    sessionUtility = new MongoDBUtility<>("sessions", Session.class);
    sessionUtility.delete(sessionToken);
    Session newSession = Session.builder().id(sessionToken).userId("pretend-userId").build();
    sessionUtility.post(newSession);
    Optional<Session> optionalSession = sessionUtility.get(sessionToken);
    assertFalse(optionalSession.isEmpty());

    gameUtility = new MongoDBUtility<>("games", Game.class);
    Player player = Player.builder().playerId(userId).build();
    Game newGame = new Game(TimeControl.BLITZ_5, player);
    try {
      newGame.setup(Player.builder().playerId("second-player-id").build());
    } catch (Exception e) {
      System.out.println("could not create game -- tests will fail...");
    }
    gameId = newGame.getId();
    gameUtility.post(newGame);
    Optional<Game> optionalGame = gameUtility.get(gameId);
    assertFalse(optionalGame.isEmpty());
    assertEquals(GameStatus.ONGOING, newGame.getGameStatus());
    LogoutService service =
        new LogoutService(new SessionService(sessionUtility), new GameStateService(gameUtility));
    logoutHandler = new LogoutHandler(service);
  }

  @AfterAll
  public static void tearDown() {
    gameUtility.delete(gameId);
    Optional<Session> optionalSession = sessionUtility.get(sessionToken);
    if (optionalSession.isPresent()) {
      sessionUtility.delete(sessionToken);
    }
  }

  @DisplayName("OK 🔀")
  @Test
  @Order(1)
  void returnOk() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", sessionToken);
    headers.put("userId", userId);

    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(headers);

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, new MockContext());
    // TODO: the above line will break-- need to inject a SocketLogger into the logoutHandler
    assertEquals(StatusCodes.OK, response.getStatusCode());
    Optional<Session> optionalSession = sessionUtility.get(sessionToken);
    assertTrue(optionalSession.isEmpty());
  }

  @DisplayName("BadRequest 🔀")
  @Test
  @Order(2)
  void returnBadRequest() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    Optional<Session> optionalSession = sessionUtility.get(sessionToken);
    assertTrue(optionalSession.isEmpty());
  }

  @DisplayName("Forfeits Game 🔀")
  @Test
  @Order(3)
  void returnSuccessAndForfeit() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", sessionToken);
    headers.put("userId", userId);
    // create a session
    Session newSession = Session.builder().id(sessionToken).userId(userId).build();
    sessionUtility.post(newSession);
    Optional<Session> optionalSession = sessionUtility.get(sessionToken);
    assertFalse(optionalSession.isEmpty());

    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(headers);

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, new MockContext());
    optionalSession = sessionUtility.get(sessionToken);
    assertEquals(StatusCodes.OK, response.getStatusCode());
    assertTrue(optionalSession.isEmpty());
    Optional<Game> optionalGame = gameUtility.get(gameId);
    assertTrue(optionalGame.isPresent());
    System.out.println(optionalGame.get().getPlayers().toString());
    assertEquals(GameStatus.FINISHED, optionalGame.get().getGameStatus());
  }
}
