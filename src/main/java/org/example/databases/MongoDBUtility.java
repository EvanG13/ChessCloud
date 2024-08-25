package org.example.databases;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.entities.DataTransferObject;

public class MongoDBUtility<T extends DataTransferObject> {

  private final MongoClient client;
  private final MongoDatabase database;
  private final Class<T> tClass;
  private final String collectionName;

  public MongoDBUtility(String collectionName, Class<T> tClass) {

    this.collectionName = collectionName;
    this.tClass = tClass;

    Dotenv dotenv = Dotenv.load();
    final String connectionString = dotenv.get("MONGODB_CONNECTION_STRING");

    CodecRegistry pojoCodecRegistry =
        fromProviders(PojoCodecProvider.builder().automatic(true).build());
    CodecRegistry codecRegistry =
        fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
    MongoClientSettings clientSettings =
        MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .codecRegistry(codecRegistry)
            .build();

    try {
      this.client = MongoClients.create(clientSettings);
      this.database = client.getDatabase("chess");
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private MongoCollection<T> getCollection() {
    return database.getCollection(collectionName, tClass);
  }

  public void createIndex(String field) {
    getCollection().createIndex(Indexes.ascending(field), new IndexOptions().unique(true));
  }

  public Optional<T> get(String id) {
    return Optional.ofNullable(getCollection().find(eq("_id", id)).first());
  }

  public Optional<T> get(ObjectId id) {
    return Optional.ofNullable(getCollection().find(eq("_id", id)).first());
  }

  public Optional<T> get(Bson filter) {
    return Optional.ofNullable(getCollection().find(filter).first());
  }

  public void post(T object) {
    getCollection().insertOne(object);
  }

  public List<T> list(Bson filter) {
    return getCollection().find(filter).into(new ArrayList<>());
  }

  public void patch(String id, Bson filter) {
    getCollection().updateOne(new Document("_id", id), filter);
  }

  public void patch(ObjectId id, Bson filter) {
    getCollection().updateOne(new Document("_id", id), filter);
  }

  public void delete(String id) {
    getCollection().findOneAndDelete(eq("_id", id));
  }

  public void delete(String index, String indexValue) {
    getCollection().findOneAndDelete(eq(index, indexValue));
  }

  public void delete() {
    getCollection().deleteMany(new Document());
  }
}
