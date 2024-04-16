package org.example.databases.mongoDB;

import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.databases.MongoDBUtility;
import org.example.databases.mongoDB.UsersMongoDBUtility;
import org.example.entities.User;
import org.example.requestRecords.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsersMongoDBUtilityTest {
    private UsersMongoDBUtility utility;
    MongoDBUtility mockedUtility;

    final String email = "foo@gmail.com";
    final String password = "foo";
    final ObjectId objectId = new ObjectId();

    @BeforeEach
    public void setUp() {
        mockedUtility = mock(MongoDBUtility.class);

        utility = new UsersMongoDBUtility(mockedUtility);

        doNothing().when(mockedUtility).post(any(Document.class));

        UserRequest userData = new UserRequest(email, password);
        utility.post(userData);
    }

    @Test
    public void canGetUser() {
        Document document = new Document("_id", objectId)
                .append("email", email)
                .append("password", password);

        when(mockedUtility.get(anyString())).thenReturn(document);

        User user = utility.get("foo-id");
        assertNotNull(user);
        assertEquals(user.getEmail(), email);
        assertEquals(user.getPassword(), password);
        assertEquals(user.getId(), objectId.toString());
    }

    @Test
    public void canDeleteUser() {
        doNothing().when(mockedUtility).delete(anyString());
        utility.delete(objectId.toString());

        when(mockedUtility.get(anyString())).thenReturn(null);
        User user = utility.get(objectId.toString());
        assertNull(user);
    }

    @Test
    public void canPatchUser() {
        User newUser = new User("brother", "hello@gmail.com", "world");

        doNothing().when(mockedUtility).post(any(Document.class));

        doNothing().when(mockedUtility).patch(anyString(), any(Bson.class));

        utility.patch("brother", Updates.set("email", "another@gmail.com"));
    }
}
