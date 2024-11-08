package org.example.entities.game;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import org.bson.conversions.Bson;
import org.example.entities.player.ArchivedPlayer;
import org.example.entities.player.Player;
import org.example.enums.ResultReason;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

@Builder
public class ArchivedGameDbService {
  @Builder.Default
  private final MongoDBUtility<ArchivedGame> archivedGameDbUtility =
      new MongoDBUtility<>("archived_games", ArchivedGame.class);

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

  public void archiveGame(@NonNull ArchivedGame archivedGame) {
    archivedGameDbUtility.post(archivedGame);
  }

  public void archiveGame(@NonNull Game game, String winner, ResultReason resultReason) {
    archivedGameDbUtility.post(toArchivedGame(game, winner, resultReason));
  }

  public ArchivedGame getArchivedGame(String id) throws NotFound {
    return archivedGameDbUtility.get(id).orElseThrow(() -> new NotFound("No Archive Game found"));
  }

  public List<ArchivedGame> listArchivedGames(String username, TimeControl timeControl) {
    return archivedGameDbUtility.list(
        Filters.and(
            eq("timeControl", timeControl),
            Filters.elemMatch("players", Filters.eq("username", username))));
  }

  public List<ArchivedGame> listArchivedGames(String username) {
    Bson filter = Filters.elemMatch("players", Filters.eq("username", username));
    return archivedGameDbUtility.list(filter);
  }

  public void deleteArchivedGame(String id) {
    archivedGameDbUtility.delete(id);
  }
}
