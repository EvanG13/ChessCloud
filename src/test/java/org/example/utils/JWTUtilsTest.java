package org.example.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JWTUtilsTest {

  Dotenv dotenv;
  JWTUtils jwtUtils;
  String secreteKey;

  @BeforeEach
  public void setUp() {
    jwtUtils = new JWTUtils();
    dotenv = Dotenv.configure().filename(".env.prod").load();

    secreteKey = dotenv.get("SECRETE_KEY");
  }

  @Test
  public void canGenerateJWT() {

    String jwt = jwtUtils.generateJWT("foo@gmail.com");

    assertNotNull(jwt);
    assertTrue(jwtUtils.verifyJWT(jwt));
  }

  @Test
  public void canDetectInvalidJWT() {
    String invalidJWT = "invalidjwt";

    assertFalse(jwtUtils.verifyJWT(invalidJWT), "Not a valid jwt");

    Algorithm algorithm = Algorithm.HMAC256(secreteKey);

    Date expirationTime = new Date();
    String token =
        JWT.create()
            .withIssuer("chess")
            .withSubject("foo@gmail.com")
            .withIssuedAt(new Date())
            .withExpiresAt(expirationTime)
            .sign(algorithm);
    assertFalse(jwtUtils.verifyJWT(token), "Token is out of date");
  }

  @Test
  public void canDetectOutOfDatedJWT() {
    Algorithm algorithm = Algorithm.HMAC256(secreteKey);

    Date expirationTime = new Date();
    String token =
        JWT.create()
            .withIssuer("chess")
            .withSubject("foo@gmail.com")
            .withIssuedAt(new Date())
            .withExpiresAt(expirationTime)
            .sign(algorithm);
    assertFalse(jwtUtils.verifyJWT(token), "Token is out of date");
  }

  @Test
  public void canDetectInvalidJWTIssuer() {
    Algorithm algorithm = Algorithm.HMAC256(secreteKey);

    Date expirationTime = new Date(System.currentTimeMillis() + 3600 * 1000);
    String token =
        JWT.create()
            .withIssuer("invalidissuer")
            .withSubject("foo@gmail.com")
            .withIssuedAt(new Date())
            .withExpiresAt(expirationTime)
            .sign(algorithm);
    assertFalse(jwtUtils.verifyJWT(token), "Issuer is invalid");
  }
}
