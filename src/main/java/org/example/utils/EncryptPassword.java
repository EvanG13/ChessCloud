package org.example.utils;
import at.favre.lib.crypto.bcrypt.BCrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

public class EncryptPassword {
    public static String encrypt( String input) {
        String bcryptHashString = BCrypt.withDefaults().hashToString(12, input.toCharArray());
        return bcryptHashString;
    }
}

//throws NoSuchPaddingException, NoSuchAlgorithmException,
//            InvalidAlgorithmParameterException, InvalidKeyException,
//            BadPaddingException, IllegalBlockSizeException
