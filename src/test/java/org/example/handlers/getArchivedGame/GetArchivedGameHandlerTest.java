package org.example.handlers.getArchivedGame;

import static org.example.utils.HttpTestUtils.assertResponse;
import static org.example.utils.TestUtils.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameUtility;
import org.example.entities.game.Game;
import org.example.entities.timeControl.TimeControl;
import org.example.enums.GameStatus;
import org.example.enums.ResultReason;
import org.example.handlers.rest.getArchivedGame.GetArchivedGameHandler;
import org.example.utils.MockContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GetArchivedGameHandlerTest {
  private static ArchivedGameUtility archivedGameUtility;
  private static ArchivedGame expected;
  private static GetArchivedGameHandler handler;

  private static String gameId;

  @BeforeAll
  public static void setUp() throws Exception {
    archivedGameUtility = new ArchivedGameUtility();

    Game game = validGame(new TimeControl(300, 0));

    gameId = game.getId();

    game.setGameStatus(GameStatus.FINISHED);

    expected = archivedGameUtility.toArchivedGame(game, "user1", ResultReason.CHECKMATE);
    handler = new GetArchivedGameHandler();

    archivedGameUtility.post(expected);
  }

  @AfterAll
  public static void tearDown() {
    archivedGameUtility.delete(gameId);
  }

  @Test
  public void canGetArchivedGame() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder().withPathParameters(Map.of("gameId", gameId)).build();

    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertResponse(response, StatusCodes.OK, expected.toResponseJson());
  }

  @Test
  public void missingPathParams() {
    APIGatewayV2HTTPResponse response =
        handler.handleRequest(new APIGatewayV2HTTPEvent(), new MockContext());
    assertResponse(response, StatusCodes.BAD_REQUEST, "No path params");
  }

  @Test
  public void missingGameIdFromQueryParams() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withPathParameters(Map.of("userid", "not-a-game-id!"))
            .build();

    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertResponse(response, StatusCodes.BAD_REQUEST, "Missing gameId from path params");
  }
}
