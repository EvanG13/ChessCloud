package org.example.utils.socketMessenger;

public interface SocketMessenger {
  public abstract void sendMessage(String connectionId, String message);

  public abstract void sendMessages(String connectionId, String connectionId2, String message);
}
