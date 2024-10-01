package org.example.handlers.register;

import static org.junit.jupiter.api.Assertions.*;

import org.example.constants.StatusCodes;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsService;
import org.example.entities.user.User;
import org.example.entities.user.UserService;
import org.example.enums.GameMode;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.models.requests.RegisterRequest;
import org.example.utils.BaseTest;
import org.example.utils.EncryptPassword;
import org.example.utils.TestUtils;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegisterHandlerTest extends BaseTest {
  private static final String endpoint = "/register";

  private static UserService userService;
  private static StatsService statsService;

  private static TestUtils<String> testUtils;

  private static User expectedUser;
  private static Stats expectedStats;

  private static String registeredUserId;
  private static String registeredUserStatsId;

  @BeforeAll
  public static void setUp() {
    userService = new UserService();
    statsService = new StatsService();

    expectedUser =
        User.builder()
            .email("reg-it-test@gmail.com")
            .password(EncryptPassword.encrypt("test"))
            .username("TestUsername")
            .build();

    expectedStats = new Stats(expectedUser.getId());

    testUtils = new TestUtils<>();
  }

  @AfterAll
  public static void tearDown() {
    userService.deleteUser(registeredUserId);
    statsService.deleteStats(registeredUserStatsId);
  }

  @DisplayName("OK 👍")
  @Test
  @Order(1)
  void returnSuccess() {
    RegisterRequest registerRequest =
        new RegisterRequest(expectedUser.getEmail(), expectedUser.getUsername(), "test");

    String response = testUtils.post(registerRequest, endpoint, StatusCodes.OK);
    assertEquals("Successfully registered", response);

    User actualUser;
    try {
      actualUser = userService.getByEmail(expectedUser.getEmail());
    } catch (NotFound e) {
      fail("User was not successfully registered");
      return;
    }
    registeredUserId = actualUser.getId();

    assertEquals(expectedUser.getEmail(), actualUser.getEmail());
    assertEquals(expectedUser.getUsername(), actualUser.getUsername());
    assertTrue(EncryptPassword.verify("test", actualUser.getPassword()));

    Stats actualStats;
    try {
      actualStats = statsService.getStatsByUserID(actualUser.getId());
    } catch (InternalServerError e) {
      fail("Registered User " + actualUser.getEmail() + " is missing stats");
      return;
    }
    registeredUserStatsId = actualStats.getId();

    assertEquals(actualUser.getId(), actualStats.getId());
    assertEquals(
        expectedStats.getGamemodeStats(GameMode.BLITZ),
        actualStats.getGamemodeStats(GameMode.BLITZ));
    assertEquals(
        expectedStats.getGamemodeStats(GameMode.BULLET),
        actualStats.getGamemodeStats(GameMode.BULLET));
    assertEquals(
        expectedStats.getGamemodeStats(GameMode.RAPID),
        actualStats.getGamemodeStats(GameMode.RAPID));
  }

  @DisplayName("Conflict - User already registered by the same email 🔀")
  @Test
  @Order(2)
  void userAlreadyRegisteredByThisEmail() {
    RegisterRequest registerRequest =
        new RegisterRequest(expectedUser.getEmail(), expectedUser.getUsername(), "test");

    String response = testUtils.post(registerRequest, endpoint, StatusCodes.CONFLICT);
    assertEquals("Email already exists", response);
  }

  @DisplayName("Bad Request - Missing Arg 😠")
  @Test
  @Order(3)
  void returnBadRequestMissingArgs() {
    RegisterRequest registerRequest = new RegisterRequest(expectedUser.getEmail(), null, "test");

    String response = testUtils.post(registerRequest, endpoint, StatusCodes.BAD_REQUEST);
    assertEquals("Missing argument(s)", response);
  }
}
