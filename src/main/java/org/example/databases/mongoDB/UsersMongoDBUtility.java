package org.example.databases.mongoDB;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.databases.MongoDBUtility;
import org.example.entities.User;
import org.example.requestRecords.UserRequest;

public class UsersMongoDBUtility {
    private final MongoDBUtility utility;

    // TODO : https://github.com/EvanG13/ChessCloud/issues/10
    public UsersMongoDBUtility(MongoDBUtility utility) {
        this.utility = utility;
    }

    /**
     * Get a User by their id
     * @param id id
     * @return user
     */
    public User get(String id) {
        Document doc = utility.get(id);
        if (doc == null) {
            return null;
        }

        return User.fromDocument(doc);
    }

    /**
     * Create a new User
     * @param userData user request data object
     */
    public void post(UserRequest userData) {
        utility.post(new Document("_id", new ObjectId())
                .append("email", userData.email())
                .append("password", userData.password())
        );
    }

    /**
     * Deletes a User by their id
     * @param id id
     */
    public void delete(String id) {
        utility.delete(id);
    }

    /**
     * Update a user with the given id
     * @param id object id
     * @param filter filter
     */
    public void patch(String id, Bson filter) {
        utility.patch(id, filter);
    }
}
