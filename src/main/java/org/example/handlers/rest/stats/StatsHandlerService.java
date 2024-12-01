package org.example.handlers.rest.stats;

import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsDbService;
import org.example.exceptions.NotFound;

public class StatsHandlerService {
  private final StatsDbService statsDbService;

  public StatsHandlerService(StatsDbService statsDbService) {
    this.statsDbService = statsDbService;
  }

  public StatsHandlerService() {
    this.statsDbService = new StatsDbService();
  }

  public boolean doesCategoryExist(String category) {
    return statsDbService.doesCategoryExist(category);
  }

  public Stats getStatsByUsername(String username) throws NotFound {
    return statsDbService.getStatsByUsername(username);
  }

  public Stats getStatsByUsernameAndCategory(String username, String category) throws NotFound {
    return statsDbService.getStatsByUsernameAndCategory(username, category);
  }
}
