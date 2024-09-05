package org.example.handlers.logout;

import lombok.AllArgsConstructor;
import org.example.handlers.session.SessionService;

@AllArgsConstructor
public class LogoutService {
  private final SessionService service;

  public LogoutService() {
    service = new SessionService();
  }

  public void logout(String sessionToken) {
    service.delete(sessionToken);
  }
}
