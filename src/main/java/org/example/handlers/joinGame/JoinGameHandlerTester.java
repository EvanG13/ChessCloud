package org.example.handlers.joinGame;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.Optional;
import org.example.entities.Game;
import org.example.entities.Player;
import org.example.entities.User;
import org.example.requestRecords.JoinGameRequest;
import org.example.statusCodes.StatusCodes;

public class JoinGameHandlerTester
        implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

    private final JoinGameService service;

    public JoinGameHandlerTester() {
        service = new JoinGameService();
    }

    public JoinGameHandlerTester(JoinGameService service) {
        this.service = service;
    }

    /**
     * @param event { <br>
     *     action: "joinGame", "userId": "fooid", "timeControl": "BLITZ_5" <br>
     *     }
     * @return 409 if a player is already connected to a game <br>
     *     200 if a game was found, and we were able to join <br>
     *     201 if no game was found to join thus we created a game and are awaiting a second player
     *     This is a testing class not for actual use.
     *     It differs from the actual class in that it does not send clients messages
     *     And the returned response sends just the Id of the game that was created.
     */
    @Override
    public APIGatewayV2WebSocketResponse handleRequest(
            APIGatewayV2WebSocketEvent event, Context context) {
        APIGatewayV2WebSocketEvent.RequestContext requestContext = event.getRequestContext();
        APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();

        if (requestContext == null || requestContext.getConnectionId() == null) {
            System.err.println("Invalid event: missing requestContext or connectionId");
            response.setStatusCode(StatusCodes.BAD_REQUEST);
            return response;
        }

        String connectionId = requestContext.getConnectionId();

        // TODO : Check if the player is already connected to a game

        Gson gson = new Gson();
        JoinGameRequest joinRequestData = gson.fromJson(event.getBody(), JoinGameRequest.class);

        String userId = joinRequestData.userId();
        Optional<User> optionalUser = service.getUser(userId);
        if (optionalUser.isEmpty()) {
            response.setStatusCode(StatusCodes.UNAUTHORIZED);

            return response;
        }

        User user = optionalUser.get();
        Optional<Game> optionalGame =
                service.getPendingGame(joinRequestData.timeControl(), user.getRating());

        Player newPlayer =
                Player.builder()
                        .playerId(userId)
                        .connectionId(connectionId)
                        .username(user.getUsername())
                        .rating(user.getRating())
                        .build();

        if (optionalGame.isEmpty()) {
            // No pending game for the requested time control
            // Create new game with requested time control

            Game newGame = new Game(joinRequestData.timeControl(), newPlayer);
            service.createGame(newGame);

            response.setStatusCode(StatusCodes.CREATED);
            response.setBody(newGame.getId());
        } else {
            // Pending game exists for the requested time control
            // Join pending game

            Game game = optionalGame.get();
            try {
                game.setup(newPlayer);
            } catch (Exception e) {
                response.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
                return response;
            }

            service.updateGame(game);

            // Notify both players that the game is
            String gameJson = game.toResponseJson();


            response.setBody(game.getId());
            response.setStatusCode(StatusCodes.OK);
        }

        return response;
    }
}
