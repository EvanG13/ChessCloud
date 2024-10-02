package org.example.utils;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("Integration")
public class BaseTest {
  @BeforeAll
  public static void setup() {
    RestAssured.baseURI = DotenvClass.dotenv.get("REST_BACKEND_ENDPOINT");
    RestAssured.basePath = DotenvClass.dotenv.get("AWS_STAGE");

    RestAssured.config =
        RestAssuredConfig.config()
            .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails());
  }
}
