package org.example.utils;

import static org.junit.jupiter.api.Assertions.*;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EncryptPasswordTest {

  @DisplayName("Password Encryption")
  @Test
  public void canEncryptAndVerifyPassword() {
    String password = "testPassword";
    String encryptedPass = EncryptPassword.encrypt(password);

    System.out.println(encryptedPass);

    assertNotNull(encryptedPass);

    BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), encryptedPass);
    assertTrue(result.verified);
  }
}
