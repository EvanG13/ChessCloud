package org.example.handlers.offerDraw;

import static org.example.utils.TestUtils.validGame;
import static org.example.utils.TestUtils.validUser;
import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameDbService;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.stats.StatsDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.enums.OfferDrawAction;
import org.example.enums.ResultReason;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;
import org.example.handlers.websocket.offerDraw.OfferDrawHandler;
import org.example.handlers.websocket.offerDraw.OfferDrawService;
import org.example.models.requests.OfferDrawRequest;
import org.example.utils.MockContext;
import org.example.utils.socketMessenger.SocketMessenger;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OfferDrawHandlerTest {
  private static UserDbService userDbService;
  private static StatsDbService statsDbService;
  private static GameDbService gameDbService;
  private static ArchivedGameDbService archivedGameDbService;

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
    archivedGameDbService = ArchivedGameDbService.builder().build();

    userOne = validUser();
    userTwo = validUser();

    game = validGame(TimeControl.BLITZ_5, userOne, userTwo);
    gameDbService.post(game);
  }

  @Test
  @DisplayName("Player 1 tries to cancel nonexistent offer")
  @Order(1)
  public void cantCancelOfferDraw() {
    // Player 1
    String offeringPlayerConnectionId = game.getPlayers().getFirst().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.CANCEL);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());

    try {
      Game gameActual = gameDbService.get(game.getId());

      assertFalse(gameActual.getPlayers().getFirst().getWantsDraw()); // Player 1: nothing
      assertFalse(gameActual.getPlayers().getLast().getWantsDraw()); // Player 2: nothing
    } catch (Exception e) {
      fail("Game was not retrieved properly");
    }
  }

  @Test
  @DisplayName("Player 1 tries to deny nonexistent offer")
  @Order(2)
  public void cantDenyOfferDraw() {
    // Player 1
    String offeringPlayerConnectionId = game.getPlayers().getFirst().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.DENY);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());

    try {
      Game gameActual = gameDbService.get(game.getId());

      assertFalse(gameActual.getPlayers().getFirst().getWantsDraw()); // Player 1: nothing
      assertFalse(gameActual.getPlayers().getLast().getWantsDraw()); // Player 2: nothing
    } catch (Exception e) {
      fail("Game was not retrieved properly");
    }
  }

  @Test
  @DisplayName("Player 1 tries to accept nonexistent offer")
  @Order(3)
  public void cantAcceptOfferDraw() {
    // Player 1
    String offeringPlayerConnectionId = game.getPlayers().getFirst().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.ACCEPT);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());

    try {
      Game gameActual = gameDbService.get(game.getId());

      assertFalse(gameActual.getPlayers().getFirst().getWantsDraw()); // Player 1: nothing
      assertFalse(gameActual.getPlayers().getLast().getWantsDraw()); // Player 2: nothing
    } catch (Exception e) {
      fail("Game was not retrieved properly");
    }
  }

  @Test
  @DisplayName("Player 1 offers draw")
  @Order(4)
  public void canOfferDraw() {
    // Player 1
    String offeringPlayerConnectionId = game.getPlayers().getFirst().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.OFFER);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    try {
      Game gameActual = gameDbService.get(game.getId());

      assertTrue(gameActual.getPlayers().getFirst().getWantsDraw()); // Player 1: offered to draw
      assertFalse(
          gameActual.getPlayers().getLast().getWantsDraw()); // Player 2: waiting for response
    } catch (Exception e) {
      fail("Game was not retrieved properly");
    }
  }

  @Test
  @DisplayName("Player 2 tries to offer draw")
  @Order(5)
  public void cantOfferWhileOffered() {
    // Player 2
    String offeringPlayerConnectionId = game.getPlayers().getLast().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.OFFER);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());

    try {
      Game gameActual = gameDbService.get(game.getId());

      assertTrue(gameActual.getPlayers().getFirst().getWantsDraw()); // Player 1: offered to draw
      assertFalse(
          gameActual.getPlayers().getLast().getWantsDraw()); // Player 2: waiting for response
    } catch (Exception e) {
      fail("Game was not retrieved properly");
    }
  }

  @Test
  @DisplayName("Player 1 cancels draw offer")
  @Order(6)
  public void canCancelOfferDraw() {
    // Player 1
    String offeringPlayerConnectionId = game.getPlayers().getFirst().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.CANCEL);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    try {
      Game gameActual = gameDbService.get(game.getId());

      assertFalse(gameActual.getPlayers().getFirst().getWantsDraw()); // Player 1: canceled offer
      assertFalse(
          gameActual
              .getPlayers()
              .getLast()
              .getWantsDraw()); // Player 2: reset before they could respond
    } catch (Exception e) {
      fail("Game was not retrieved properly");
    }
  }

  @Test
  @DisplayName("Player 2 offers draw")
  @Order(7)
  public void canOfferDraw2() {
    // Player 2
    String offeringPlayerConnectionId = game.getPlayers().getLast().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.OFFER);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    try {
      Game gameActual = gameDbService.get(game.getId());

      assertTrue(gameActual.getPlayers().getLast().getWantsDraw()); // Player 2: made offer to draw
      assertFalse(
          gameActual.getPlayers().getFirst().getWantsDraw()); // Player 1: waiting for response
    } catch (Exception e) {
      fail("Game was not retrieved properly");
    }
  }

  @Test
  @DisplayName("Player 2 tries to accept their own draw offer")
  @Order(8)
  public void checkPlayerStartingOfferCantAccept() {
    // Player 2
    String offeringPlayerConnectionId = game.getPlayers().getLast().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.ACCEPT);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());

    try {
      Game gameActual = gameDbService.get(game.getId());

      assertTrue(
          gameActual
              .getPlayers()
              .getLast()
              .getWantsDraw()); // Player 2: no change, tried to accept their own offer
      assertFalse(
          gameActual
              .getPlayers()
              .getFirst()
              .getWantsDraw()); // Player 1: waiting for response still
    } catch (Exception e) {
      fail("Game was not retrieved properly");
    }
  }

  @Test
  @DisplayName("Player 1 denies draw offer")
  @Order(9)
  public void canDenyDraw() {
    // Player 1
    String denyingPlayerConnectionId = game.getPlayers().getFirst().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.DENY);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(denyingPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    try {
      Game gameActual = gameDbService.get(game.getId());

      assertFalse(
          gameActual
              .getPlayers()
              .getFirst()
              .getWantsDraw()); // Player 1: reset because Player 1 rejected
      assertFalse(
          gameActual
              .getPlayers()
              .getLast()
              .getWantsDraw()); // Player 2: reset because Player 1 rejected
    } catch (Exception e) {
      fail("Game was not retrieved properly");
    }
  }

  @Test
  @DisplayName("Player 1 offers draw")
  @Order(10)
  public void canOfferDraw3() {
    // Player 1
    String offeringPlayerConnectionId = game.getPlayers().getFirst().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.OFFER);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(offeringPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    try {
      Game gameActual = gameDbService.get(game.getId());

      assertTrue(gameActual.getPlayers().getFirst().getWantsDraw()); // Player 1: offered
      assertFalse(
          gameActual.getPlayers().getLast().getWantsDraw()); // Player 2: waiting for response
    } catch (Exception e) {
      fail("Game was not retrieved properly");
    }
  }

  @Test
  @DisplayName("Player 2 accepts draw")
  @Order(11)
  public void canAcceptDraw() {
    // Player 2
    String acceptingPlayerConnectionId = game.getPlayers().getLast().getConnectionId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest(game.getId(), OfferDrawAction.ACCEPT);
    event.setBody(new Gson().toJson(request));

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(acceptingPlayerConnectionId);
    requestContext.setRouteKey("offerDraw");
    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    // Assert game got deleted
    assertThrowsExactly(NotFound.class, () -> gameDbService.get(game.getId()));

    // Get the archived game
    ArchivedGame archivedGame;
    try {
      archivedGame = archivedGameDbService.getArchivedGame(game.getId());
    } catch (Exception e) {
      fail("Game was not successfully archived");
      return;
    }

    // Make sure reason was mutually agreed draw
    assertEquals(ResultReason.MUTUAL_DRAW, archivedGame.getResultReason());
  }

  @Test
  public void checksValidBody() {
    String offeringPlayerConnectionId = game.getPlayers().getFirst().getPlayerId();

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
    String offeringPlayerConnectionId = game.getPlayers().getFirst().getPlayerId();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    OfferDrawRequest request =
        new OfferDrawRequest("nonexistinggame", OfferDrawAction.OFFER);
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

    gameDbService.deleteGame(game.getId()); // if tests error midway
    archivedGameDbService.deleteArchivedGame(game.getId()); // if tests didn't error
  }
}
