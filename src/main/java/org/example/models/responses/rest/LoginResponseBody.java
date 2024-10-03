package org.example.models.responses.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.entities.user.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseBody extends ResponseBody {
  String token;
  User user;
}
