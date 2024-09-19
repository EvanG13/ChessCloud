package org.example.exceptions;

import org.example.constants.StatusCodes;

public class Unauthorized extends StatusCodeException {
  public Unauthorized(String message) {
    super(StatusCodes.UNAUTHORIZED, message);
  }
}
