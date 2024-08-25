package org.example.utils;

import io.github.cdimascio.dotenv.Dotenv;

// Singleton class to prevent multiple env loads.
// Access .env variables in other files like: String myEnvValue =
// DotenvClass.dotenv.get("ENVIRONMENT_KEY");
public class DotenvClass {
  public static Dotenv dotenv = Dotenv.load();

  private DotenvClass() {}
}
