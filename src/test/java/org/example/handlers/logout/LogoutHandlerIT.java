package org.example.handlers.logout;

import io.restassured.response.Response;
import org.example.constants.StatusCodes;
import org.example.entities.session.SessionDbService;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.models.requests.SessionRequest;
import org.example.utils.BaseTest;
import org.example.utils.EncryptPassword;
import org.example.utils.IntegrationTestUtils;
import org.junit.jupiter.api.*;

import java.util.Map;

public class LogoutHandlerIT extends BaseTest {
    private static final String endpoint = "/logout";

    private static UserDbService userDbService;
    private static StatsDbService statsDbService;

    private static IntegrationTestUtils<Response> testUtils;

    private static String userId;
    private static String statsId;
    private static String sessionId;

    @BeforeAll
    public static void setUp() {
        testUtils = new IntegrationTestUtils<>();

        userDbService = new UserDbService();
        statsDbService = new StatsDbService();
        SessionDbService sessionDbService = new SessionDbService();

        User user = User.builder()
                .email("logout-it-test@gmail.com")
                .password(EncryptPassword.encrypt("logout"))
                .username("logoutuser")
                .build();

        Stats stats = new Stats(user.getId());

        userId = user.getId();
        statsId = stats.getId();

        userDbService.createUser(user);
        statsDbService.post(stats);
        sessionId = sessionDbService.createSession(new SessionRequest(userId));
    }

    @AfterAll
    public static void tearDown() {
        userDbService.deleteUser(userId);
        statsDbService.deleteStats(statsId);
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
