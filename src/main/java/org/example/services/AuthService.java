package org.example.services;

import java.util.Optional;
import org.example.entities.User;
import org.example.entities.session.Session;
import org.example.models.requests.AuthRequest;
import org.example.utils.MongoDBUtility;

public class AuthService {

  public boolean isValidSession(AuthRequest data) {
    MongoDBUtility<User> userUtility = new MongoDBUtility<>("users", User.class);
    Optional<User> optionalUser = userUtility.get(data.userId());
    if (optionalUser.isEmpty()) {
      return false;
    }

    MongoDBUtility<Session> sessionUtility = new MongoDBUtility<>("sessions", Session.class);

    Optional<Session> optionalSession = sessionUtility.get(data.sessionId());
    if (optionalSession.isEmpty()) {
      return false;
    }

    Session session = optionalSession.get();

    return session.getUserId().equals(data.userId());
  }
}
