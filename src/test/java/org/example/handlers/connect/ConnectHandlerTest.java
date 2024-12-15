package org.example.handlers.connect;

import static org.example.utils.WebsocketTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.example.constants.StatusCodes;
import org.example.entities.game.Game;
import org.example.entities.game.GameUtility;
import org.example.entities.player.Player;
import org.example.entities.timeControl.TimeControl;
import org.example.exceptions.NotFound;
import org.example.handlers.websocket.connect.ConnectHandler;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConnectHandlerTest {

  public static String userId;
  public static String userId2;
  public static String connectId;
  public static String connectId2;
  public static String newConnId;
  public static String gameId;
  public static GameUtility gameUtility;

  @BeforeAll
  public static void setUp() throws NotFound {
    userId = "test-connection";
    userId2 = "user2";
    connectId = "connection-id";
    connectId2 = "second-connection-id";
    newConnId = "connection-id2";
    gameUtility = new GameUtility();
    Player player = Player.builder().playerId(userId).connectionId(connectId).build();
    Player player2 = Player.builder().playerId(userId2).connectionId(connectId2).build();
    Game newGame = new Game(new TimeControl(300, 0), player);
    try {
      newGame.setup(player2);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    gameId = newGame.getId();
    gameUtility.post(newGame);
  }

  @AfterAll
  public static void tearDown() {
    gameUtility.delete(gameId);
  }

  @DisplayName("OK ✅")
  @Test
  @Order(1)
  public void returnSuccess() {
    Map<String, String> queryStrings = new HashMap<>();
    queryStrings.put("userid", userId);

    APIGatewayV2WebSocketResponse response =
        getResponse(new ConnectHandler(), "", queryStrings, makeRoutelessRequestContext(connectId));

    assertEquals(StatusCodes.OK, response.getStatusCode());
  }

  @DisplayName("Updated ConnectionId 😘😘")
  @Test
  @Order(2)
  public void updateSuccessful() {
    Map<String, String> queryStrings = new HashMap<>();
    queryStrings.put("userid", userId);

    APIGatewayV2WebSocketResponse response =
        getResponse(new ConnectHandler(), "", queryStrings, makeRequestContext("", newConnId));

    assertEquals(StatusCodes.OK, response.getStatusCode());

    Optional<Game> oGame = gameUtility.get(gameId);
    assertFalse(oGame.isEmpty());
    Game game = oGame.get();
    assertEquals(newConnId, game.getPlayers().get(0).getConnectionId());
    assertEquals(connectId2, game.getPlayers().get(1).getConnectionId());
  }

  @DisplayName("Updated Other Player's ConnectionId 😘😘")
  @Test
  @Order(3)
  public void updateSecondPlayerSuccessful() {
    Map<String, String> queryStrings = new HashMap<>();
    queryStrings.put("userid", userId2);

    APIGatewayV2WebSocketResponse response =
        getResponse(new ConnectHandler(), "", queryStrings, makeRequestContext("", connectId));

    assertEquals(StatusCodes.OK, response.getStatusCode());

    Optional<Game> oGame = gameUtility.get(gameId);
    assertFalse(oGame.isEmpty());
    Game game = oGame.get();
    assertEquals(newConnId, game.getPlayers().get(0).getConnectionId());
    assertEquals(connectId, game.getPlayers().get(1).getConnectionId());
  }
}
