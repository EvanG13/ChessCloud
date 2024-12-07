package org.example.handlers.logout;

import io.restassured.response.Response;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.session.SessionUtility;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.models.requests.SessionRequest;
import org.example.utils.BaseTest;
import org.example.utils.EncryptPassword;
import org.example.utils.IntegrationTestUtils;
import org.junit.jupiter.api.*;

public class LogoutHandlerIT extends BaseTest {
  private static final String endpoint = "/logout";

  private static UserUtility userUtility;
  private static StatsUtility statsUtility;

  private static IntegrationTestUtils<Response> testUtils;

  private static String userId;
  private static String statsId;
  private static String sessionId;

  @BeforeAll
  public static void setUp() {
    testUtils = new IntegrationTestUtils<>();

    userUtility = new UserUtility();
    statsUtility = new StatsUtility();
    SessionUtility sessionUtility = new SessionUtility();

    User user =
        User.builder()
            .email("logout-it-test@gmail.com")
            .password(EncryptPassword.encrypt("logout"))
            .username("logoutuser")
            .build();

    Stats stats = new Stats(user.getId());

    userId = user.getId();
    statsId = stats.getId();

    userUtility.post(user);
    statsUtility.post(stats);
    sessionId = sessionUtility.createSession(new SessionRequest(userId));
  }

  @AfterAll
  public static void tearDown() {
    userUtility.delete(userId);
    statsUtility.delete(statsId);
  }

  @Test
  public void canLogout() {
    Map<String, String> authHeaders = Map.of("userid", userId, "Authorization", sessionId);
    testUtils.post(endpoint, authHeaders, StatusCodes.OK);
  }

  @Test
  public void invalidSessionCredentials() {
    Map<String, String> authHeaders = Map.of("userid", userId, "Authorization", "fake");
    testUtils.post(endpoint, authHeaders, StatusCodes.FORBIDDEN);
  }
}
