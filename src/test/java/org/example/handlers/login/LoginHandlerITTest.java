package org.example.handlers.login;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.databases.users.UsersMongoDBUtility;
import org.example.entities.User;
import org.example.handlers.TestContext;
import org.example.requestRecords.UserRequest;
import org.example.statusCodes.StatusCodes;
import org.junit.jupiter.api.*;

@Tag("Integration")
public class LoginHandlerITTest {
  private static LoginHandler loginHandler;

  private static Context context;

  private static UsersMongoDBUtility dbUtility;

  @BeforeAll
  public static void setUp() {

    dbUtility = new UsersMongoDBUtility();
    dbUtility.post(
        new UserRequest(
            "it-test@gmail.com",
            "TestUsername",
            "$2a$12$MwPTs6UFjy7NAge3HxHwEOTUvX2M6bXpqkHCozjisNTCpcaQ9ZiyC"));

    loginHandler = new LoginHandler();

    context = new TestContext();
  }

  @AfterAll
  public static void tearDown() {
    User tempUser = dbUtility.getByEmail("it-test@gmail.com");
    dbUtility.delete(tempUser.getId());
  }

  @DisplayName("OK")
  @Test
  public void canLogin() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

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
