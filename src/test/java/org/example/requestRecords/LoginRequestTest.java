package org.example.requestRecords;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.apache.commons.logging.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginRequestTest {
    @Test
    public void canCreateLoginRequestObject(){
        String user = "{\"email\":\"123@gmail.com\",\"password\":\"123\"}";
        Gson gson = new Gson();
        LoginRequest newLR = gson.fromJson(user, LoginRequest.class);

        assertEquals(newLR.email(), "123@gmail.com");
        assertEquals(newLR.password(), "123");
    }
}
