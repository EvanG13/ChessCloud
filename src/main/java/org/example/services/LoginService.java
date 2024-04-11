package org.example.services;

public class LoginService {

    private final String email;
    private final String password;

    public LoginService(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getResponseMessage() {
        return "Email : " + email + "\nPassword : " + password;
    }
}
