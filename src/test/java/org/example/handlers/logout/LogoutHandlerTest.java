package org.example.handlers.logout;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.*;
import org.example.entities.player.Player;
import org.example.entities.session.Session;
import org.example.entities.session.SessionUtility;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;
import org.example.handlers.rest.getGameState.GameStateService;
import org.example.handlers.rest.logout.LogoutHandler;
import org.example.handlers.rest.logout.LogoutService;
import org.example.utils.MockContext;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogoutHandlerTest {

  private static SessionUtility sessionUtility;
  private static GameUtility gameUtility;
  private static StatsUtility statsUtility;
  private static UserUtility userUtility;
  private static ArchivedGameUtility archivedGameUtility;
  private static LogoutHandler logoutHandler;

  private static final String gameId = "super-extreme-duper-fake-game-id";

  private static final String sessionToken1 = "pretend-session-token1";
  private static final String sessionToken2 = "pretend-session-token2";

  private static final String userId = "pretend-userId";
  private static final String user2id = "pretend-userid-2";

  @BeforeAll
  public static void setUp() {
    sessionUtility = new SessionUtility();
    archivedGameUtility = new ArchivedGameUtility();
    gameUtility = new GameUtility();
    statsUtility = new StatsUtility();
    userUtility = new UserUtility();

    LogoutService service =
        LogoutService.builder()
            .gameService(new GameStateService())
            .sessionUtility(sessionUtility)
            .socketMessenger(new SocketSystemLogger())
            .build();

    logoutHandler = new LogoutHandler(service);
  }

  @AfterAll
  public static void tearDown() {
    gameUtility.delete(gameId);

    statsUtility.delete(userId);
    statsUtility.delete(user2id);

    userUtility.delete(userId);
    userUtility.delete(user2id);

    archivedGameUtility.delete(gameId);
  }

  @DisplayName("User can logout 🔀")
  @Test
  @Order(1)
  void userCanLogout() {
    Session userOneSession = Session.builder().id(sessionToken1).userId(userId).build();
    sessionUtility.post(userOneSession);

    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withHeaders(
                Map.of(
                    "Authorization", sessionToken1,
                    "userid", userId))
            .build();

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    assertThrows(NotFound.class, () -> sessionUtility.getSession(sessionToken1));
  }

  @DisplayName("BadRequest - Missing Headers 🔀")
  @Test
  @Order(2)
  void returnBadRequest() {
    APIGatewayV2HTTPResponse response =
        logoutHandler.handleRequest(new APIGatewayV2HTTPEvent(), new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Logout and Forfeit Game 🔀")
  @Test
  @Order(3)
  void successfulLogoutForfeitsGame() {
    Player player = Player.builder().playerId(userId).username("userone").build();

    Game newGame = new Game(TimeControl.BLITZ_5, player);
    newGame.setId(gameId);

    User user1 =
        User.builder()
            .id(userId)
            .username("userone")
            .email("successfulLogoutForfeitsGame1@email.com")
            .build();
    User user2 =
        User.builder()
            .id(user2id)
            .username("usertwo")
            .email("successfulLogoutForfeitsGame2@email.com")
            .build();
    userUtility.post(user1);
    userUtility.post(user2);

    Stats user1Stats = new Stats(userId);
    Stats user2Stats = new Stats(user2id);

    statsUtility.post(user1Stats);
    statsUtility.post(user2Stats);

    try {
      newGame.setup(Player.builder().playerId(user2id).username("usertwo").build());
    } catch (Exception e) {
      fail(e.getMessage());
      return;
    }

    gameUtility.post(newGame);

    Session userOneSession = Session.builder().id(sessionToken2).userId(userId).build();
    sessionUtility.post(userOneSession);

    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withHeaders(
                Map.of(
                    "Authorization", sessionToken2,
                    "userid", userId))
            .build();

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    assertDoesNotThrow(() -> archivedGameUtility.getGame(gameId));
    assertThrows(NotFound.class, () -> gameUtility.getGame(gameId));
    assertThrows(NotFound.class, () -> sessionUtility.getSession(sessionToken2));
  }
}
