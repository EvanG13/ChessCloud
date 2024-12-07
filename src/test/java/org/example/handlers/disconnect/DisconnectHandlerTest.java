package org.example.handlers.disconnect;

import static org.example.utils.WebsocketTestUtils.getResponse;
import static org.example.utils.WebsocketTestUtils.makeRoutelessRequestContext;
import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.Optional;
import org.example.constants.StatusCodes;
import org.example.entities.connection.Connection;
import org.example.entities.connection.ConnectionUtility;
import org.example.handlers.websocket.disconnect.DisconnectHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DisconnectHandlerTest {
  public static String username;
  public static String id;
  public static ConnectionUtility connectionUtility;

  @BeforeAll
  public static void setUp() {
    username = "foo-username";
    id = "connection-id";

    connectionUtility = new ConnectionUtility();

    connectionUtility.post(Connection.builder().id(id).username(username).build());
  }

  @DisplayName("OK ✅")
  @Test
  public void returnSuccess() {
    Optional<Connection> connection = connectionUtility.get(id);
    assertTrue(connection.isPresent());

    assertEquals(connection.get().toString(), username + " " + id);

    APIGatewayV2WebSocketResponse response =
        getResponse(new DisconnectHandler(), "", makeRoutelessRequestContext(id));

    assertEquals(response.getStatusCode(), StatusCodes.OK);

    Optional<Connection> previousRecord = connectionUtility.get(id);
    assertTrue(previousRecord.isEmpty());
  }
}
