package org.example.handlers.session;

import java.util.UUID;
import org.example.databases.MongoDBUtility;
import org.example.databases.SessionMongoDBUtility;
import org.example.requestRecords.SessionRequest;

public class SessionService {
  private final SessionMongoDBUtility dbUtility;

  public SessionService(SessionMongoDBUtility dbUtility) {
    this.dbUtility = dbUtility;
  }

  public SessionService() {
    this.dbUtility = new SessionMongoDBUtility(MongoDBUtility.getInstance("sessions"));
  }

  public String createSession(String userId) {
    // Generate a session token, possibly using UUID
    String token = UUID.randomUUID().toString();

    SessionRequest newSession = new SessionRequest(token, userId);
    // Store the session token in the database with an association to the userId
    dbUtility.post(newSession);

    return token;
  }

  public boolean isLoggedIn(String sessionId) {
    return dbUtility.get(sessionId) != null;
  }

  public void logOut(String sessionId) {
    dbUtility.delete(sessionId);
  }
}
