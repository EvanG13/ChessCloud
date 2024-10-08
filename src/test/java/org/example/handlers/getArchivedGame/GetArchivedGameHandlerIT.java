package org.example.handlers.getArchivedGame;

import static org.example.utils.TestUtils.validGame;

import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGameDbService;
import org.example.entities.game.Game;
import org.example.entities.session.SessionDbService;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.enums.GameStatus;
import org.example.enums.TimeControl;
import org.example.models.requests.SessionRequest;
import org.example.models.responses.rest.ArchiveGameResponse;
import org.example.utils.BaseTest;
import org.example.utils.IntegrationTestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GetArchivedGameHandlerIT extends BaseTest {
  private static final String endpoint = "/archivedGame/{gameId}";
  private static Map<String, String> authHeaders;

  private static UserDbService userDbService;
  private static SessionDbService sessionDbService;
  private static ArchivedGameDbService archivedGameDbService;
  private static StatsDbService statsDbService;

  private static String userId;
  private static String gameId;

  private static IntegrationTestUtils<ArchiveGameResponse> testUtils;

  @BeforeAll
  public static void setUp() throws Exception {
    userDbService = new UserDbService();
    sessionDbService = new SessionDbService();
    statsDbService = new StatsDbService();
    archivedGameDbService = ArchivedGameDbService.builder().build();

    User testUser =
        User.builder().email("test1@gmail.com").password("1223").username("test-username1").build();
    userId = testUser.getId();
    userDbService.createUser(testUser);
    String sessionToken = sessionDbService.createSession(new SessionRequest(userId));

    authHeaders =
        Map.of(
            "userid", userId,
            "Authorization", sessionToken);

    Stats testUserStats = new Stats(userId);
    statsDbService.post(testUserStats);

    testUtils = new IntegrationTestUtils<>();
    Game game = validGame(TimeControl.BLITZ_5);
    game.setGameStatus(GameStatus.FINISHED);

    gameId = game.getId();
    archivedGameDbService.addFinishedGameToArchive(game);
  }

  @AfterAll
  public static void tearDown() {
    userDbService.deleteUser(userId);
    sessionDbService.deleteByUserId(userId);
    statsDbService.deleteStats(userId);
    archivedGameDbService.deleteArchivedGame(gameId);
  }

  @Test
  public void canGetArchivedGame() {
    Map<String, String> queryParams = Map.of("gameId", gameId);
    testUtils.get(authHeaders, endpoint, queryParams, StatusCodes.OK);
  }
}
