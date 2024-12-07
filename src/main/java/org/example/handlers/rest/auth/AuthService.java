package org.example.handlers.rest.auth;

import java.util.Optional;
import org.example.entities.session.Session;
import org.example.entities.session.SessionUtility;
import org.example.entities.user.UserUtility;
import org.example.models.requests.AuthRequest;

public class AuthService {

  public boolean isValidSession(AuthRequest data) {
    UserUtility userUtility = new UserUtility();
    SessionUtility sessionUtility = new SessionUtility();

    if (userUtility.get(data.userId()).isEmpty()) {
      return false;
    }

    Optional<Session> optionalSession = sessionUtility.get(data.sessionId());
    if (optionalSession.isEmpty()) {
      return false;
    }

    Session session = optionalSession.get();

    return session.getUserId().equals(data.userId());
  }
}
