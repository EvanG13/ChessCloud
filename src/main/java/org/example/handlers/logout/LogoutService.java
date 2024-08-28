package org.example.handlers.logout;

import com.google.gson.Gson;
import org.example.handlers.session.SessionService;

public class LogoutService {
  private SessionService service;

  public LogoutService() {
    service = new SessionService();
  }

  public String getMessage() {
    return "Logged out successfully.";
  }

  public void destroySession(String sessionToken) {
    service.logOut(sessionToken);
  }
}
