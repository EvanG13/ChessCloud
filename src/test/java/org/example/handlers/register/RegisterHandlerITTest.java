package org.example.handlers.register;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.example.databases.MongoDBUtility;
import org.example.entities.Stats;
import org.example.entities.User;
import org.example.statusCodes.StatusCodes;
import org.example.utils.EncryptPassword;
import org.example.utils.FakeContext;
import org.junit.jupiter.api.*;

public class RegisterHandlerITTest {
  private static RegisterHandler registerHandler;
  private static MongoDBUtility<User> utility;
  private static MongoDBUtility<Stats> statsUtility;

  @BeforeAll
  public static void setUp() {
    utility = new MongoDBUtility<>("users", User.class);
    statsUtility = new MongoDBUtility<>("stats", Stats.class);

    User newUser =
        User.builder()
            .id(new ObjectId().toString())
            .email("reg-it-test@gmail.com")
            .password(EncryptPassword.encrypt("test"))
            .username("TestUsername")
            .build();

    utility.post(newUser);

    Stats newUserStats = new Stats(newUser.getId());
    statsUtility.post(newUserStats);

    RegisterService service = new RegisterService(utility, statsUtility);

    registerHandler = new RegisterHandler(service);
  }

  @AfterAll
  public static void tearDown() {
    Optional<User> user = utility.get(eq("email", "test3@gmail.com"));
    assertTrue(user.isPresent());

    Optional<Stats> userStats = statsUtility.get(user.get().getId());
    assertTrue(userStats.isPresent());

    utility.delete(user.get().getId());
    statsUtility.delete(userStats.get().getId());

    Optional<User> tempUser = utility.get(eq("email", "reg-it-test@gmail.com"));
    assertTrue(tempUser.isPresent());

    Optional<Stats> tempUserStats = statsUtility.get(tempUser.get().getId());
    assertTrue(tempUserStats.isPresent());

    utility.delete(tempUser.get().getId());
    statsUtility.delete(tempUserStats.get().getId());
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
    assertEquals(headers.get("Access-Control-Allow-Origin"), "*");
    assertEquals(headers.get("Access-Control-Allow-Methods"), "POST,OPTIONS");
    assertEquals(headers.get("Access-Control-Allow-Headers"), "*");

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @DisplayName("Bad Request - Missing Event 😠")
  @Test
  void returnBadRequest() {
    Context context = new FakeContext();

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(null, context);

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Bad Request - Missing Arg 😠")
  @Test
  void returnBadRequestMissingArgs() {
    Context context = new FakeContext();
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setBody(
        """
             {
                      "email": "reg-it-test@gmail.com",
                      "password": "test"
             }""");

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);

    assertEquals("Missing argument(s)", response.getBody());
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
