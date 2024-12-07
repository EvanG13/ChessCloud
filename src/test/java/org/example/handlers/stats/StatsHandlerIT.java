package org.example.handlers.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.restassured.response.Response;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.session.SessionUtility;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.enums.GameMode;
import org.example.models.requests.SessionRequest;
import org.example.utils.BaseTest;
import org.example.utils.IntegrationTestUtils;
import org.junit.jupiter.api.*;

@Tag("Integration")
public class StatsHandlerIT extends BaseTest {
  private static final String endpoint = "/stats/{username}";
  private static Map<String, String> authHeaders;
  private static Map<String, String> pathParams;

  private static UserUtility userUtility;
  private static StatsUtility statsUtility;
  private static SessionUtility sessionUtility;

  private static String userId;

  private static IntegrationTestUtils<JsonObject> testUtils;

  private static Gson gson;

  @BeforeAll
  public static void setUp() {
    userUtility = new UserUtility();
    statsUtility = new StatsUtility();
    sessionUtility = new SessionUtility();

    User testUser =
        User.builder().email("test@gmail.com").password("1223").username("test-username").build();

    userId = testUser.getId();
    String username = testUser.getUsername();

    userUtility.post(testUser);
    String sessionToken = sessionUtility.createSession(new SessionRequest(userId));

    authHeaders =
        Map.of(
            "userid", userId,
            "Authorization", sessionToken);

    pathParams = Map.of("username", username);

    Stats testUserStats = new Stats(userId);
    statsUtility.post(testUserStats);

    testUtils = new IntegrationTestUtils<>();
    gson = new Gson();
  }

  @AfterAll
  public static void tearDown() {
    userUtility.delete(userId);
    statsUtility.delete(userId);
    sessionUtility.deleteByUserId(userId);
  }

  @DisplayName("No Query")
  @Test
  public void returnNoQuery() {
    Response response = testUtils.get(authHeaders, endpoint, pathParams, StatusCodes.OK);

    JsonObject expectedStats = gson.fromJson((new Stats(userId)).toJSON(), JsonObject.class);
    JsonObject actualStats = gson.fromJson(response.asPrettyString(), JsonObject.class);

    assertEquals(expectedStats.toString(), actualStats.toString());
  }

  @DisplayName("Query \"timeCategory=bullet\" (valid)")
  @Test
  public void returnQueryBullet() {
    Map<String, String> queryStrings = Map.of("timeCategory", "bullet");

    Response response =
        testUtils.get(authHeaders, endpoint, pathParams, queryStrings, StatusCodes.OK);

    JsonObject expectedStats =
        gson.fromJson((new Stats(userId)).toJSON(GameMode.BULLET), JsonObject.class);
    JsonObject actualStats = gson.fromJson(response.asPrettyString(), JsonObject.class);

    assertEquals(expectedStats.toString(), actualStats.toString());
  }

  @DisplayName("Query \"timeCategory=invalidgamemode\" (invalid)")
  @Test
  public void returnQueryInvalidGameCategory() {
    Map<String, String> queryStrings = Map.of("timeCategory", "invalidgamemode");

    Response response =
        testUtils.get(authHeaders, endpoint, pathParams, queryStrings, StatusCodes.BAD_REQUEST);

    assertEquals(
        "Query parameter \"timeCategory\" had an invalid value: invalidgamemode",
        response.asPrettyString());
  }

  @DisplayName("Invalid query parameter")
  @Test
  public void returnInvalidQueryParameter() {
    Map<String, String> queryStrings = Map.of("param", "bullet");

    Response response =
        testUtils.get(authHeaders, endpoint, pathParams, queryStrings, StatusCodes.BAD_REQUEST);

    assertEquals(
        "Query defined, but query parameter \"timeCategory\" was missing",
        response.asPrettyString());
  }

  @DisplayName("Query \"timeCategory=\" (invalid)")
  @Test
  public void returnQueryBlankGameCategory() {
    Map<String, String> queryStrings = Map.of("timeCategory", "");

    Response response =
        testUtils.get(authHeaders, endpoint, pathParams, queryStrings, StatusCodes.BAD_REQUEST);
    assertEquals("Query parameter \"timeCategory\" was missing a value", response.asPrettyString());
  }
}
