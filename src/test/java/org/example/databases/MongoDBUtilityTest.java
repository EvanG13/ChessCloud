package org.example.databases;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.MongoException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MongoDBUtilityTest {
  private static final String COLLECTION_NAME = "testCollection";

  MongoDBUtility service;
  Document newUser;

  @BeforeEach
  public void setUp() {
    service = new MongoDBUtility(COLLECTION_NAME);

    newUser =
        new Document("_id", new ObjectId())
            .append("email", "test-foo-email@test.com")
            .append("password", "test-fod");

    service.post(newUser);
  }

  @AfterEach
  public void tearDown() {
    service.list(new Document()).forEach(doc -> service.delete(doc.getObjectId("_id").toString()));
  }

  @DisplayName("Can create an index")
  @Test
  public void createIndex() {
    service.createIndex("email");
  }

  @DisplayName("Can get a document from MongoDB \uD83E\uDD8D")
  @Test
  public void getDocument() {
    try {
      Document actual = service.get(newUser.getObjectId("_id").toString());
      assertNotNull(actual);

      assertEquals(newUser.getObjectId("_id"), actual.getObjectId("_id"));
      assertEquals(newUser.get("email"), actual.get("email"));
      assertEquals(newUser.get("password"), actual.get("password"));

    } catch (MongoException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can delete a document from MongoDB \uD83E\uDD8D")
  @Test
  public void deleteDocument() {

    try {
      service.delete(newUser.getObjectId("_id").toString());

      Document actual = service.get(newUser.getObjectId("_id").toString());
      assertNull(actual);
    } catch (MongoException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can update a document from MongoDB \uD83E\uDD8D")
  @Test
  public void patchDocument() {

    try {
      service.patch(
          newUser.getObjectId("_id").toString(), Updates.set("email", "new-fake-email@gmail.com"));

      Document actual = service.get(newUser.getObjectId("_id").toString());
      assertNotNull(actual);

      assertEquals(actual.get("email"), "new-fake-email@gmail.com");

      System.out.println(actual);
    } catch (MongoException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can list documents from MongoDB \uD83E\uDD8D")
  @Test
  public void listDocuments() {

    Document document1 =
        new Document("_id", new ObjectId())
            .append("email", "twosecond-email@test.com")
            .append("password", "list");
    Document document2 =
        new Document("_id", new ObjectId())
            .append("email", "third-email@test.com")
            .append("password", "list");

    try {
      service.post(document1);
      service.post(document2);

      List<Document> actualDocs = service.list(Filters.eq("password", "list"));

      assertNotNull(actualDocs);
      assertEquals(2, actualDocs.size());
    } catch (MongoException e) {
      e.printStackTrace();
      fail("fail");
    }
  }
}
