package org.example.exceptions;

import org.example.constants.StatusCodes;

public class InternalServerError extends StatusCodeException {
  public InternalServerError(String message) {
    super(StatusCodes.INTERNAL_SERVER_ERROR, message);
  }
}
