package org.example.databases;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.utils.DotenvClass;

public class MongoDBUtility implements DatabaseUtility<Document, Bson> {

  private static MongoDBUtility instance;
  final String DATABASE_NAME = "chess";
  private final MongoDatabase database;
  private final MongoClient client;
  private final String collectionName;
  private MongoCollection<org.bson.Document> collection;

  public MongoDBUtility(String collectionName) {

    final String connectionString = DotenvClass.dotenv.get("MONGODB_CONNECTION_STRING");

    try {
      client = MongoClients.create(connectionString);

      database = client.getDatabase(DATABASE_NAME);
      this.collectionName = collectionName;
    } catch (MongoException e) {
      e.printStackTrace();
      throw e;
    }
  }

  public static synchronized MongoDBUtility getInstance(String collectionName) {
    if (instance == null) {
      instance = new MongoDBUtility(collectionName);
    }

    return instance;
  }

  public void createIndex(String field) {
    collection.createIndex(Indexes.ascending(field), new IndexOptions().unique(true));
  }

  /**
   * Get Document by an object id
   *
   * @param id id
   * @return Document
   */
  @Override
  public Document get(String id) {

    collection = database.getCollection(collectionName);

    return collection.find(eq("_id", new ObjectId(id))).first();
  }

  public Document get(Bson filter) {
    collection = database.getCollection(collectionName);

    return collection.find(filter).first();
  }

  @Override
  public void post(Document document) {
    collection = database.getCollection(collectionName);
    collection.insertOne(document);
  }

  @Override
  public List<Document> list(Bson filter) {
    collection = database.getCollection(collectionName);
    return collection.find(filter).into(new ArrayList<>());
  }

  @Override
  public void patch(String id, Bson filter) {
    collection = database.getCollection(collectionName);

    collection.updateOne(new Document("_id", new ObjectId(id)), filter);
  }

  @Override
  public void delete(String id) {
    collection = database.getCollection(collectionName);

    collection.findOneAndDelete(eq("_id", new ObjectId(id)));
  }

  public void deleteByIndex(String index, String indexValue) {
    collection = database.getCollection(collectionName);

    collection.findOneAndDelete(eq(index, indexValue));
  }

  public void delete() {
    collection.deleteMany(new Document());
  }
}
