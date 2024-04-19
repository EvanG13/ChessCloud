package org.example.handlers.register;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.example.databases.users.UsersDynamoDBUtility;
import org.example.entities.User;
import org.example.handlers.TestContext;
import org.example.requestRecords.UserRequest;
import org.example.statusCodes.StatusCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RegisterHandlerTest {
    private RegisterHandler registerHandler;
    private UsersDynamoDBUtility dbUtility;
    @BeforeEach
    void setUp(){
        dbUtility = mock(UsersDynamoDBUtility.class);

        RegisterService service = new RegisterService(dbUtility);

        registerHandler = new RegisterHandler(service);
    }

    @DisplayName("OK 👍")
    @Test
    void returnSuccess(){
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

        event.setBody( """
         {
                  "email": "test@gmail.com",
                  "username": "testuser",
                  "password": "test"
         }""");

        Context context = new TestContext();

        when(dbUtility.getByEmail(anyString()))
                .thenReturn(null);
        doNothing().when(dbUtility).post(any(UserRequest.class));
        APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);

        //assertTrue(response.getBody().contains("JWT"));
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }

    @DisplayName("Bad Request 😠")
    @Test
    void returnBadRequest(){
        Context context = new TestContext();

        when(dbUtility.getByEmail(anyString())).thenReturn(null);
        APIGatewayV2HTTPResponse response = registerHandler.handleRequest(null, context);

        assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    }

    @DisplayName("Conflict 🔀")
    @Test
    void returnConflict(){
        Context context = new TestContext();
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody( """
         {
                  "email": "test@gmail.com",
                  "username": "testuser",
                  "password": "test"
         }""");

        when(dbUtility.getByEmail(anyString())).thenReturn(new User("1","test@gmail.com", "test","testuser"));
        APIGatewayV2HTTPResponse response = registerHandler.handleRequest(event, context);

        assertEquals(StatusCodes.CONFLICT, response.getStatusCode());
    }
}
