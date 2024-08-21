package org.example.databases;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.entities.User;
import org.example.handlers.session.SessionService;
import org.example.requestRecords.UserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public class SessionMongoDBUtilityTest {
    final String email = "foo@gmail.com";
    final String password = "foo";

    final ObjectId objectId = new ObjectId();
    MongoDBUtility mockedUtility;
    private UsersMongoDBUtility utility;
    private SessionMongoDBUtility sessionUtility;
    private User fakeUser;
    private String token;

    private SessionService sessionService;
    @BeforeEach
    public void setUp() {
        mockedUtility = mock(MongoDBUtility.class);

        utility = new UsersMongoDBUtility(mockedUtility);
        sessionUtility = new SessionMongoDBUtility(mockedUtility);

        doNothing().when(mockedUtility).post(any(Document.class));

        UserRequest userData = new UserRequest(email, "evan", password);

        utility.post(userData);
    }

    @Test
    public void canCreateSession() {
        sessionService = new SessionService();
        token = sessionService.createSession("123456789123456789000000");

        assertEquals(token, sessionUtility.get(token).getId());
    }

    @AfterEach
    public void tearDown() {
        sessionUtility.delete(token);
        sessionService = null;
        sessionUtility = null;
        token = null;
    }
}