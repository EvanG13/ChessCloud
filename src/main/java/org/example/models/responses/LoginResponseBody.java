package org.example.models.responses;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.entities.User;

@Getter
@AllArgsConstructor
public class LoginResponseBody extends ResponseBody {
  @Expose String token;
  @Expose User user;
}
