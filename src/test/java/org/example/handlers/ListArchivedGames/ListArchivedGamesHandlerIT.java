package org.example.handlers.ListArchivedGames;

import static org.example.utils.TestUtils.validGame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameDbService;
import org.example.entities.game.Game;
import org.example.entities.session.SessionDbService;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.enums.ResultReason;
import org.example.enums.TimeControl;
import org.example.models.requests.SessionRequest;
import org.example.models.responses.rest.ListArchivedGamesResponse;
import org.example.utils.BaseTest;
import org.example.utils.IntegrationTestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ListArchivedGamesHandlerIT extends BaseTest {
  final String endpoint = "/archivedGames/{username}";
  private static Map<String, String> authHeaders;
  private static Map<String, String> pathParams;

  private static IntegrationTestUtils<ListArchivedGamesResponse> testUtils;

  private static ArchivedGameDbService archivedGameDbService;

  private static UserDbService userDbService;
  private static SessionDbService sessionDbService;
  private static StatsDbService statsDbService;

  private static String userId;
  private static String gameId;
  private static String gameId2;

  @BeforeAll
  public static void setUp() throws Exception {
    userDbService = new UserDbService();
    sessionDbService = new SessionDbService();
    statsDbService = new StatsDbService();
    archivedGameDbService = ArchivedGameDbService.builder().build();
    testUtils = new IntegrationTestUtils<>();

    User testUser =
        User.builder().email("test1@gmail.com").password("1223").username("test-username1").build();
    userId = testUser.getId();

    User testUserTwo =
        User.builder().email("test2@gmail.com").password("1223").username("test-username2").build();

    userDbService.createUser(testUser);
    String sessionToken = sessionDbService.createSession(new SessionRequest(userId));

    Game one = validGame(TimeControl.BLITZ_5, testUser, testUserTwo);
    gameId = one.getId();
    archivedGameDbService.archiveGame(one, testUser.getUsername(), ResultReason.CHECKMATE);

    Game two = validGame(TimeControl.BULLET_3, testUser, testUserTwo);
    gameId2 = two.getId();
    archivedGameDbService.archiveGame(two, testUserTwo.getUsername(), ResultReason.FORFEIT);

    authHeaders =
        Map.of(
            "userid", userId,
            "Authorization", sessionToken);
    pathParams = Map.of("username", testUser.getUsername());

    Stats testUserStats = new Stats(userId);
    statsDbService.post(testUserStats);
  }

  @AfterAll
  public static void tearDown() {
    userDbService.deleteUser(userId);
    sessionDbService.deleteByUserId(userId);
    statsDbService.deleteStats(userId);
    archivedGameDbService.deleteArchivedGame(gameId);
    archivedGameDbService.deleteArchivedGame(gameId2);
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
    Map<String, String> timeControlFilter =
        Map.of("timeControl", String.valueOf(TimeControl.BLITZ_5));
    Response response =
        testUtils.get(authHeaders, endpoint, pathParams, timeControlFilter, StatusCodes.OK);

    String jsonResponse = response.getBody().asString();
    ListArchivedGamesResponse listArchivedGamesResponse =
        (new Gson()).fromJson(jsonResponse, ListArchivedGamesResponse.class);

    List<ArchivedGame> archivedGames = listArchivedGamesResponse.getArchivedGames();
    assertEquals(1, archivedGames.size());
  }
}
