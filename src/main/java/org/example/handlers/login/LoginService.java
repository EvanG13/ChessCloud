package org.example.handlers.login;

public class LoginService {

    private final String email;
    private final String password;

    public LoginService(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getResponseMessage() {
        return "New Input email : " + email + "\nNew Input Password : " + password;
    }
}
