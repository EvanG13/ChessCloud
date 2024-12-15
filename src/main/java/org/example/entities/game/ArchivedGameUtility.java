package org.example.entities.game;

import static com.mongodb.client.model.Filters.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import org.example.entities.player.ArchivedPlayer;
import org.example.entities.player.Player;
import org.example.enums.GameMode;
import org.example.enums.ResultReason;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

public class ArchivedGameUtility extends MongoDBUtility<ArchivedGame> {
  public ArchivedGameUtility() {
    super("archived_games", ArchivedGame.class);
  }

  public ArchivedGameUtility(String collection) {
    super(collection, ArchivedGame.class);
  }

  public ArchivedGame getGame(String id) throws NotFound {
    return get(id).orElseThrow(() -> new NotFound("No Archived Game found"));
  }

  public List<ArchivedGame> listArchivedGames(String username, GameMode gameMode) {
    return list(and(eq("gameMode", gameMode), elemMatch("players", eq("username", username))));
  }

  public List<ArchivedGame> listArchivedGames(String username) {
    return list(elemMatch("players", eq("username", username)));
  }

  public ArchivedGame toArchivedGame(
      @NonNull Game game, String winningUsername, ResultReason resultReason) {
    ArchivedPlayer one =
        toArchivedPlayer(
            game.players.getFirst(), game.players.getFirst().getUsername().equals(winningUsername));
    ArchivedPlayer two =
        toArchivedPlayer(
            game.players.getLast(), game.players.getLast().getUsername().equals(winningUsername));
    return ArchivedGame.builder()
        .id(game.getId())
        .created(new Date())
        .timeControl(game.getTimeControl())
        .moveList(new ArrayList<>(game.getMoveList()))
        .players(Arrays.asList(one, two))
        .rating(game.getRating())
        .numMoves(game.getMoveList().size())
        .resultReason(resultReason)
        .gameMode(game.getGameMode())
        .build();
  }

  public ArchivedPlayer toArchivedPlayer(@NonNull Player player, Boolean isWinner) {
    return ArchivedPlayer.builder()
        .playerId(player.getPlayerId())
        .username(player.getUsername())
        .isWhite(player.getIsWhite())
        .rating(player.getRating())
        .isWinner(isWinner)
        .build();
  }

  public void archiveGame(@NonNull Game game, String winner, ResultReason resultReason) {
    post(toArchivedGame(game, winner, resultReason));
  }
}
