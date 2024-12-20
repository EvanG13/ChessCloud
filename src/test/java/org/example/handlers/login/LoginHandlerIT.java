package org.example.handlers.login;

import static org.junit.jupiter.api.Assertions.*;

import org.example.constants.StatusCodes;
import org.example.entities.session.SessionUtility;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.models.requests.LoginRequest;
import org.example.models.responses.rest.LoginResponseBody;
import org.example.utils.BaseTest;
import org.example.utils.IntegrationTestUtils;
import org.junit.jupiter.api.*;

@Tag("Integration")
public class LoginHandlerIT extends BaseTest {

  private static final String endpoint = "/login";

  private static IntegrationTestUtils<LoginResponseBody> testUtils;

  private static UserUtility userUtility;
  private static SessionUtility sessionUtility;

  private static User expectedUser;

  @BeforeAll
  public static void setUp() {
    sessionUtility = new SessionUtility();
    userUtility = new UserUtility();
    expectedUser =
        User.builder()
            .email("it-test@gmail.com")
            .password("$2a$12$MwPTs6UFjy7NAge3HxHwEOTUvX2M6bXpqkHCozjisNTCpcaQ9ZiyC")
            .username("TestUsername")
            .build();

    userUtility.post(expectedUser);

    testUtils = new IntegrationTestUtils<>();
  }

  @AfterAll
  public static void tearDown() {
    userUtility.delete(expectedUser.getId());
    sessionUtility.deleteByUserId(expectedUser.getId());
  }

  @DisplayName("OK")
  @Test
  public void canLogin() {
    LoginRequest loginRequest = new LoginRequest("it-test@gmail.com", "testPassword");

    LoginResponseBody response =
        testUtils.post(loginRequest, LoginResponseBody.class, endpoint, StatusCodes.OK);

    assertNotNull(response.getToken());

    User actualUser = response.getUser();
    assertNotNull(actualUser);
    assertEquals(actualUser.getId(), expectedUser.getId());
    assertEquals(actualUser.getEmail(), expectedUser.getEmail());
    assertEquals(actualUser.getUsername(), expectedUser.getUsername());
  }

  @DisplayName("Unauthorized \uD83D\uDD25")
  @Test
  public void returnsUnauthorizedOnInvalidEmailOrPassword() {

    LoginRequest loginRequest = new LoginRequest("super-fake-email@gmail.com", "testPassword");

    String response = testUtils.post(loginRequest, endpoint, StatusCodes.UNAUTHORIZED);

    assertEquals(response, "Email or Password is incorrect");
  }

  @DisplayName("Bad Request - Missing Argument \uD83D\uDE1E")
  @Test
  public void nullArgumentBadRequest() {
    LoginRequest loginRequest = new LoginRequest("super-fake-email@gmail.com", null);

    String response = testUtils.post(loginRequest, endpoint, StatusCodes.BAD_REQUEST);

    assertEquals("Missing argument(s)", response);
  }
}
