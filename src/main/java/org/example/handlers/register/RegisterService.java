package org.example.handlers.register;

import org.example.databases.DynamoDBUtility;
import org.example.databases.users.UsersDynamoDBUtility;
import org.example.entities.User;
import org.example.requestRecords.UserRequest;
import org.example.utils.EncryptPassword;

public class RegisterService {
    private final UsersDynamoDBUtility utility;

    public RegisterService(UsersDynamoDBUtility dbUtility) {
        this.utility = dbUtility;
    }

    public RegisterService() {
        this.utility = new UsersDynamoDBUtility(DynamoDBUtility.create("users"));
    }

    public boolean doesEmailExist(String email){
        User user = utility.getByEmail(email);
        if (user == null) {
            return false;
        }
        return true;
    }

    public void registerUser(String email, String username, String password){
        UserRequest newUser = new UserRequest(email,username, EncryptPassword.encrypt(password));

        utility.post(newUser);
    }
}
