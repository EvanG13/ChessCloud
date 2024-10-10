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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
  public void canResignGame() {
    List<Player> players = game.getPlayers();

    String loserPlayerId = players.getFirst().getPlayerId();
    String winningPlayerId = players.getLast().getPlayerId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    event.setPathParameters(Map.of("gameId", game.getId()));

    ResignRequest request = new ResignRequest(loserPlayerId);

    event.setBody((new Gson()).toJson(request, ResignRequest.class));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId("foo-connection-id");
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
  public void checksForMissingPathParam() {
    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    event.setPathParameters(Map.of("foo", "fooagain"));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId("foo-connection-id");
    requestContext.setRouteKey("resign");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals("Missing gameId as a path param", response.getBody());
  }

  @Test
  public void checksForMissingRequestBody() {
    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    event.setPathParameters(Map.of("gameId", "fake"));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId("foo-connection-id");
    requestContext.setRouteKey("resign");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals("Missing argument(s)", response.getBody());
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
