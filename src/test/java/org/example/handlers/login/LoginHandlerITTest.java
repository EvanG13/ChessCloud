package org.example.handlers.login;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.entities.User;
import org.example.handlers.TestContext;
import org.example.statusCodes.StatusCodes;
import org.junit.jupiter.api.*;

@Tag("Integration")
public class LoginHandlerITTest {
  private static LoginHandler loginHandler;

  private static Context context;

  @BeforeAll
  public static void setUp() {

    loginHandler = new LoginHandler();

    context = new TestContext();
  }

  @AfterAll
  public static void tearDown() {}

  @DisplayName("OK")
  @Test
  public void canLogin() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    // * important the email and password below are valid, and match the user created within
    // dynamo.tf.
    // please DO NOT change this username and password unless you have the intention to update
    // dynamo.tf
    // or uploading the credentials a different way
    event.setBody(
        """
             {
                      "email": "it-test@gmail.com",
                      "password": "testPassword"
                    }""");

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);

    // The response Body contains the expected fields
    assertTrue(response.getBody().contains("jwt"));
    assertTrue(response.getBody().contains("user"));

    String body = response.getBody();
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
    String userJsonString = jsonObject.get("user").getAsString();
    User user = gson.fromJson(userJsonString, User.class);

    assertNotNull(user.getId());
    assertEquals(user.getUsername(), "TestUsername");
    assertEquals(user.getEmail(), "it-test@gmail.com");
    assertNull(user.getPassword());

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @DisplayName("Bad Request \uD83D\uDE1E")
  @Test
  public void canReturnBadRequest() {

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(null, context);

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }
}
