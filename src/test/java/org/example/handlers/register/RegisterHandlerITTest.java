package org.example.handlers.register;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.example.databases.users.UsersMongoDBUtility;
import org.example.entities.User;
import org.example.handlers.TestContext;
import org.example.requestRecords.UserRequest;
import org.example.statusCodes.StatusCodes;
import org.junit.jupiter.api.*;

@Tag("Integration")
public class RegisterHandlerITTest {
  private static RegisterHandler registerHandler;
  private static UsersMongoDBUtility dbUtility;

  @BeforeAll
  public static void setUp() {
    dbUtility = new UsersMongoDBUtility();

    dbUtility.post(
        new UserRequest(
            "reg-it-test@gmail.com",
            "TestUsername",
            "$2a$12$MwPTs6UFjy7NAge3HxHwEOTUvX2M6bXpqkHCozjisNTCpcaQ9ZiyC"));

    RegisterService service = new RegisterService(dbUtility);

    registerHandler = new RegisterHandler(service);
  }

  @AfterAll
  public static void tearDown() {
    User user = dbUtility.getByEmail("test3@gmail.com");
    dbUtility.delete(user.getId());

    User tempUser = dbUtility.getByEmail("reg-it-test@gmail.com");
    dbUtility.delete(tempUser.getId());
  }

  @DisplayName("OK 👍")
  @Test
  void returnSuccess() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    event.setBody(
        """
         {
                  "email": "test3@gmail.com",
                  "username": "testuser3",
                  "password": "test"
         }""");

    Context context = new TestContext();

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @DisplayName("Bad Request 😠")
  @Test
  void returnBadRequest() {
    Context context = new TestContext();

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(null, context);

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Conflict 🔀")
  @Test
  void returnConflict() {
    Context context = new TestContext();
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setBody(
        """
         {
                  "email": "reg-it-test@gmail.com",
                  "username": "testuser",
                  "password": "test"
         }""");

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);

    assertEquals(StatusCodes.CONFLICT, response.getStatusCode());
  }
}
