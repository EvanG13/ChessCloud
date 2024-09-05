package org.example.handlers.session;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.example.databases.MongoDBUtility;
import org.example.entities.Session;
import org.example.requestRecords.SessionRequest;

@AllArgsConstructor
public class SessionService {

  private final MongoDBUtility<Session> dbUtility;

  public SessionService() {
    this.dbUtility = new MongoDBUtility<>("sessions", Session.class);
  }

  public String createSession(SessionRequest data) {
    String sessionId = UUID.randomUUID().toString();

    // Store the session token in the database with an association to the userId
    Session session = Session.builder().id(sessionId).userId(data.userId()).build();
    dbUtility.post(session);

    return sessionId;
  }

  public void delete(String sessionId) {
    dbUtility.delete(sessionId);
  }
}
