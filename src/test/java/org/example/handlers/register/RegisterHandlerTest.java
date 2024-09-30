package org.example.handlers.register;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import java.util.Map;
import java.util.Optional;
import org.bson.conversions.Bson;
import org.example.constants.StatusCodes;
import org.example.entities.User;
import org.example.entities.stats.Stats;
import org.example.handlers.rest.RegisterHandler;
import org.example.models.requests.RegisterRequest;
import org.example.services.RegisterService;
import org.example.utils.MockContext;
import org.example.utils.MongoDBUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class RegisterHandlerTest {
  private RegisterHandler registerHandler;
  private MongoDBUtility<User> dbUtility;
  private static Gson gson;

  @BeforeEach
  void setUp() {
    dbUtility = (MongoDBUtility<User>) mock(MongoDBUtility.class);
    MongoDBUtility<Stats> statsUtility = (MongoDBUtility<Stats>) mock(MongoDBUtility.class);

    RegisterService service = new RegisterService(dbUtility, statsUtility);
    gson = new Gson();
    registerHandler = new RegisterHandler(service);
  }

  @DisplayName("OK 👍")
  @Test
  void returnSuccess() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    RegisterRequest registerRequest = new RegisterRequest("test@gmail.com", "testuser", "test");
    event.setBody(gson.toJson(registerRequest));

    Context context = new MockContext();

    when(dbUtility.get(any(Bson.class))).thenReturn(Optional.empty());
    doNothing().when(dbUtility).post(any(User.class));
    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);

    Map<String, String> headers = response.getHeaders();
    assertEquals(headers.get("Access-Control-Allow-Origin"), "*");
    assertEquals(headers.get("Access-Control-Allow-Methods"), "POST,OPTIONS");
    assertEquals(headers.get("Access-Control-Allow-Headers"), "*");

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @DisplayName("Bad Request 😠")
  @Test
  void returnBadRequest() {
    Context context = new MockContext();

    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(null, context);

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @DisplayName("Conflict 🔀")
  @Test
  void returnConflict() {
    Context context = new MockContext();
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    RegisterRequest registerRequest = new RegisterRequest("test@gmail.com", "testuser", "test");
    event.setBody(gson.toJson(registerRequest));

    when(dbUtility.get(any(Bson.class)))
        .thenReturn(
            Optional.of(
                User.builder().id("1").email("test@gmail.com").username("testuser").build()));
    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);

    assertEquals(StatusCodes.CONFLICT, response.getStatusCode());
  }
}
