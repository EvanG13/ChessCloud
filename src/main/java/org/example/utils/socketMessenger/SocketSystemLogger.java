package org.example.utils.socketMessenger;

public class SocketSystemLogger implements SocketMessenger {

  @Override
  public void sendMessage(String connectionId, String message) {
    System.out.println("connectionId: " + connectionId + " message " + message);
  }

  @Override
  public void sendMessages(String connectionId, String connectionId2, String message) {
    System.out.println(
        "connectionId: "
            + connectionId
            + " connectionId2: "
            + connectionId2
            + " message: "
            + message);
  }
}
