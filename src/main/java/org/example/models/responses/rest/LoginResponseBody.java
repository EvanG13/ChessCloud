package org.example.models.responses.rest;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.entities.user.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseBody extends ResponseBody {
  @Expose String token;
  @Expose User user;
}
