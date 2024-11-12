package org.example.handlers.websocket.timeout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.move.Move;
import org.example.entities.player.Player;
import org.example.enums.ResultReason;
import org.example.exceptions.BadRequest;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.exceptions.Unauthorized;
import org.example.handlers.websocket.gameOver.GameOverService;
import org.example.utils.socketMessenger.SocketMessenger;

import java.util.Date;
import java.util.List;


@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeoutService {
    @Builder.Default private final GameDbService gameDbService = new GameDbService();

    public void processTimeout(String gameId, String connectionId, SocketMessenger messenger)
            throws NotFound, InternalServerError, Unauthorized {
        Game game;
        try {
            game = gameDbService.get(gameId);
        } catch (NotFound e) {
            throw new NotFound("No Game found with id " + gameId);
        }


        Player reportingPlayer =
                game.getPlayers().stream()
                        .filter(player -> player.getConnectionId().equals(connectionId))
                        .findFirst()
                        .orElseThrow(() -> new Unauthorized("Your connection ID is not bound to this game"));
        // or Forbidden (or something) because if not found among the two players, that should mean they
        // aren't in the game?


        List<Player> players = game.getPlayers();

        Date lastModified = game.getLastModified();

        long t = ((new Date().getTime() - lastModified.getTime())) / 1000; // convert to seconds from millis


        Player activePlayer;
        boolean isWhiteTurn = game.getIsWhitesTurn();
        if ((players.getFirst().getIsWhite() && isWhiteTurn)
                || (!players.getFirst().getIsWhite() && !isWhiteTurn)) {
            activePlayer = players.getFirst();
        } else {
            activePlayer = players.getLast();
        }
        activePlayer.setRemainingTime((int) (activePlayer.getRemainingTime() - t));

        Player timedOutPlayer;

        if(players.getFirst().getRemainingTime() < 1){
            timedOutPlayer = players.getFirst();
        }
        else if(players.getLast().getRemainingTime() < 1){
            timedOutPlayer = players.getLast();
        }
        else {
            throw new NotFound("Not a valid timeout");
        }

        if((players.getFirst().getRemainingTime() < 1) && (players.getLast().getRemainingTime() < 1)){
            throw new InternalServerError("both players have no remaining time...");
            //maybe make this a draw? It should never happen.
        }

        GameOverService service =
                new GameOverService(ResultReason.TIMEOUT, game, timedOutPlayer.getPlayerId(), messenger);

        service.endGame();
    }
}
