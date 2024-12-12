package org.example.handlers.rest.getArchivedGame;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameUtility;
import org.example.enums.GameMode;
import org.example.exceptions.NotFound;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetArchivedGameService {
  @Builder.Default
  private final ArchivedGameUtility archivedGameUtility = new ArchivedGameUtility();

  ArchivedGame getArchivedGame(String gameId) throws NotFound {
    return archivedGameUtility.getGame(gameId);
  }

  List<ArchivedGame> getArchivedGames(String username, GameMode gameMode) {
    return archivedGameUtility.listArchivedGames(username, gameMode);
  }

  List<ArchivedGame> getArchivedGames(String username) {
    return archivedGameUtility.listArchivedGames(username);
  }
}
