package org.example.handlers.resign;

import static org.example.utils.TestUtils.validGame;
import static org.example.utils.TestUtils.validUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameDbService;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.player.ArchivedPlayer;
import org.example.entities.player.Player;
import org.example.entities.stats.StatsDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.enums.ResultReason;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;
import org.example.handlers.websocket.resign.ResignGameHandler;
import org.example.handlers.websocket.resign.ResignGameService;
import org.example.models.requests.ResignRequest;
import org.example.utils.MockContext;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResignGameHandlerTest {
  private static ArchivedGameDbService archivedGameDbService;
  private static UserDbService userDbService;
  private static StatsDbService statsDbService;

  private static ResignGameHandler handler;
  private static Game game;
  private static User userOne;
  private static User userTwo;

  @BeforeAll
  public static void setUp() throws Exception {
    handler = new ResignGameHandler(new ResignGameService(), new SocketSystemLogger());

    archivedGameDbService = ArchivedGameDbService.builder().build();
    GameDbService gameDbService = new GameDbService();
    userDbService = new UserDbService();
    statsDbService = new StatsDbService();

    userOne = validUser();
    userTwo = validUser();

    game = validGame(TimeControl.BLITZ_5, userOne, userTwo);
    gameDbService.post(game);
  }

  @Test
  @Order(1)
  public void checkNonPlayerUserTriedResigning() {
    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    event.setBody(new Gson().toJson(Map.of("gameId", game.getId())));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId("some-other-guy");
    requestContext.setRouteKey("resign");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Your connection ID is not bound to this game", response.getBody());
  }

  @Test
  @Order(2)
  public void canResignGame() {
    List<Player> players = game.getPlayers();

    String winningPlayerId = players.getLast().getPlayerId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    ResignRequest request = new ResignRequest(game.getId());
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId("foo-id");
    requestContext.setRouteKey("resign");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.OK, response.getStatusCode());

    ArchivedGame archivedGame;
    try {
      archivedGame = archivedGameDbService.getArchivedGame(game.getId());
    } catch (NotFound e) {
      fail("Game was not successfully archived");
      return;
    }

    assertEquals(ResultReason.FORFEIT, archivedGame.getResultReason());

    ArchivedPlayer winningPlayer = archivedGame.getPlayers().getLast();
    assertEquals(true, winningPlayer.getIsWinner());
    assertEquals(winningPlayerId, winningPlayer.getPlayerId());
  }

  @Test
  public void checksForMissingBody() {
    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    event.setBody(new Gson().toJson(Map.of("foo", "fooagain")));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId("foo-id");
    requestContext.setRouteKey("resign");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals("Missing argument(s)", response.getBody());
  }

  @Test
  public void checksThatGameExists() {
    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    String fakeID = "fake";
    event.setBody(new Gson().toJson(Map.of("gameId", fakeID)));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId("foo-id");
    requestContext.setRouteKey("resign");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.NOT_FOUND, response.getStatusCode());
    assertEquals("No Game found with id " + fakeID, response.getBody());
  }

  @AfterAll
  public static void tearDown() {
    archivedGameDbService.deleteArchivedGame(game.getId());

    userDbService.deleteUser(userOne.getId());
    userDbService.deleteUser(userTwo.getId());

    statsDbService.deleteStats(userOne.getId());
    statsDbService.deleteStats(userTwo.getId());
  }
}
