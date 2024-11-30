package org.example.handlers.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.util.Map;
import org.example.entities.session.Session;
import org.example.entities.user.User;
import org.example.handlers.rest.auth.AuthHandler;
import org.example.utils.MockContext;
import org.example.utils.MongoDBUtility;
import org.junit.jupiter.api.*;

public class AuthHandlerTest {
  static AuthHandler authHandler;

  static MongoDBUtility<User> userUtility;
  static MongoDBUtility<Session> sessionUtility;

  static String validSessionToken;
  static String validUserId;

  @BeforeAll
  public static void setUp() {
    authHandler = new AuthHandler();

    userUtility = new MongoDBUtility<>("users", User.class);
    sessionUtility = new MongoDBUtility<>("sessions", Session.class);

    validSessionToken = "231420d4-f162-406e-8eaa-5652afb0c43d";
    validUserId = "auth-test";

    User tempUser = User.builder().id(validUserId).build();
    Session session = Session.builder().id(validSessionToken).userId("auth-test").build();

    sessionUtility.post(session);
    userUtility.post(tempUser);
  }

  @AfterAll
  public static void tearDown() {
    sessionUtility.delete(validSessionToken);
    userUtility.delete(validUserId);
  }

  @Test
  public void canAllowPolicy() {
    APIGatewayV2CustomAuthorizerEvent.Http http = APIGatewayV2CustomAuthorizerEvent.Http.builder()
        .withMethod("GET")
        .build();

    APIGatewayV2CustomAuthorizerEvent.RequestContext requestContext = APIGatewayV2CustomAuthorizerEvent.RequestContext.builder()
        .withHttp(http)
        .build();

    APIGatewayV2CustomAuthorizerEvent event = APIGatewayV2CustomAuthorizerEvent.builder()
        .withHeaders(Map.of(
            "Authorization", validSessionToken,
            "userid", validUserId))
        .withRequestContext(requestContext)
        .build();

    IamPolicyResponse response = authHandler.handleRequest(event, new MockContext());
    assertNotNull(response);

    Map<String, Object> policyDocument = response.getPolicyDocument();

    Object[] statements = (Object[]) policyDocument.get("Statement");
    assertEquals(1, statements.length);

    Map<String, Object> statementMap = (Map<String, Object>) statements[0];

    assertEquals("2012-10-17", policyDocument.get("Version"));
    assertEquals(validUserId, response.getPrincipalId());
    assertEquals("Allow", statementMap.get("Effect"));
    assertEquals("execute-api:Invoke", statementMap.get("Action"));
  }

  @Test
  public void canDenyPolicy() {
    APIGatewayV2CustomAuthorizerEvent.RequestContext mockRequestContext = APIGatewayV2CustomAuthorizerEvent.RequestContext.builder()
        .withAccountId("123456789012")
        .withApiId("abcdef1234")
        .build();

    APIGatewayV2CustomAuthorizerEvent event = APIGatewayV2CustomAuthorizerEvent.builder()
        .withHeaders(Map.of(
            "Authorization", "7a897393-4167-43fe-a618-9bdb65b53529",
            "userid", "fakeUserId"))
        .withRequestContext(mockRequestContext)
        .build();

    IamPolicyResponse response = authHandler.handleRequest(event, new MockContext());
    assertNotNull(response);

    Map<String, Object> policyDocument = response.getPolicyDocument();

    Object[] statements = (Object[]) policyDocument.get("Statement");
    assertEquals(1, statements.length);

    Map<String, Object> statementMap = (Map<String, Object>) statements[0];

    assertEquals("2012-10-17", policyDocument.get("Version"));
    assertEquals("fakeUserId", response.getPrincipalId());
    assertEquals("Deny", statementMap.get("Effect"));
    assertEquals("execute-api:Invoke", statementMap.get("Action"));
  }
}
