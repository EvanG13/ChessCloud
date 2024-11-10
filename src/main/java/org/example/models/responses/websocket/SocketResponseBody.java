package org.example.models.responses.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.enums.WebsocketResponseAction;
import org.example.models.responses.rest.ResponseBody;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SocketResponseBody<T extends SocketMessageData> extends ResponseBody {
  protected WebsocketResponseAction websocketResponseAction;
  protected T data;
}
