package org.example.entities.session;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.example.exceptions.NotFound;
import org.example.models.requests.SessionRequest;
import org.example.utils.MongoDBUtility;

@AllArgsConstructor
public class SessionDbService {

  private final MongoDBUtility<Session> dbUtility;

  public SessionDbService() {
    this.dbUtility = new MongoDBUtility<>("sessions", Session.class);
  }

  public String createSession(SessionRequest data) {
    String sessionId = UUID.randomUUID().toString();

    Session session = Session.builder().id(sessionId).userId(data.userId()).build();
    dbUtility.post(session);

    return sessionId;
  }

  public void createSession(Session session) {
    dbUtility.post(session);
  }

  public Session get(String id) throws NotFound {
    return dbUtility.get(id).orElseThrow(() -> new NotFound("Session Not Found"));
  }

  public void deleteByUserId(String userid) {
    dbUtility.delete("userId", userid);
  }

  public void delete(String sessionId) {
    dbUtility.delete(sessionId);
  }
}
