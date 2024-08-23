package org.example.handlers.login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import org.example.databases.UsersMongoDBUtility;
import org.example.entities.User;
import org.example.statusCodes.StatusCodes;
import org.example.utils.EncryptPassword;
import org.example.utils.FakeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LoginHandlerTest {
  private LoginHandler loginHandler;
  private UsersMongoDBUtility dbUtility;

  @BeforeEach
  public void setUp() {
    dbUtility = mock(UsersMongoDBUtility.class);

    LoginService service = new LoginService(dbUtility);

    loginHandler = new LoginHandler(service);
  }

  @DisplayName("OK ✅")
  @Test
  public void returnSuccess() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    event.setBody(
        """
         {
                  "email": "test@gmail.com",
                  "password": "test"
                }""");

    Context context = new FakeContext();

    when(dbUtility.getByEmail(anyString()))
        .thenReturn(
            Optional.of(
                new User(
                    "foo",
                    "nonexistingemail@example.com",
                    EncryptPassword.encrypt("test"),
                    "fake")));
    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);

    // The response Body contains the expected fields
    assertTrue(response.getBody().contains("token"));
    assertTrue(response.getBody().contains("user"));

    Map<String, String> headers = response.getHeaders();
    assertEquals(headers.get("Access-Control-Allow-Origin"), "*");
    assertEquals(headers.get("Access-Control-Allow-Methods"), "POST,OPTIONS");
    assertEquals(
        headers.get("Access-Control-Allow-Headers"),
        "Content-Type,X-Amz-Date,Authorization,X-Api-Key");

    String body = response.getBody();
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
    String userJsonString = jsonObject.get("user").getAsString();
    User user = gson.fromJson(userJsonString, User.class);

    // The user response object contains the correct fields
    assertEquals(user.getId(), "foo");
    assertEquals(user.getUsername(), "fake");
    assertEquals(user.getEmail(), "nonexistingemail@example.com");
    assertNull(user.getPassword());

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @DisplayName("Bad Request \uD83D\uDE1E")
  @Test
  public void returnBadRequest() {
    Context context = new FakeContext();

    when(dbUtility.getByEmail(anyString())).thenReturn(null);
    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(null, context);

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Unauthorized \uD83D\uDD25")
  @Test
  public void returnsUnauthorized() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    event.setBody(
        """
            {
              "email": "nonexistingemail@example.com",
              "password": "notmatching"
            }""");

    Context context = new FakeContext();

    when(dbUtility.getByEmail(anyString()))
        .thenReturn(
            Optional.of(new User("foo", "nonexistingemail@example.com", "password123", "fake")));

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);

    assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
  }
}
