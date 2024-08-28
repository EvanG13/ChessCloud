package org.example.handlers.logout;

import org.example.handlers.session.SessionService;

public class LogoutService {
  private SessionService service;

  public LogoutService() {
    service = new SessionService();
  }

  public LogoutService(SessionService service) {
    this.service = service;
  }

  public String getMessage() {
    return "Logged out successfully.";
  }

  public void destroySession(String sessionToken) {
    service.logOut(sessionToken);
  }
}
