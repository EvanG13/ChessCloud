package org.example.handlers.register;

import static com.mongodb.client.model.Filters.eq;
import static org.example.utils.TestUtils.assertCorsHeaders;
import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.example.constants.StatusCodes;
import org.example.entities.stats.Stats;
import org.example.entities.user.User;
import org.example.handlers.rest.RegisterHandler;
import org.example.models.requests.RegisterRequest;
import org.example.services.RegisterService;
import org.example.utils.EncryptPassword;
import org.example.utils.MockContext;
import org.example.utils.MongoDBUtility;
import org.junit.jupiter.api.*;

public class RegisterHandlerTest {
  private static RegisterHandler registerHandler;
  private static MongoDBUtility<User> userDbUtility;
  private static MongoDBUtility<Stats> statsUtility;
  private static Gson gson;

  @BeforeAll
  public static void setUp() {
    userDbUtility = new MongoDBUtility<>("users", User.class);
    statsUtility = new MongoDBUtility<>("stats", Stats.class);
    gson = new Gson();
    User newUser =
        User.builder()
            .id(new ObjectId().toString())
            .email("reg-it-test@gmail.com")
            .password(EncryptPassword.encrypt("test"))
            .username("TestUsername")
            .build();

    userDbUtility.post(newUser);

    Stats newUserStats = new Stats(newUser.getId());
    statsUtility.post(newUserStats);

    RegisterService service = new RegisterService(userDbUtility, statsUtility);

    registerHandler = new RegisterHandler(service);
  }

  @AfterAll
  public static void tearDown() {
    Optional<User> user = userDbUtility.get(eq("email", "test3@gmail.com"));
    assertTrue(user.isPresent());

    Optional<Stats> userStats = statsUtility.get(user.get().getId());
    assertTrue(userStats.isPresent());

    userDbUtility.delete(user.get().getId());
    statsUtility.delete(userStats.get().getId());

    Optional<User> tempUser = userDbUtility.get(eq("email", "reg-it-test@gmail.com"));
    assertTrue(tempUser.isPresent());

    Optional<Stats> tempUserStats = statsUtility.get(tempUser.get().getId());
    assertTrue(tempUserStats.isPresent());

    userDbUtility.delete(tempUser.get().getId());
    statsUtility.delete(tempUserStats.get().getId());
  }

  @DisplayName("OK 👍")
  @Test
  void returnSuccess() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    RegisterRequest registerRequest = new RegisterRequest("test3@gmail.com", "testuser3", "test");
    event.setBody(gson.toJson(registerRequest));

    Context context = new MockContext();

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);

    assertCorsHeaders(response.getHeaders());

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @DisplayName("Bad Request - Missing Event 😠")
  @Test
  void returnBadRequest() {
    Context context = new MockContext();

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(null, context);
    assertCorsHeaders(response.getHeaders());

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Bad Request - Missing Arg 😠")
  @Test
  void returnBadRequestMissingArgs() {
    Context context = new MockContext();
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    RegisterRequest registerRequest = new RegisterRequest("reg-it-test@gmail.com", null, "test");
    event.setBody(gson.toJson(registerRequest));

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);
    assertCorsHeaders(response.getHeaders());

    assertEquals("Missing argument(s)", response.getBody());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Conflict 🔀")
  @Test
  void returnConflict() {
    Context context = new MockContext();
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    RegisterRequest registerRequest =
        new RegisterRequest("reg-it-test@gmail.com", "testuser", "test");
    event.setBody(gson.toJson(registerRequest));

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);
    assertCorsHeaders(response.getHeaders());

    assertEquals(StatusCodes.CONFLICT, response.getStatusCode());
  }
}
