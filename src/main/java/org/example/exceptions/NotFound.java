package org.example.exceptions;

import org.example.constants.StatusCodes;

public class NotFound extends StatusCodeException {

  public NotFound(String message) {
    super(StatusCodes.NOT_FOUND, message);
  }
}
