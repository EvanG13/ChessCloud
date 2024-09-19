package org.example.databases;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.MongoException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.example.entities.User;
import org.example.utils.MongoDBUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MongoDBUtilityTest {
  private static final String COLLECTION_NAME = "testCollection";

  MongoDBUtility<User> service;
  User newUser;

  @BeforeEach
  public void setUp() {
    service = new MongoDBUtility<>(COLLECTION_NAME, User.class);

    newUser =
        User.builder()
            .id(new ObjectId().toString())
            .email("test-foo-email@test.com")
            .username("test-username")
            .password("test-password")
            .build();

    service.post(newUser);
  }

  @AfterEach
  public void tearDown() {
    service.delete();
  }

  @DisplayName("Can create an index")
  @Test
  public void createIndex() {
    service.createIndex("email");
  }

  @DisplayName("Can get a Object from MongoDB \uD83E\uDD8D")
  @Test
  public void getDocument() {
    try {
      Optional<User> actual = service.get(newUser.getId());
      assertTrue(actual.isPresent());

      assertEquals(newUser, actual.get());

    } catch (MongoException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can delete a document from MongoDB \uD83E\uDD8D")
  @Test
  public void deleteDocument() {

    try {
      service.delete(newUser.getId());

      Optional<User> actual = service.get(newUser.getId());
      assertTrue(actual.isEmpty());
    } catch (MongoException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can update a document from MongoDB \uD83E\uDD8D")
  @Test
  public void patchDocument() {
    final String newEmail = "new-fake-email@gmail.com";
    try {
      service.patch(newUser.getId(), Updates.set("email", newEmail));

      Optional<User> optionalUser = service.get(newUser.getId());
      assertTrue(optionalUser.isPresent());

      User actual = optionalUser.get();
      assertEquals(actual.getId(), newUser.getId());
      assertEquals(actual.getEmail(), newEmail);
    } catch (MongoException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can list documents from MongoDB \uD83E\uDD8D")
  @Test
  public void listDocuments() {

    User listUser1 =
        User.builder()
            .id(new ObjectId().toString())
            .email("twosecond-email@test.com")
            .password("list")
            .username("second")
            .build();

    User listUser2 =
        User.builder()
            .id(new ObjectId().toString())
            .email("third-email@test.com")
            .password("list")
            .username("third")
            .build();

    try {
      service.post(listUser1);
      service.post(listUser2);

      List<User> actualDocs = service.list(Filters.eq("password", "list"));

      assertNotNull(actualDocs);
      assertEquals(2, actualDocs.size());
    } catch (MongoException e) {
      e.printStackTrace();
      fail("fail");
    }
  }
}
