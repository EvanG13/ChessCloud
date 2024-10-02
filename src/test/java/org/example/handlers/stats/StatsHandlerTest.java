package org.example.handlers.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.restassured.response.Response;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.session.SessionService;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsService;
import org.example.entities.user.User;
import org.example.entities.user.UserService;
import org.example.enums.GameMode;
import org.example.models.requests.SessionRequest;
import org.example.utils.BaseTest;
import org.example.utils.TestUtils;
import org.junit.jupiter.api.*;

public class StatsHandlerTest extends BaseTest {
  private static final String endpoint = "/stats";
  private static Map<String, String> authHeaders;

  private static UserService userService;
  private static StatsService statsService;
  private static SessionService sessionService;

  private static String userId;

  private static TestUtils<JsonObject> testUtils;

  private static Gson gson;

  @BeforeAll
  public static void setUp() {
    userService = new UserService();
    statsService = new StatsService();
    sessionService = new SessionService();

    userId = "test-Id";

    User testUser =
        User.builder()
            .id(userId)
            .email("test@gmail.com")
            .password("1223")
            .username("test-username")
            .build();
    userService.createUser(testUser);
    String sessionToken = sessionService.createSession(new SessionRequest(userId));

    authHeaders =
        Map.of(
            "userid", userId,
            "Authorization", sessionToken);

    Stats testUserStats = new Stats(userId);
    statsService.post(testUserStats);

    testUtils = new TestUtils<>();
    gson = new Gson();
  }

  @AfterAll
  public static void tearDown() {
    userService.deleteUser(userId);
    statsService.deleteStats(userId);
    sessionService.deleteByUserId(userId);
  }

  @DisplayName("No Query")
  @Test
  public void returnNoQuery() {
    Response response = testUtils.get(authHeaders, endpoint, StatusCodes.OK);

    JsonObject expectedStats = gson.fromJson((new Stats(userId)).toJSON(), JsonObject.class);
    JsonObject actualStats = gson.fromJson(response.asPrettyString(), JsonObject.class);

    assertEquals(expectedStats.toString(), actualStats.toString());
  }

  @DisplayName("Query \"gamemode=bullet\" (valid)")
  @Test
  public void returnQueryBullet() {
    Map<String, String> queryStrings = Map.of("gamemode", "bullet");

    Response response = testUtils.get(authHeaders, queryStrings, endpoint, StatusCodes.OK);

    JsonObject expectedStats =
        gson.fromJson((new Stats(userId)).toJSON(GameMode.BULLET), JsonObject.class);
    JsonObject actualStats = gson.fromJson(response.asPrettyString(), JsonObject.class);

    assertEquals(expectedStats.toString(), actualStats.toString());
  }

  @DisplayName("Query \"gamemode=invalidgamemode\" (invalid)")
  @Test
  public void returnQueryInvalidGamemode() {
    Map<String, String> queryStrings = Map.of("gamemode", "invalidgamemode");

    Response response = testUtils.get(authHeaders, queryStrings, endpoint, StatusCodes.BAD_REQUEST);

    assertEquals(
        "Query parameter \"gamemode\" had an invalid value: invalidgamemode",
        response.asPrettyString());
  }

  @DisplayName("Invalid query parameter")
  @Test
  public void returnInvalidQueryParameter() {
    Map<String, String> queryStrings = Map.of("param", "bullet");

    Response response = testUtils.get(authHeaders, queryStrings, endpoint, StatusCodes.BAD_REQUEST);

    assertEquals(
        "Query defined, but query parameter \"gamemode\" was missing", response.asPrettyString());
  }

  @DisplayName("Query \"gamemode=\" (invalid)")
  @Test
  public void returnQueryBlankGamemode() {
    Map<String, String> queryStrings = Map.of("gamemode", "");

    Response response = testUtils.get(authHeaders, queryStrings, endpoint, StatusCodes.BAD_REQUEST);
    assertEquals("Query parameter \"gamemode\" was missing a value", response.asPrettyString());
  }
}
