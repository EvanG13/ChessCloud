package org.example.handlers.register;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import java.util.Optional;
import org.bson.conversions.Bson;
import org.example.constants.StatusCodes;
import org.example.entities.Stats;
import org.example.entities.User;
import org.example.handlers.rest.RegisterHandler;
import org.example.services.RegisterService;
import org.example.utils.FakeContext;
import org.example.utils.MongoDBUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class RegisterHandlerTest {
  private RegisterHandler registerHandler;
  private MongoDBUtility<User> dbUtility;
  private MongoDBUtility<Stats> statsUtility;

  @BeforeEach
  void setUp() {
    dbUtility = (MongoDBUtility<User>) mock(MongoDBUtility.class);
    statsUtility = (MongoDBUtility<Stats>) mock(MongoDBUtility.class);

    RegisterService service = new RegisterService(dbUtility, statsUtility);

    registerHandler = new RegisterHandler(service);
  }

  @DisplayName("OK 👍")
  @Test
  void returnSuccess() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    event.setBody(
        """
         {
                  "email": "test@gmail.com",
                  "username": "testuser",
                  "password": "test"
         }""");

    Context context = new FakeContext();

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
                  "email": "test@gmail.com",
                  "username": "testuser",
                  "password": "test"
         }""");

    when(dbUtility.get(any(Bson.class)))
        .thenReturn(
            Optional.of(
                User.builder().id("1").email("test@gmail.com").username("testuser").build()));
    APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);

    assertEquals(StatusCodes.CONFLICT, response.getStatusCode());
  }
}
