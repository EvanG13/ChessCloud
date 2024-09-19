package org.example.handlers.login;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import java.util.Map;
import org.bson.types.ObjectId;
import org.example.constants.StatusCodes;
import org.example.entities.User;
import org.example.handlers.rest.LoginHandler;
import org.example.models.requests.LoginRequest;
import org.example.models.responses.LoginResponseBody;
import org.example.utils.MockContext;
import org.example.utils.MongoDBUtility;
import org.junit.jupiter.api.*;

public class LoginHandlerITTest {
  private static LoginHandler loginHandler;

  private static Context context;

  private static MongoDBUtility<User> dbUtility;

  private static User newUser;

  private static Gson gson;

  @BeforeAll
  public static void setUp() {

    dbUtility = new MongoDBUtility<>("users", User.class);
    gson = new Gson();
    newUser =
        User.builder()
            .id(new ObjectId().toString())
            .email("it-test@gmail.com")
            .password("$2a$12$MwPTs6UFjy7NAge3HxHwEOTUvX2M6bXpqkHCozjisNTCpcaQ9ZiyC")
            .username("TestUsername")
            .build();

    dbUtility.post(newUser);

    loginHandler = new LoginHandler();

    context = new MockContext();
  }

  @AfterAll
  public static void tearDown() {
    dbUtility.delete(newUser.getId());
  }

  @DisplayName("OK")
  @Test
  public void canLogin() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    LoginRequest loginRequest = new LoginRequest("it-test@gmail.com", "testPassword");
    event.setBody(gson.toJson(loginRequest));

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);

    // The response Body contains the expected fields
    assertTrue(response.getBody().contains("token"));
    assertTrue(response.getBody().contains("user"));

    Map<String, String> headers = response.getHeaders();
    assertEquals(headers.get("Access-Control-Allow-Origin"), "*");
    assertEquals(headers.get("Access-Control-Allow-Methods"), "POST,OPTIONS");
    assertEquals(headers.get("Access-Control-Allow-Headers"), "*");

    LoginResponseBody body = (new Gson()).fromJson(response.getBody(), LoginResponseBody.class);

    User user = body.getUser();

    assertNotNull(user.getId());
    assertEquals(user.getUsername(), "TestUsername");
    assertEquals(user.getEmail(), "it-test@gmail.com");
    assertNull(user.getPassword());

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @DisplayName("Unauthorized \uD83D\uDD25")
  @Test
  public void returnsUnauthorized() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    LoginRequest loginRequest = new LoginRequest("super-fake-email@gmail.com", "testPassword");
    event.setBody(gson.toJson(loginRequest));

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);

    Map<String, String> headers = response.getHeaders();
    assertEquals(headers.get("Access-Control-Allow-Origin"), "*");
    assertEquals(headers.get("Access-Control-Allow-Methods"), "POST,OPTIONS");
    assertEquals(headers.get("Access-Control-Allow-Headers"), "*");

    assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
  }

  @DisplayName("Bad Request - Missing Event \uD83D\uDE1E")
  @Test
  public void canReturnBadRequest() {

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(null, context);

    Map<String, String> headers = response.getHeaders();
    assertEquals(headers.get("Access-Control-Allow-Origin"), "*");
    assertEquals(headers.get("Access-Control-Allow-Methods"), "POST,OPTIONS");
    assertEquals(headers.get("Access-Control-Allow-Headers"), "*");

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Bad Request \uD83D\uDE1E")
  @Test
  public void nullArgumentBadRequest() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    LoginRequest loginRequest = new LoginRequest("super-fake-email@gmail.com", null);
    event.setBody(gson.toJson(loginRequest));

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);

    Map<String, String> headers = response.getHeaders();
    assertEquals(headers.get("Access-Control-Allow-Origin"), "*");
    assertEquals(headers.get("Access-Control-Allow-Methods"), "POST,OPTIONS");
    assertEquals(headers.get("Access-Control-Allow-Headers"), "*");

    assertEquals("Missing argument(s)", response.getBody());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }
}
