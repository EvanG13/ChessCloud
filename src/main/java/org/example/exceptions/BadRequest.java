package org.example.exceptions;

import org.example.constants.StatusCodes;

public class BadRequest extends StatusCodeException {

  public BadRequest(String message) {
    super(StatusCodes.BAD_REQUEST, message);
  }
}
