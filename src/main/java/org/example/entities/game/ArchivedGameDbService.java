package org.example.entities.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import lombok.Builder;
import lombok.NonNull;
import org.example.entities.player.ArchivedPlayer;
import org.example.entities.player.Player;
import org.example.enums.GameStatus;
import org.example.exceptions.BadRequest;
import org.example.utils.MongoDBUtility;

@Builder
public class ArchivedGameDbService {
  @Builder.Default
  private MongoDBUtility<ArchivedGame> archivedGameDbService =
      new MongoDBUtility<>("archived_games", ArchivedGame.class);

  private ArchivedGame archiveGame(@NonNull Game game) {
    boolean didPlayerOneWin = false;
    boolean didPlayerTwoWin = false;
    ArchivedPlayer one = archivePlayer(game.players.getFirst(), didPlayerOneWin);
    ArchivedPlayer two = archivePlayer(game.players.getFirst(), didPlayerTwoWin);
    return ArchivedGame.builder()
        .id(game.getId())
        .created(new Date())
        .timeControl(game.getTimeControl())
        .moveList(new ArrayList<>(game.getMoveList()))
        .players(Arrays.asList(one, two))
        .rating(game.getRating())
        .build();
  }

  private ArchivedPlayer archivePlayer(@NonNull Player player, Boolean isWinner) {
    return ArchivedPlayer.builder()
        .playerId(player.getPlayerId())
        .username(player.getUsername())
        .isWhite(player.getIsWhite())
        .rating(player.getRating())
        .isWinner(isWinner)
        .build();
  }

  public void addFinishedGameToArchive(@NonNull ArchivedGame archivedGame) {
    archivedGameDbService.post(archivedGame);
  }

  public void addFinishedGameToArchive(@NonNull Game game) throws BadRequest {
    if (game.getGameStatus() != GameStatus.FINISHED) {
      throw new BadRequest("Can not archive a game that is not finished");
    }

    archivedGameDbService.post(archiveGame(game));
  }
}
