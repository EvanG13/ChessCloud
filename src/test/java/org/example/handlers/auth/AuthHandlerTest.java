package org.example.handlers.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.util.HashMap;
import java.util.Map;
import org.example.databases.MongoDBUtility;
import org.example.entities.Session;
import org.example.utils.FakeContext;
import org.junit.jupiter.api.*;

public class AuthHandlerTest {

  static AuthHandler authHandler;

  static APIGatewayV2CustomAuthorizerEvent event;
  static Map<String, String> headers;

  static MongoDBUtility<Session> dbUtility;

  static String validSessionToken;
  static String validUserId;

  @BeforeAll
  public static void setUp() {
    event = new APIGatewayV2CustomAuthorizerEvent();

    APIGatewayV2CustomAuthorizerEvent.RequestContext mockRequestContext =
        new APIGatewayV2CustomAuthorizerEvent.RequestContext();
    mockRequestContext.setAccountId("123456789012");
    mockRequestContext.setApiId("abcdef1234");

    event.setRequestContext(mockRequestContext);
    headers = new HashMap<>();

    authHandler = new AuthHandler();

    dbUtility = new MongoDBUtility<>("sessions", Session.class);

    validSessionToken = "231420d4-f162-406e-8eaa-5652afb0c43d";
    validUserId = "auth-test";
    Session session = Session.builder().id(validSessionToken).userId("auth-test").build();
    dbUtility.post(session);
  }

  @AfterAll
  public static void tearDown() {
    dbUtility.delete(validSessionToken);
  }

  @Test
  public void canAllowPolicy() {
    headers.put("Authorization", validSessionToken);
    headers.put("userId", validUserId);

    event.setHeaders(headers);

    IamPolicyResponse response = authHandler.handleRequest(event, new FakeContext());
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
    headers.put("Authorization", "7a897393-4167-43fe-a618-9bdb65b53529");
    headers.put("userId", "fakeUserId");

    event.setHeaders(headers);

    IamPolicyResponse response = authHandler.handleRequest(event, new FakeContext());
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