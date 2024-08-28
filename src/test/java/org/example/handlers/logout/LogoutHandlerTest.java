package org.example.handlers.logout;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Optional;
import org.example.databases.MongoDBUtility;
import org.example.entities.Session;
import org.example.entities.User;
import org.example.handlers.session.SessionService;
import org.example.statusCodes.StatusCodes;
import org.example.utils.FakeContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LogoutHandlerTest {

  private static MongoDBUtility<User> userUtility;
  private static MongoDBUtility<Session> sessionUtility;
  private static LogoutHandler logoutHandler;

  private static String sessionToken = "pretend-session-token";

  @BeforeAll
  public static void setUp() {

    sessionUtility = new MongoDBUtility<>("sessions", Session.class);
    Session newSession = Session.builder().id(sessionToken).userId("pretend-userId").build();
    sessionUtility.post(newSession);
    Optional<Session> optionalSession = sessionUtility.get(sessionToken);
    assertFalse(optionalSession.isEmpty());

    LogoutService service = new LogoutService(new SessionService(sessionUtility));

    logoutHandler = new LogoutHandler(service);
  }

  @DisplayName("OK 🔀")
  @Test
  void returnOk() {
    Context context = new FakeContext();
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setBody("{ 'sessionToken': " + sessionToken + "}");

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, context);

    assertEquals(StatusCodes.OK, response.getStatusCode());
    Optional<Session> optionalSession = sessionUtility.get(sessionToken);
    assertTrue(optionalSession.isEmpty());
  }

  @DisplayName("OK 🔀")
  @Test
  void returnBadRequest() {
    Context context = new FakeContext();
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    APIGatewayV2HTTPResponse response = logoutHandler.handleRequest(event, context);

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    Optional<Session> optionalSession = sessionUtility.get(sessionToken);
    assertTrue(optionalSession.isEmpty());
  }
}
