package org.example.handlers.login;

import static org.example.utils.HttpTestUtils.assertResponse;
import static org.example.utils.TestUtils.assertCorsHeaders;
import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import org.bson.types.ObjectId;
import org.example.constants.StatusCodes;
import org.example.entities.session.SessionDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.handlers.rest.login.LoginHandler;
import org.example.models.requests.LoginRequest;
import org.example.models.responses.rest.LoginResponseBody;
import org.example.utils.MockContext;
import org.junit.jupiter.api.*;

public class LoginHandlerTest {
  private static LoginHandler loginHandler;

  private static Context context;

  private static UserDbService userDbService;
  private static SessionDbService sessionDbService;

  private static User newUser;

  private static Gson gson;

  @BeforeAll
  public static void setUp() {
    sessionDbService = new SessionDbService();
    userDbService = new UserDbService();
    gson = new Gson();
    newUser =
        User.builder()
            .id(new ObjectId().toString())
            .email("it-test@gmail.com")
            .password("$2a$12$MwPTs6UFjy7NAge3HxHwEOTUvX2M6bXpqkHCozjisNTCpcaQ9ZiyC")
            .username("TestUsername")
            .build();

    userDbService.createUser(newUser);

    loginHandler = new LoginHandler();

    context = new MockContext();
  }

  @AfterAll
  public static void tearDown() {
    userDbService.deleteUser(newUser.getId());
    sessionDbService.deleteByUserId(newUser.getId());
  }

  @DisplayName("OK")
  @Test
  public void canLogin() {
    APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
        .withBody(gson.toJson(new LoginRequest("it-test@gmail.com", "testPassword")))
        .build();

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);
    assertEquals(StatusCodes.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("token"));
    assertTrue(response.getBody().contains("user"));
    assertCorsHeaders(response.getHeaders());

    LoginResponseBody body =  gson.fromJson(response.getBody(), LoginResponseBody.class);

    User user = body.getUser();
    assertNotNull(user.getId());
    assertEquals(user.getUsername(), "TestUsername");
    assertEquals(user.getEmail(), "it-test@gmail.com");
    assertNull(user.getPassword());
  }

  @DisplayName("Unauthorized \uD83D\uDD25")
  @Test
  public void returnsUnauthorized() {
    String body = gson.toJson(new LoginRequest("super-fake-email@gmail.com", "testPassword"));
    APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
        .withBody(body)
        .build();

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);
    assertCorsHeaders(response.getHeaders());
    assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
  }

  @DisplayName("Bad Request - Missing Event \uD83D\uDE1E")
  @Test
  public void canReturnBadRequest() {
    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(null, context);
    assertCorsHeaders(response.getHeaders());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Bad Request \uD83D\uDE1E")
  @Test
  public void nullArgumentBadRequest() {
    String body = gson.toJson(new LoginRequest("super-fake-email@gmail.com", null));
    APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
        .withBody(body)
        .build();

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);
    assertCorsHeaders(response.getHeaders());
    assertResponse(response, StatusCodes.BAD_REQUEST, "Missing argument(s)");
  }
}
