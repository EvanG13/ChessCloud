package org.example.handlers.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import org.example.databases.MongoDBUtility;
import org.example.entities.Stats;
import org.example.entities.User;
import org.example.statusCodes.StatusCodes;
import org.example.utils.FakeContext;
import org.junit.jupiter.api.*;

public class StatsHandlerTest {
  public static MongoDBUtility<User> userUtility;
  public static MongoDBUtility<Stats> statsUtility;

  public static String userId;

  @BeforeAll
  public static void setUp() {
    userUtility = new MongoDBUtility<>("users", User.class);
    statsUtility = new MongoDBUtility<>("stats", Stats.class);

    userId = "test-Id";

    User testUser =
        User.builder()
            .id(userId)
            .email("test@gmail.com")
            .password("1223")
            .username("test-username")
            .build();
    userUtility.post(testUser);

    Stats testUserStats = new Stats(testUser.getId());
    statsUtility.post(testUserStats);
  }

  @AfterAll
  public static void tearDown() {
    userUtility.delete(userId);
    statsUtility.delete(userId);
  }

  @DisplayName("No Query")
  @Test
  @Order(1)
  public void returnNoQuery() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(Map.of("userid", userId));

    Context context = new FakeContext();

    StatsHandler statsHandler = new StatsHandler();

    APIGatewayV2HTTPResponse response = statsHandler.handleRequest(event, context);

    assertEquals(StatusCodes.OK, response.getStatusCode());
    assertEquals(
        "{\"blitz\":{\"wins\":0,\"losses\":0,\"draws\":0,\"rating\":1000,\"RD\":350.0},\"rapid\":{\"wins\":0,\"losses\":0,\"draws\":0,\"rating\":1000,\"RD\":350.0},\"bullet\":{\"wins\":0,\"losses\":0,\"draws\":0,\"rating\":1000,\"RD\":350.0}}",
        response.getBody());
  }

  @DisplayName("Query \"gamemode=bullet\" (valid)")
  @Test
  @Order(2)
  public void returnQueryBullet() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(Map.of("userid", userId));
    event.setQueryStringParameters(Map.of("gamemode", "bullet"));

    Context context = new FakeContext();

    StatsHandler statsHandler = new StatsHandler();

    APIGatewayV2HTTPResponse response = statsHandler.handleRequest(event, context);

    assertEquals(StatusCodes.OK, response.getStatusCode());
    assertEquals(
        "{\"wins\":0,\"losses\":0,\"draws\":0,\"rating\":1000,\"RD\":350.0}", response.getBody());
  }

  @DisplayName("Query \"gamemode=invalidgamemode\" (invalid)")
  @Test
  @Order(3)
  public void returnQueryInvalidGamemode() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(Map.of("userid", userId));
    event.setQueryStringParameters(Map.of("gamemode", "invalidgamemode"));

    Context context = new FakeContext();

    StatsHandler statsHandler = new StatsHandler();

    APIGatewayV2HTTPResponse response = statsHandler.handleRequest(event, context);

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals(
        "Query parameter \"gamemode\" had an invalid value: invalidgamemode", response.getBody());
  }

  @DisplayName("Invalid query parameter")
  @Test
  @Order(4)
  public void returnInvalidQueryParameter() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(Map.of("userid", userId));
    event.setQueryStringParameters(Map.of("parameter", "bullet"));

    Context context = new FakeContext();

    StatsHandler statsHandler = new StatsHandler();

    APIGatewayV2HTTPResponse response = statsHandler.handleRequest(event, context);

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals("Query defined, but query parameter \"gamemode\" was missing", response.getBody());
  }

  @DisplayName("Query \"gamemode=\" (invalid)")
  @Test
  @Order(5)
  public void returnQueryBlankGamemode() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(Map.of("userid", userId));
    event.setQueryStringParameters(Map.of("gamemode", ""));

    Context context = new FakeContext();

    StatsHandler statsHandler = new StatsHandler();

    APIGatewayV2HTTPResponse response = statsHandler.handleRequest(event, context);

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals("Query parameter \"gamemode\" was missing a value", response.getBody());
  }
}
