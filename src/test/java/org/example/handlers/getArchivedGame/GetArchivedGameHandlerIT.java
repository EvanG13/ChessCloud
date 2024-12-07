package org.example.handlers.getArchivedGame;

import static org.example.utils.TestUtils.validGame;

import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameUtility;
import org.example.entities.game.Game;
import org.example.entities.session.SessionUtility;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.enums.GameStatus;
import org.example.enums.ResultReason;
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

  private static UserUtility userUtility;
  private static SessionUtility sessionUtility;
  private static ArchivedGameUtility archivedGameUtility;
  private static StatsUtility statsUtility;

  private static String userId;
  private static String gameId;

  private static IntegrationTestUtils<ArchiveGameResponse> testUtils;

  @BeforeAll
  public static void setUp() throws Exception {
    userUtility = new UserUtility();
    sessionUtility = new SessionUtility();
    statsUtility = new StatsUtility();
    archivedGameUtility = new ArchivedGameUtility();

    User testUser =
        User.builder().email("test1@gmail.com").password("1223").username("test-username1").build();
    userId = testUser.getId();
    userUtility.post(testUser);
    String sessionToken = sessionUtility.createSession(new SessionRequest(userId));

    authHeaders =
        Map.of(
            "userid", userId,
            "Authorization", sessionToken);

    Stats testUserStats = new Stats(userId);
    statsUtility.post(testUserStats);

    testUtils = new IntegrationTestUtils<>();
    Game game = validGame(TimeControl.BLITZ_5);
    game.setGameStatus(GameStatus.FINISHED);

    gameId = game.getId();

    archivedGameUtility.archiveGame(game, testUser.getUsername(), ResultReason.CHECKMATE);
  }

  @AfterAll
  public static void tearDown() {
    userUtility.delete(userId);
    sessionUtility.deleteByUserId(userId);
    statsUtility.delete(userId);
    archivedGameUtility.delete(gameId);
  }

  @Test
  public void canGetArchivedGame() {
    Map<String, String> queryParams = Map.of("gameId", gameId);
    testUtils.get(authHeaders, endpoint, queryParams, StatusCodes.OK);
  }
}
