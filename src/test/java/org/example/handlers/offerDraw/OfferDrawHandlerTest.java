package org.example.handlers.offerDraw;

import static org.example.utils.TestUtils.validGame;
import static org.example.utils.TestUtils.validUser;
import static org.example.utils.WebsocketTestUtils.getResponse;
import static org.example.utils.WebsocketTestUtils.makeRequestContext;
import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.*;
import org.example.entities.stats.StatsUtility;
import org.example.entities.timeControl.TimeControl;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.enums.OfferDrawAction;
import org.example.enums.ResultReason;
import org.example.exceptions.NotFound;
import org.example.handlers.websocket.offerDraw.OfferDrawHandler;
import org.example.handlers.websocket.offerDraw.OfferDrawService;
import org.example.models.requests.OfferDrawRequest;
import org.example.utils.socketMessenger.SocketMessenger;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OfferDrawHandlerTest {
  private static UserUtility userUtility;
  private static StatsUtility statsUtility;
  private static GameUtility gameUtility;
  private static ArchivedGameUtility archivedGameUtility;

  private static OfferDrawHandler handler;
  private static Game game;
  private static User userOne;
  private static User userTwo;

  @BeforeAll
  public static void setUp() throws Exception {
    SocketMessenger messenger = new SocketSystemLogger();
    OfferDrawService offerDrawService = OfferDrawService.builder().messenger(messenger).build();

    handler = new OfferDrawHandler(offerDrawService, messenger);

    gameUtility = new GameUtility();
    userUtility = new UserUtility();
    statsUtility = new StatsUtility();
    archivedGameUtility = new ArchivedGameUtility();

    userOne = validUser();
    userTwo = validUser();

    game = validGame(new TimeControl(300, 0), userOne, userTwo);
    gameUtility.post(game);
  }

  @Test
  @DisplayName("Player 1 tries to cancel nonexistent offer")
  @Order(1)
  public void cantCancelOfferDraw() {
    // Player 1
    String offeringPlayerConnectionId = game.getPlayers().getFirst().getConnectionId();

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.CANCEL),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());

    try {
      Game gameActual = gameUtility.getGame(game.getId());

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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.DENY),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());

    try {
      Game gameActual = gameUtility.getGame(game.getId());

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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.ACCEPT),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());

    try {
      Game gameActual = gameUtility.getGame(game.getId());

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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.OFFER),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.OK, response.getStatusCode());

    try {
      Game gameActual = gameUtility.getGame(game.getId());

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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.OFFER),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());

    try {
      Game gameActual = gameUtility.getGame(game.getId());

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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.CANCEL),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.OK, response.getStatusCode());

    try {
      Game gameActual = gameUtility.getGame(game.getId());

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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.OFFER),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.OK, response.getStatusCode());

    try {
      Game gameActual = gameUtility.getGame(game.getId());

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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.ACCEPT),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());

    try {
      Game gameActual = gameUtility.getGame(game.getId());

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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.DENY),
            makeRequestContext("offerDraw", denyingPlayerConnectionId));

    assertEquals(StatusCodes.OK, response.getStatusCode());

    try {
      Game gameActual = gameUtility.getGame(game.getId());

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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.OFFER),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.OK, response.getStatusCode());

    try {
      Game gameActual = gameUtility.getGame(game.getId());

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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest(game.getId(), OfferDrawAction.ACCEPT),
            makeRequestContext("offerDraw", acceptingPlayerConnectionId));

    assertEquals(StatusCodes.OK, response.getStatusCode());

    // Assert game got deleted
    assertThrowsExactly(NotFound.class, () -> gameUtility.getGame(game.getId()));

    // Get the archived game
    ArchivedGame archivedGame;
    try {
      archivedGame = archivedGameUtility.getGame(game.getId());
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

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            Map.of("nonsense", "nonexistinggame"),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void checksThatGameExists() {
    String offeringPlayerConnectionId = game.getPlayers().getFirst().getPlayerId();

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new OfferDrawRequest("nonexistinggame", OfferDrawAction.OFFER),
            makeRequestContext("offerDraw", offeringPlayerConnectionId));

    assertEquals(StatusCodes.NOT_FOUND, response.getStatusCode());
  }

  @AfterAll
  public static void tearDown() {
    userUtility.delete(userOne.getId());
    userUtility.delete(userTwo.getId());

    statsUtility.delete(userOne.getId());
    statsUtility.delete(userTwo.getId());

    gameUtility.delete(game.getId()); // if tests error midway
    archivedGameUtility.delete(game.getId()); // if tests didn't error
  }
}
