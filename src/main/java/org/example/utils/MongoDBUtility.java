package org.example.utils;

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
import com.mongodb.client.model.ReplaceOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.example.entities.DataTransferObject;

public class MongoDBUtility<T extends DataTransferObject> {
  private static final MongoClient sharedClient;
  private static final String databaseName = "chess";

  static {
    String connectionString = DotenvClass.dotenv.get("MONGODB_CONNECTION_STRING");

    CodecRegistry pojoCodecRegistry =
        fromProviders(PojoCodecProvider.builder().automatic(true).build());
    CodecRegistry codecRegistry =
        fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
    MongoClientSettings clientSettings =
        MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .codecRegistry(codecRegistry)
            .build();

    sharedClient = MongoClients.create(clientSettings);
  }

  private final MongoDatabase database;
  private final Class<T> tClass;
  private final String collectionName;

  public MongoDBUtility(String collectionName, Class<T> tClass) {
    this.collectionName = collectionName;
    this.tClass = tClass;
    this.database = sharedClient.getDatabase(databaseName);
  }

  private MongoCollection<T> getCollection() {
    return database.getCollection(collectionName, tClass);
  }

  public void createIndex(String field) {
    createIndex(field, true);
  }

  public void createIndex(String field, boolean unique) {
    getCollection().createIndex(Indexes.ascending(field), new IndexOptions().unique(unique));
  }

  public void createCompoundIndex(List<String> fields, String indexName, boolean unique) {
    List<Bson> indexFields = fields.stream().map(Indexes::ascending).toList();
    Bson compoundIndex = Indexes.compoundIndex(indexFields);

    getCollection().createIndex(compoundIndex, new IndexOptions().name(indexName).unique(unique));
  }

  public Optional<T> get(String id) {
    return Optional.ofNullable(getCollection().find(eq("_id", id)).first());
  }

  public Optional<T> get(Bson filter) {
    return Optional.ofNullable(getCollection().find(filter).first());
  }

  public Optional<T> get(Bson filter, Bson projection) {
    return Optional.ofNullable(getCollection().find(filter).projection(projection).first());
  }

  public void post(T object) {
    getCollection().insertOne(object);
  }

  public List<T> list(Bson filter) {
    return getCollection().find(filter).into(new ArrayList<>());
  }

  public void patch(String id, Bson filter) {
    getCollection().updateOne(eq("_id", id), filter);
  }

  public void put(String id, T object) {
    ReplaceOptions options = new ReplaceOptions().upsert(true);
    getCollection().replaceOne(eq("_id", id), object, options);
  }

  public void replace(String id, T object) {
    getCollection().replaceOne(eq("_id", id), object);
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
