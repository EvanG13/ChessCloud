package org.example.handlers.register;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import java.util.Optional;
import org.example.databases.UsersMongoDBUtility;
import org.example.entities.User;
import org.example.requestRecords.UserRequest;
import org.example.statusCodes.StatusCodes;
import org.example.utils.FakeContext;
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
    Optional<User> user = dbUtility.getByEmail("test3@gmail.com");
    assertTrue(user.isPresent());

    dbUtility.delete(user.get().getId());

    Optional<User> tempUser = dbUtility.getByEmail("reg-it-test@gmail.com");
    assertTrue(tempUser.isPresent());
    dbUtility.delete(tempUser.get().getId());
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

    Context context = new FakeContext();

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);

    Map<String, String> headers = response.getHeaders();
    assertEquals(headers.get("Access-Control-Allow-Origin"), "http://localhost:8081");
    assertEquals(headers.get("Access-Control-Allow-Methods"), "OPTIONS,POST,GET");
    assertEquals(headers.get("Access-Control-Allow-Headers"), "Content-Type");

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @DisplayName("Bad Request 😠")
  @Test
  void returnBadRequest() {
    Context context = new FakeContext();

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(null, context);

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Conflict 🔀")
  @Test
  void returnConflict() {
    Context context = new FakeContext();
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
