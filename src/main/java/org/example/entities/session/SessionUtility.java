package org.example.entities.session;

import java.util.UUID;
import org.example.exceptions.NotFound;
import org.example.models.requests.SessionRequest;
import org.example.utils.MongoDBUtility;

public class SessionUtility extends MongoDBUtility<Session> {
  public SessionUtility() {
    super("sessions", Session.class);
  }

  public SessionUtility(String collection) {
    super(collection, Session.class);
  }

  public String createSession(SessionRequest data) {
    String sessionId = UUID.randomUUID().toString();

    post(Session.builder().id(sessionId).userId(data.userId()).build());

    return sessionId;
  }

  public void deleteByUserId(String userId) {
    delete("userId", userId);
  }

  public Session getSession(String id) throws NotFound {
    return get(id).orElseThrow(() -> new NotFound("Session Not Found"));
  }
}
