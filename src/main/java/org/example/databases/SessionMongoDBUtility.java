package org.example.databases;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.entities.Session;
import org.example.entities.User;
import org.example.requestRecords.SessionRequest;
import org.example.requestRecords.UserRequest;

public class SessionMongoDBUtility {
    private final MongoDBUtility utility;

    public SessionMongoDBUtility() {
        utility = MongoDBUtility.getInstance("sessions");
    }

    public SessionMongoDBUtility(MongoDBUtility utility) {
        this.utility = utility;
    }

    /**
     * Get Session by sessionId, returns null if sessionId is invalid
     *
     * @param id sessionId
     * @return Session
     */
    public Session get(String id) {
        Document doc = utility.get(id);
        if (doc == null) {
            return null;
        }

        return Session.fromDocument(doc);
    }


    /**
     * Create a new Session
     *
     * @param sessionData session request data object
     */
    public void post(SessionRequest sessionData) {
        utility.post(
                new Document("_id", new ObjectId(sessionData.id()))
                        .append("userId", sessionData.userId()));
    }

    /**
     * Deletes a User by their id
     *
     * @param id id
     */
    public void delete(String id) {
        utility.delete(id);
    }
}
