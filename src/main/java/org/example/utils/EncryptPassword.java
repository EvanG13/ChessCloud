package org.example.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class EncryptPassword {
  public static String encrypt(String input) {
    return BCrypt.withDefaults().hashToString(12, input.toCharArray());
  }

  public static boolean verify(String plain, String hashed) {
    return BCrypt.verifyer().verify(plain.toCharArray(), hashed).verified;
  }
}
