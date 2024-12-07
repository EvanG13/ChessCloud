package org.example.handlers.rest.stats;

import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.exceptions.NotFound;

public class StatsHandlerService {
  private final StatsUtility statsUtility;

  public StatsHandlerService(StatsUtility statsUtility) {
    this.statsUtility = statsUtility;
  }

  public StatsHandlerService() {
    this.statsUtility = new StatsUtility();
  }

  public boolean doesCategoryExist(String category) {
    return statsUtility.doesGameModeExist(category);
  }

  public Stats getStatsByUsername(String username) throws NotFound {
    return statsUtility.getStatsByUsername(username);
  }

  public Stats getStatsByUsernameAndCategory(String username, String category) throws NotFound {
    return statsUtility.getStatsByUsernameAndCategory(username, category);
  }
}
