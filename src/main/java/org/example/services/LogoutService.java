package org.example.services;

import lombok.AllArgsConstructor;

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
