package org.example.handlers.message;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.nio.ByteBuffer;
import org.example.requestRecords.MessageRequest;
import org.example.statusCodes.StatusCodes;
import org.example.utils.DotenvClass;

public class MessageHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final AmazonApiGatewayManagementApi apiClient;

  private String apiEndpoint = DotenvClass.dotenv.get("WEB_SOCKET_BACKEND_ENDPOINT");
  private String region = DotenvClass.dotenv.get("AWS_REGION");

  public MessageHandler() {
    this.apiClient =
        AmazonApiGatewayManagementApiClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(apiEndpoint, region))
            .build();
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {

    // TODO eventually try fetching all the players in the game room's socket ids and
    // send a message to all of them

    APIGatewayV2WebSocketEvent.RequestContext requestContext = event.getRequestContext();
    APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();

    if (requestContext == null || requestContext.getConnectionId() == null) {
      System.err.println("Invalid event: missing requestContext or connectionId");
      response.setStatusCode(StatusCodes.BAD_REQUEST);
      return response;
    }
    String connectionId = requestContext.getConnectionId();
    try {
      Gson gson = new Gson();
      MessageRequest message = gson.fromJson(event.getBody(), MessageRequest.class);
      sendMessage(connectionId, message.data());
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
      return response;
    }

    response.setStatusCode(StatusCodes.OK);
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
