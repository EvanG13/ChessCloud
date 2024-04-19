package org.example.handlers.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.example.databases.users.UsersDynamoDBUtility;
import org.example.entities.User;
import org.example.handlers.TestContext;
import org.example.handlers.login.LoginHandler;
import org.example.handlers.login.LoginService;
import org.example.statusCodes.StatusCodes;
import org.example.utils.EncryptPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LoginHandlerTest {
  private LoginHandler loginHandler;
  private UsersDynamoDBUtility dbUtility;

  @BeforeEach
  public void setUp() {
    dbUtility = mock(UsersDynamoDBUtility.class);

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

    Context context = new TestContext();

    when(dbUtility.getByEmail(anyString()))
        .thenReturn(
            new User(
                "foo", "nonexistingemail@example.com", EncryptPassword.encrypt("test"), "fake"));
    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @DisplayName("Bad Request \uD83D\uDE1E")
  @Test
  public void returnBadRequest() {
    Context context = new TestContext();

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

    Context context = new TestContext();

    when(dbUtility.getByEmail(anyString()))
        .thenReturn(new User("foo", "nonexistingemail@example.com", "password123", "fake"));

    APIGatewayV2HTTPResponse response = loginHandler.handleRequest(event, context);

    assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
  }
}
