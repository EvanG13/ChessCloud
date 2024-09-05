package org.example.handlers.stats;

import com.google.gson.Gson;
import org.example.databases.MongoDBUtility;
import org.example.entities.Session;
import org.example.entities.User;

import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

public class StatsService {
    private final MongoDBUtility<User> dbUtility;

    public StatsService() {
        this.dbUtility = new MongoDBUtility<>("users", User.class);
    }

    public Optional<User> getByID(String id) {
        return dbUtility.get(id);
    }
}
