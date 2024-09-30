package org.example.entities.session;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.example.models.requests.SessionRequest;
import org.example.utils.MongoDBUtility;

@AllArgsConstructor
public class SessionService {

  private final MongoDBUtility<Session> dbUtility;

  public SessionService() {
    this.dbUtility = new MongoDBUtility<>("sessions", Session.class);
  }

  public String createSession(SessionRequest data) {
    String sessionId = UUID.randomUUID().toString();

    Session session = Session.builder().id(sessionId).userId(data.userId()).build();
    dbUtility.post(session);

    return sessionId;
  }

  public void delete(String sessionId) {
    dbUtility.delete(sessionId);
  }
}
