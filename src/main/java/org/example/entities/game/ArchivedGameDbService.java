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
import org.example.enums.GameStatus;
import org.example.enums.TimeControl;
import org.example.exceptions.BadRequest;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

@Builder
public class ArchivedGameDbService {
  @Builder.Default
  private final MongoDBUtility<ArchivedGame> archivedGameDbUtility =
      new MongoDBUtility<>("archived_games", ArchivedGame.class);

  public ArchivedGame archiveGame(@NonNull Game game, String winningUsername) {

    ArchivedPlayer one =
        archivePlayer(
            game.players.getFirst(), game.players.getFirst().getUsername().equals(winningUsername));
    ArchivedPlayer two =
        archivePlayer(
            game.players.getLast(), game.players.getLast().getUsername().equals(winningUsername));
    return ArchivedGame.builder()
        .id(game.getId())
        .created(new Date())
        .timeControl(game.getTimeControl())
        .moveList(new ArrayList<>(game.getMoveList()))
        .players(Arrays.asList(one, two))
        .rating(game.getRating())
        .numMoves(game.getMoveList().size())
        .build();
  }

  public ArchivedPlayer archivePlayer(@NonNull Player player, Boolean isWinner) {
    return ArchivedPlayer.builder()
        .playerId(player.getPlayerId())
        .username(player.getUsername())
        .isWhite(player.getIsWhite())
        .rating(player.getRating())
        .isWinner(isWinner)
        .build();
  }

  public void addFinishedGameToArchive(@NonNull ArchivedGame archivedGame) {
    archivedGameDbUtility.post(archivedGame);
  }

  public void addFinishedGameToArchive(@NonNull Game game, String winner) throws BadRequest {
    if (game.getGameStatus() != GameStatus.FINISHED) {
      throw new BadRequest("Can not archive a game that is not finished");
    }

    archivedGameDbUtility.post(archiveGame(game, winner));
  }

  public ArchivedGame getArchivedGame(String id) throws NotFound {
    return archivedGameDbUtility.get(id).orElseThrow(() -> new NotFound("No Archive Game found"));
  }

  public List<ArchivedGame> listArchivedGames(String userId, TimeControl timeControl) {
    System.out.println("time control: " + timeControl + "userid: " + userId);
    return archivedGameDbUtility.list(
        Filters.and(
            eq("timeControl", timeControl),
            Filters.elemMatch("players", Filters.eq("playerId", userId))));
  }

  public List<ArchivedGame> listArchivedGames(String userId) {
    Bson filter = Filters.elemMatch("players", Filters.eq("playerId", userId));
    return archivedGameDbUtility.list(filter);
  }

  public void deleteArchivedGame(String id) {
    archivedGameDbUtility.delete(id);
  }
}
