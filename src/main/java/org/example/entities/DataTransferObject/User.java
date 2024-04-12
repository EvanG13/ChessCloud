package org.example.entities.DataTransferObject;

import org.bson.Document;
import org.bson.types.ObjectId;

public class User extends DataTransferObject {
    private final String email;
    private final String password;

    public User(String email, String password, String id) {
        super(id);
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public Document toDocument() {
        return new Document("_id", new ObjectId(id))
                .append("email", email)
                .append("password", password);
    }

    public static User toObject(Document user) {
        return new User(
                user.getString("email"),
                user.getString("password"),
                String.valueOf(user.getObjectId("_id"))
        );
    }
}
