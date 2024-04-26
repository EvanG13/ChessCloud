package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Date;

public class JWTUtils {

  private final String ISSUER = "chess";
  private final String secreteKey;

  public JWTUtils() {
    Dotenv dotenv = Dotenv.configure().filename(".env.prod").load();

    secreteKey = dotenv.get("SECRETE_KEY");
  }

  public String generateJWT(String email) throws JWTCreationException {

    Algorithm algorithm = Algorithm.HMAC256(secreteKey);

    // 1 hour from the current time
    Date expirationTime = new Date(System.currentTimeMillis() + 3600 * 1000);

    return JWT.create()
        .withIssuer(ISSUER)
        .withSubject(email)
        .withIssuedAt(new Date())
        .withExpiresAt(expirationTime)
        .sign(algorithm);
  }

  public boolean verifyJWT(String token) {

    try {
      Algorithm algorithm = Algorithm.HMAC256(secreteKey);

      JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();

      DecodedJWT decodedJWT = verifier.verify(token);

      if (!decodedJWT.getIssuer().equals(ISSUER)) {
        throw new JWTVerificationException("Issuer does not match expected issuer");
      }

      return true;
    } catch (JWTVerificationException e) {
      return false;
    }
  }
}
