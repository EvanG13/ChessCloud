package org.example.utils.socketMessenger;

import java.net.URI;
import org.example.utils.DotenvClass;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

public class SocketEmitter implements SocketMessenger {
  private final String apiEndpoint = DotenvClass.dotenv.get("WEB_SOCKET_BACKEND_ENDPOINT");
  private final String region = DotenvClass.dotenv.get("AWS_REGION");
  private final ApiGatewayManagementApiClient apiClient;

  public SocketEmitter() {
    apiClient =
        ApiGatewayManagementApiClient.builder()
            .endpointOverride(URI.create(apiEndpoint)) // Use endpointOverride for v2
            .region(Region.of(region))
            .build();
  }

  public void sendMessage(String connectionId, String message) {
    try {
      PostToConnectionRequest request =
          PostToConnectionRequest.builder()
              .connectionId(connectionId)
              .data(SdkBytes.fromUtf8String(message))
              .build();

      apiClient.postToConnection(request);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  public void sendMessages(String connectionId, String secondConnectionId, String message) {
    try {
      sendMessage(connectionId, message);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

    try {
      sendMessage(secondConnectionId, message);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}
