package org.example.handlers.auth;

import java.util.Optional;
import org.example.databases.MongoDBUtility;
import org.example.entities.Session;
import org.example.requestRecords.AuthRequest;

public class AuthService {

  public boolean isValidSession(AuthRequest data) {
    MongoDBUtility<Session> utility = new MongoDBUtility<>("sessions", Session.class);

    Optional<Session> optionalSession = utility.get(data.sessionId());
    if (optionalSession.isEmpty()) {
      return false;
    }

    Session session = optionalSession.get();

    return session.getUserId().equals(data.userId());
  }
}
