package org.example.handlers.resign;

import static org.example.utils.TestUtils.validGame;
import static org.example.utils.TestUtils.validUser;
import static org.example.utils.WebsocketTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.List;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.*;
import org.example.entities.player.ArchivedPlayer;
import org.example.entities.player.Player;
import org.example.entities.stats.StatsUtility;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.enums.ResultReason;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;
import org.example.handlers.websocket.resign.ResignGameHandler;
import org.example.handlers.websocket.resign.ResignGameService;
import org.example.models.requests.ResignRequest;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResignGameHandlerTest {
  private static ArchivedGameUtility archivedGameUtility;
  private static UserUtility userUtility;
  private static StatsUtility statsUtility;

  private static ResignGameHandler handler;
  private static Game game;
  private static User userOne;
  private static User userTwo;

  @BeforeAll
  public static void setUp() throws Exception {
    handler = new ResignGameHandler(new ResignGameService(), new SocketSystemLogger());

    archivedGameUtility = new ArchivedGameUtility();
    GameUtility gameUtility = new GameUtility();
    userUtility = new UserUtility();
    statsUtility = new StatsUtility();

    userOne = validUser();
    userTwo = validUser();

    game = validGame(TimeControl.BLITZ_5, userOne, userTwo);
    gameUtility.post(game);
  }

  @Test
  @Order(1)
  public void checkNonPlayerUserTriedResigning() {
    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new ResignRequest(game.getId()),
            makeRequestContext("resign", "some-other-guy"));

    assertResponse(
        response, StatusCodes.UNAUTHORIZED, "Your connection ID is not bound to this game");
  }

  @Test
  @Order(2)
  public void canResignGame() {
    List<Player> players = game.getPlayers();

    String winningPlayerId = players.getLast().getPlayerId();
    String losingConnectionId = players.getFirst().getConnectionId();

    APIGatewayV2WebSocketResponse response =
        getResponse(
            handler,
            new ResignRequest(game.getId()),
            makeRequestContext("resign", losingConnectionId));

    assertEquals(StatusCodes.OK, response.getStatusCode());

    ArchivedGame archivedGame;
    try {
      archivedGame = archivedGameUtility.getGame(game.getId());
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
    APIGatewayV2WebSocketResponse response =
        getResponse(handler, Map.of("foo", "fooagain"), makeRequestContext("resign", "foo-id"));

    assertResponse(response, StatusCodes.BAD_REQUEST, "Missing argument(s)");
  }

  @Test
  public void checksThatGameExists() {
    String fakeID = "fake";
    String expectedBody = "No Game found with id " + fakeID;

    APIGatewayV2WebSocketResponse response =
        getResponse(handler, new ResignRequest(fakeID), makeRequestContext("resign", "foo-id"));

    assertResponse(response, StatusCodes.NOT_FOUND, expectedBody);
  }

  @AfterAll
  public static void tearDown() {
    archivedGameUtility.delete(game.getId());

    userUtility.delete(userOne.getId());
    userUtility.delete(userTwo.getId());

    statsUtility.delete(userOne.getId());
    statsUtility.delete(userTwo.getId());
  }
}
