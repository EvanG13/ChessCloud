package org.example.handlers.message;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AbstractAmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.nio.ByteBuffer;

public class MessageHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final AbstractAmazonApiGatewayManagementApi apiClient;
  private String apiEndpoint = "https://4wre5to3yc.execute-api.us-east-1.amazonaws.com/dev";
  private String region = "us-east-1";

  public MessageHandler() {
    this.apiClient =
        (AbstractAmazonApiGatewayManagementApi)
            AmazonApiGatewayManagementApiClientBuilder.standard()
                .withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(apiEndpoint, region))
                .build();
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {
    String connectionId = event.getRequestContext().getConnectionId();
    String message = "Hello, this is a message from Lambda!";

    try {
      sendMessage(connectionId, message);
    } catch (Exception e) {
      e.printStackTrace();
    }

    APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();
    response.setStatusCode(200); // Respond with HTTP 200 OK
    return response;
  }

  private void sendMessage(String connectionId, String message) throws Exception {
    PostToConnectionRequest request =
        new PostToConnectionRequest()
            .withConnectionId(connectionId)
            .withData(ByteBuffer.wrap(message.getBytes()));

    apiClient.postToConnection(request);
  }
}
