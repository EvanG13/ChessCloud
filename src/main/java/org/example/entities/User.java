package org.example.entities;

import org.bson.Document;
import org.bson.types.ObjectId;

public class User extends DataTransferObject {
    private final String email;
    private final String password;

    public User(String id, String email, String password) {
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

    public static User fromDocument(Document userDocument) {
        return new User(
                String.valueOf(userDocument.getObjectId("_id")),
                userDocument.getString("email"),
                userDocument.getString("password")
        );
    }

    @Override
    public String toString() {
        return "email " + email + " - " + "Password " + password + "\n";
    }
}
