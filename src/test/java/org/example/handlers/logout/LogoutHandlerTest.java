package org.example.handlers.logout;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGameDbService;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.player.Player;
import org.example.entities.session.Session;
import org.example.entities.session.SessionDbService;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
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

  private static SessionDbService sessionUtility;
  private static GameDbService gameUtility;
  private static StatsDbService statsUtility;
  private static UserDbService usersUtility;
  private static ArchivedGameDbService archivedGameDbService;
  private static LogoutHandler logoutHandler;

  private static final String gameId = "super-extreme-duper-fake-game-id";

  private static final String sessionToken1 = "pretend-session-token1";
  private static final String sessionToken2 = "pretend-session-token2";

  private static final String userId = "pretend-userId";
  private static final String user2id = "pretend-userid-2";

  @BeforeAll
  public static void setUp() {
    sessionUtility = new SessionDbService();
    archivedGameDbService = ArchivedGameDbService.builder().build();
    gameUtility = new GameDbService();
    statsUtility = new StatsDbService();
    usersUtility = new UserDbService();

    LogoutService service =
        LogoutService.builder()
            .gameService(new GameStateService())
            .sessionDbService(sessionUtility)
            .socketMessenger(new SocketSystemLogger())
            .build();

    logoutHandler = new LogoutHandler(service);
  }

  @AfterAll
  public static void tearDown() {
    gameUtility.deleteGame(gameId);

    statsUtility.deleteStats(userId);
    statsUtility.deleteStats(user2id);

    usersUtility.deleteUser(userId);
    usersUtility.deleteUser(user2id);

    archivedGameDbService.deleteArchivedGame(gameId);
  }

  @DisplayName("User can logout 🔀")
  @Test
  @Order(1)
  void userCanLogout() {
    Session userOneSession = Session.builder().id(sessionToken1).userId(userId).build();
    sessionUtility.createSession(userOneSession);

    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withHeaders(
                Map.of(
                    "Authorization", sessionToken1,
                    "userid", userId))
            .build();

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    assertThrows(NotFound.class, () -> sessionUtility.get(sessionToken1));
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

    User user1 = User.builder().id(userId).username("userone").build();
    User user2 = User.builder().id(user2id).username("usertwo").build();
    usersUtility.createUser(user1);
    usersUtility.createUser(user2);

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
    sessionUtility.createSession(userOneSession);

    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withHeaders(
                Map.of(
                    "Authorization", sessionToken2,
                    "userid", userId))
            .build();

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    assertDoesNotThrow(() -> archivedGameDbService.getArchivedGame(gameId));
    assertThrows(NotFound.class, () -> gameUtility.get(gameId));
    assertThrows(NotFound.class, () -> sessionUtility.get(sessionToken2));
  }
}
