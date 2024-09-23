package org.example.utils.socketMessenger;

public interface SocketMessenger {
  void sendMessage(String connectionId, String message);

  void sendMessages(String connectionId, String connectionId2, String message);
}
