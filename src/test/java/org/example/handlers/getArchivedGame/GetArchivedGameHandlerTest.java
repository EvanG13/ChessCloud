package org.example.handlers.getArchivedGame;

import static org.example.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.HashMap;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameDbService;
import org.example.entities.game.Game;
import org.example.enums.GameStatus;
import org.example.enums.ResultReason;
import org.example.enums.TimeControl;
import org.example.handlers.rest.getArchivedGame.GetArchivedGameHandler;
import org.example.utils.MockContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GetArchivedGameHandlerTest {
  private static ArchivedGameDbService archivedGameDbService;
  private static ArchivedGame expected;
  private static GetArchivedGameHandler handler;

  private static String gameId;

  @BeforeAll
  public static void setUp() throws Exception {
    archivedGameDbService = ArchivedGameDbService.builder().build();

    Game game = validGame(TimeControl.BLITZ_5);

    gameId = game.getId();

    game.setGameStatus(GameStatus.FINISHED);

    expected = archivedGameDbService.toArchivedGame(game, "user1", ResultReason.CHECKMATE);
    handler = new GetArchivedGameHandler();

    archivedGameDbService.archiveGame(expected);
  }

  @AfterAll
  public static void tearDown() {
    archivedGameDbService.deleteArchivedGame(gameId);
  }

  @Test
  public void canGetArchivedGame() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("gameId", gameId);

    event.setPathParameters(pathParams);
    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    String actual = response.getBody();
    assertEquals(expected.toResponseJson(), actual);
  }

  @Test
  public void missingPathParams() {
    APIGatewayV2HTTPResponse response =
        handler.handleRequest(new APIGatewayV2HTTPEvent(), new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals("No path params", response.getBody());
  }

  @Test
  public void missingGameIdFromQueryParams() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setPathParameters(new HashMap<>());

    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals("Missing gameId from path params", response.getBody());
  }
}
