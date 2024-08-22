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
import org.example.utils.DotenvClass;

public class MessageHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final AbstractAmazonApiGatewayManagementApi apiClient;
  //TODO move this endpoint into .env file

  private String apiEndpoint = DotenvClass.dotenv.get("WEB_SOCKET_BACKEND_ENDPOINT");
  private String region = DotenvClass.dotenv.get("AWS_REGION");

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
