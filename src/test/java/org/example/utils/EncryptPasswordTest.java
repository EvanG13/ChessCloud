package org.example.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptPasswordTest {
    @Test
    public void canEncryptPassword(){
        String password = "newpassword";
        String encryptedPass = EncryptPassword.encrypt(password);

        assertNotNull(encryptedPass);

        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), encryptedPass);
        assertTrue(result.verified);
    }
}
