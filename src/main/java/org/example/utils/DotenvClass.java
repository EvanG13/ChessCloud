package org.example.utils;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.experimental.UtilityClass;

/**
 * Access .env variables in other files like: String myEnvValue =
 * DotenvClass.dotenv.get("ENVIRONMENT_KEY");
 */
@UtilityClass
public final class DotenvClass {
  public static Dotenv dotenv = Dotenv.load();
}
