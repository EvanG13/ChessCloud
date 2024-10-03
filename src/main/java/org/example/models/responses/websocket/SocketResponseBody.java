package org.example.models.responses.websocket;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.enums.Action;
import org.example.models.responses.rest.ResponseBody;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SocketResponseBody<T extends SocketMessageData> extends ResponseBody {
  @Expose protected Action action;
  @Expose protected T data;
}