package org.example.databases;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBUtility implements DatabaseUtility<Document, Bson> {

    final String DATABASE_NAME = "chess";

    private final MongoDatabase database;

    private final MongoClient client;

    private MongoCollection<org.bson.Document> collection;

    private final String collectionName;

    private static MongoDBUtility instance;

    public MongoDBUtility(String collectionName) {
        Dotenv dotenv = Dotenv.load();
        final String connectionString = dotenv.get("MONGODB_CONNECTION_STRING");

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

    @Override
    public Document get(String id) {

        collection = database.getCollection(collectionName);

        return collection.find(eq("_id", new ObjectId(id))).first();
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
//    @Override
//    public void patch(String id, Bson filter) {
//        collection = database.getCollection(collectionName);
//
//        collection.updateOne(new Document("_id", new ObjectId(id)), filter);
//    }

    @Override
    public void delete(String id) {
        collection = database.getCollection(collectionName);

        collection.findOneAndDelete(eq("_id", new ObjectId(id)));
    }
}


