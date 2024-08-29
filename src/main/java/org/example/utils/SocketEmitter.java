package org.example.utils;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import java.nio.ByteBuffer;

public class SocketEmitter {
  private final String apiEndpoint = DotenvClass.dotenv.get("WEB_SOCKET_BACKEND_ENDPOINT");
  private final String region = DotenvClass.dotenv.get("AWS_REGION");
  private final AmazonApiGatewayManagementApi apiClient;

  public SocketEmitter() {
    apiClient =
        AmazonApiGatewayManagementApiClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(apiEndpoint, region))
            .build();
  }

  public void sendMessage(String connectionId, String message) {

    PostToConnectionRequest request =
        new PostToConnectionRequest()
            .withConnectionId(connectionId)
            .withData(ByteBuffer.wrap(message.getBytes()));

    apiClient.postToConnection(request);
  }

  public void sendMessages(String connectionId, String secondConnectionId, String message) {

    PostToConnectionRequest request =
        new PostToConnectionRequest()
            .withConnectionId(connectionId)
            .withConnectionId(secondConnectionId)
            .withData(ByteBuffer.wrap(message.getBytes()));

    apiClient.postToConnection(request);
  }
}
