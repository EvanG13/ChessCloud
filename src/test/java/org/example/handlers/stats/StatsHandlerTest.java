package org.example.handlers.stats;

import static org.example.utils.HttpTestUtils.assertResponse;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.handlers.rest.stats.StatsHandler;
import org.example.utils.MockContext;
import org.junit.jupiter.api.*;

public class StatsHandlerTest {
  public static UserDbService userDbService;
  public static StatsDbService statsDbService;

  public static StatsHandler statsHandler;

  public static String userId;
  public static String username;

  @BeforeAll
  public static void setUp() {
    userDbService = new UserDbService();
    statsDbService = new StatsDbService();

    User testUser =
        User.builder().email("test@gmail.com").password("1223").username("test-username").build();

    userId = testUser.getId();
    username = testUser.getUsername();

    userDbService.createUser(testUser);

    Stats testUserStats = new Stats(testUser.getId());
    statsDbService.post(testUserStats);

    statsHandler = new StatsHandler();
  }

  @AfterAll
  public static void tearDown() {
    userDbService.deleteUser(userId);
    statsDbService.deleteStats(userId);
  }

  @DisplayName("No Query")
  @Test
  @Order(1)
  public void returnNoQuery() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withHeaders(Map.of("userid", userId))
            .withPathParameters(Map.of("username", username))
            .build();

    APIGatewayV2HTTPResponse response = statsHandler.handleRequest(event, new MockContext());
    assertResponse(
        response,
        StatusCodes.OK,
        "{\"blitz\":{\"wins\":0,\"losses\":0,\"draws\":0,\"rating\":1000},\"rapid\":{\"wins\":0,\"losses\":0,\"draws\":0,\"rating\":1000},\"bullet\":{\"wins\":0,\"losses\":0,\"draws\":0,\"rating\":1000}}");
  }

  @DisplayName("Query \"timeCategory=bullet\" (valid)")
  @Test
  @Order(2)
  public void returnQueryBullet() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withHeaders(Map.of("userid", userId))
            .withPathParameters(Map.of("username", username))
            .withQueryStringParameters(Map.of("timeCategory", "bullet"))
            .build();

    APIGatewayV2HTTPResponse response = statsHandler.handleRequest(event, new MockContext());
    assertResponse(
        response, StatusCodes.OK, "{\"wins\":0,\"losses\":0,\"draws\":0,\"rating\":1000}");
  }

  @DisplayName("Query \"timeCategory=invalidgamemode\" (invalid)")
  @Test
  @Order(3)
  public void returnQueryInvalidGamemode() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withHeaders(Map.of("userid", userId))
            .withPathParameters(Map.of("username", username))
            .withQueryStringParameters(Map.of("timeCategory", "invalidgamemode"))
            .build();

    APIGatewayV2HTTPResponse response = statsHandler.handleRequest(event, new MockContext());
    assertResponse(
        response,
        StatusCodes.BAD_REQUEST,
        "Query parameter \"timeCategory\" had an invalid value: invalidgamemode");
  }

  @DisplayName("Invalid query parameter")
  @Test
  @Order(4)
  public void returnInvalidQueryParameter() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withHeaders(Map.of("userid", userId))
            .withPathParameters(Map.of("username", username))
            .withQueryStringParameters(Map.of("parameter", "bullet"))
            .build();

    APIGatewayV2HTTPResponse response = statsHandler.handleRequest(event, new MockContext());
    assertResponse(
        response,
        StatusCodes.BAD_REQUEST,
        "Query defined, but query parameter \"timeCategory\" was missing");
  }

  @DisplayName("Query \"timeCategory=\" (invalid)")
  @Test
  @Order(5)
  public void returnQueryBlankGamemode() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withHeaders(Map.of("userid", userId))
            .withPathParameters(Map.of("username", username))
            .withQueryStringParameters(Map.of("timeCategory", ""))
            .build();

    APIGatewayV2HTTPResponse response = statsHandler.handleRequest(event, new MockContext());
    assertResponse(
        response, StatusCodes.BAD_REQUEST, "Query parameter \"timeCategory\" was missing a value");
  }
}
