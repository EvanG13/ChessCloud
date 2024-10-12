package org.example.handlers.offerDraw;

import static org.example.utils.TestUtils.validGame;
import static org.example.utils.TestUtils.validUser;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.player.Player;
import org.example.entities.stats.StatsDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.enums.TimeControl;
import org.example.handlers.websocket.offerDraw.OfferDrawHandler;
import org.example.handlers.websocket.offerDraw.OfferDrawService;
import org.example.models.requests.OfferDrawRequest;
import org.example.utils.MockContext;
import org.example.utils.socketMessenger.SocketMessenger;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OfferDrawHandlerTest {
  private static UserDbService userDbService;
  private static StatsDbService statsDbService;
  private static GameDbService gameDbService;

  private static OfferDrawHandler handler;
  private static Game game;
  private static User userOne;
  private static User userTwo;

  @BeforeAll
  public static void setUp() throws Exception {
    SocketMessenger messenger = new SocketSystemLogger();
    OfferDrawService offerDrawService = OfferDrawService.builder().messenger(messenger).build();

    handler = new OfferDrawHandler(offerDrawService, messenger);

    gameDbService = new GameDbService();
    userDbService = new UserDbService();
    statsDbService = new StatsDbService();

    userOne = validUser();
    userTwo = validUser();

    game = validGame(TimeControl.BLITZ_5, userOne, userTwo);
    gameDbService.post(game);
  }

  @Test
  public void canOfferDraw() {
    List<Player> players = game.getPlayers();

    String offeringPlayerConnectionId = players.getFirst().getPlayerId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request = new OfferDrawRequest(game.getId());
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @Test
  public void checksValidBody() {
    List<Player> players = game.getPlayers();

    String offeringPlayerConnectionId = players.getFirst().getPlayerId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    event.setBody(new Gson().toJson(Map.of("nonsense", "nonexistinggame")));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void checksThatGameExists() {
    List<Player> players = game.getPlayers();

    String offeringPlayerConnectionId = players.getFirst().getPlayerId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request = new OfferDrawRequest("nonexistinggame");
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

    assertEquals(StatusCodes.NOT_FOUND, response.getStatusCode());
  }

  @AfterAll
  public static void tearDown() {
    userDbService.deleteUser(userOne.getId());
    userDbService.deleteUser(userTwo.getId());

    statsDbService.deleteStats(userOne.getId());
    statsDbService.deleteStats(userTwo.getId());

    gameDbService.deleteGame(game.getId());
  }
}
