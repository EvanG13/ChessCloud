package org.example.handlers.ListArchivedGames;

import static org.example.utils.TestUtils.validGame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameUtility;
import org.example.entities.game.Game;
import org.example.entities.session.SessionUtility;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.entities.timeControl.TimeControl;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.enums.ResultReason;
import org.example.models.requests.SessionRequest;
import org.example.models.responses.rest.ListArchivedGamesResponse;
import org.example.utils.BaseTest;
import org.example.utils.IntegrationTestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ListArchivedGamesHandlerIT extends BaseTest {
  final String endpoint = "/games/{username}";
  private static Map<String, String> authHeaders;
  private static Map<String, String> pathParams;

  private static IntegrationTestUtils<ListArchivedGamesResponse> testUtils;

  private static ArchivedGameUtility archivedGameUtility;

  private static UserUtility userUtility;
  private static SessionUtility sessionUtility;
  private static StatsUtility statsUtility;

  private static String userId;
  private static String gameId;
  private static String gameId2;

  @BeforeAll
  public static void setUp() throws Exception {
    userUtility = new UserUtility();
    sessionUtility = new SessionUtility();
    statsUtility = new StatsUtility();
    archivedGameUtility = new ArchivedGameUtility();
    testUtils = new IntegrationTestUtils<>();

    User testUser =
        User.builder().email("test1@gmail.com").password("1223").username("test-username1").build();
    userId = testUser.getId();

    User testUserTwo =
        User.builder().email("test2@gmail.com").password("1223").username("test-username2").build();

    userUtility.post(testUser);
    String sessionToken = sessionUtility.createSession(new SessionRequest(userId));

    Game one = validGame(new TimeControl(300, 0), testUser, testUserTwo);
    gameId = one.getId();
    archivedGameUtility.archiveGame(one, testUser.getUsername(), ResultReason.CHECKMATE);

    Game two = validGame(new TimeControl(60, 0), testUser, testUserTwo);
    gameId2 = two.getId();
    archivedGameUtility.archiveGame(two, testUserTwo.getUsername(), ResultReason.FORFEIT);

    authHeaders =
        Map.of(
            "userid", userId,
            "Authorization", sessionToken);
    pathParams = Map.of("username", testUser.getUsername());

    Stats testUserStats = new Stats(userId);
    statsUtility.post(testUserStats);
  }

  @AfterAll
  public static void tearDown() {
    userUtility.delete(userId);
    sessionUtility.deleteByUserId(userId);
    statsUtility.delete(userId);
    archivedGameUtility.delete(gameId);
    archivedGameUtility.delete(gameId2);
  }

  @Test
  public void canGetArchivedGamesWithoutTimeControl() {
    Response response = testUtils.get(authHeaders, endpoint, pathParams, StatusCodes.OK);

    String jsonResponse = response.getBody().asString();
    ListArchivedGamesResponse listArchivedGamesResponse =
        (new Gson()).fromJson(jsonResponse, ListArchivedGamesResponse.class);

    List<ArchivedGame> archivedGames = listArchivedGamesResponse.getArchivedGames();
    assertEquals(2, archivedGames.size());
  }

  @Test
  public void canGetArchivedGamesWithTimeControlFilter() {
    Map<String, String> timeControlFilter = Map.of("gameMode", "blitz");
    Response response =
        testUtils.get(authHeaders, endpoint, pathParams, timeControlFilter, StatusCodes.OK);

    String jsonResponse = response.getBody().asString();
    ListArchivedGamesResponse listArchivedGamesResponse =
        (new Gson()).fromJson(jsonResponse, ListArchivedGamesResponse.class);

    List<ArchivedGame> archivedGames = listArchivedGamesResponse.getArchivedGames();
    assertEquals(1, archivedGames.size());
  }
}
