package org.example.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class EncryptPassword {
  public static String encrypt(String input) {
    return BCrypt.withDefaults().hashToString(12, input.toCharArray());
  }
}
