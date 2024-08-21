package org.example.databases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.entities.User;
import org.example.requestRecords.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UsersMongoDBUtilityTest {
  final String email = "foo@gmail.com";
  final String password = "foo";
  final ObjectId objectId = new ObjectId();
  MongoDBUtility mockedUtility;
  private UsersMongoDBUtility utility;

  @BeforeEach
  public void setUp() {
    mockedUtility = mock(MongoDBUtility.class);

    utility = new UsersMongoDBUtility(mockedUtility);

    doNothing().when(mockedUtility).post(any(Document.class));

    UserRequest userData = new UserRequest(email, "evan", password);
    utility.post(userData);
  }

  @Test
  public void canGetUserByEmailAndPassword() {
    Document document =
        new Document("_id", objectId)
            .append("email", email)
            .append("password", password)
            .append("username", "test");

    when(mockedUtility.get(any(Bson.class))).thenReturn(document);

    User user = utility.getByEmail(email);
    assertNotNull(user);
    assertEquals(user.getEmail(), document.get("email"));
    assertEquals(user.getPassword(), document.get("password"));
    assertEquals(user.getUsername(), document.get("username"));
    assertEquals(user.getId(), document.getObjectId("_id").toString());
  }

  @Test
  public void canGetUser() {
    Document document =
        new Document("_id", objectId).append("email", email).append("password", password);

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
    User newUser = new User("brother", "hello@gmail.com", "world", "username");

    doNothing().when(mockedUtility).post(any(Document.class));

    doNothing().when(mockedUtility).patch(anyString(), any(Bson.class));

    utility.patch("brother", Updates.set("email", "another@gmail.com"));
  }
}
