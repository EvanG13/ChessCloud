package org.example.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HttpTestUtils {

  public static void assertResponse(
      APIGatewayV2HTTPResponse response, int expectedStatusCode, String expectedBody) {
    assertEquals(
        expectedStatusCode, response.getStatusCode(), "Status code does not match expected value");
    assertEquals(expectedBody, response.getBody(), "Response body does not match expected value");
  }
}
