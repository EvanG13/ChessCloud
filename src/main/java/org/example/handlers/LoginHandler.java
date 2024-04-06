package org.example.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.example.services.LoginService;

public class LoginHandler implements RequestHandler<Object, String> {

    private final LoginService service;

    public LoginHandler() {
        service = new LoginService();
    }

    @Override
    public String handleRequest(Object input, Context context) {
        System.out.println(input);
        return input != null ? service.getResponseMessage() : "super duper updated";
    }
}
