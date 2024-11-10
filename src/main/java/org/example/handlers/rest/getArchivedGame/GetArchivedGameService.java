package org.example.handlers.rest.getArchivedGame;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameDbService;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetArchivedGameService {
  @Builder.Default
  private final ArchivedGameDbService archivedGameDbService =
      ArchivedGameDbService.builder().build();

  ArchivedGame getArchivedGame(String gameId) throws NotFound {
    return archivedGameDbService.getArchivedGame(gameId);
  }

  List<ArchivedGame> getArchivedGames(String username, TimeControl timeControl) {
    return archivedGameDbService.listArchivedGames(username, timeControl);
  }

  List<ArchivedGame> getArchivedGames(String username) {
    return archivedGameDbService.listArchivedGames(username);
  }
}
