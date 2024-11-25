package org.example.entities.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.example.entities.DataTransferObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseToken extends DataTransferObject {
  private String token; // hashed
  private String userId;
  private Date expiresAt;

  public boolean isExpired() {
    return new Date().after(this.expiresAt);
  }

  @Override
  public String toString() {
    StringBuilder sb =
        new StringBuilder()
            .append("id        = ")
            .append(id)
            .append("\ntoken     = ")
            .append(token)
            .append("\nuserId    = ")
            .append(userId)
            .append("\nexpiresAt = ")
            .append(expiresAt);

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseToken object = (BaseToken) o;
    return Objects.equals(id, object.getId())
        && Objects.equals(token, object.getToken())
        && Objects.equals(userId, object.getUserId())
        && Objects.equals(expiresAt, object.getExpiresAt());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, token, userId, expiresAt);
  }

  public static String generateToken() {
    try {
      SecureRandom random = SecureRandom.getInstanceStrong();

      byte[] bytes = new byte[32];
      random.nextBytes(bytes);

      return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    catch (NoSuchAlgorithmException e) {
      // should never happen
      return new ObjectId().toString();
    }
  }

  public static String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashedBytes = digest.digest(token.getBytes());

      StringBuilder hexString = new StringBuilder(2 * hashedBytes.length);
      for (byte b : hashedBytes) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    }
    catch (NoSuchAlgorithmException e) {
      // should never happen
      return token;
    }
  }

  public static Date makeExpirationDate(int field, int amount) {
    Calendar c = Calendar.getInstance();
    c.add(field, amount);
    return c.getTime();
  }
}
