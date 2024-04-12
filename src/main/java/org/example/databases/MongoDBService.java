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

public class MongoDBService {

    final String DATABASE_NAME = "Chess";

    private MongoCollection<org.bson.Document> collection;

    public MongoDBService(String collectionName) {
        Dotenv dotenv = Dotenv.load();
        final String connectionString = dotenv.get("MONGODB_CONNECTION_STRING");

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

                collection = database.getCollection(collectionName);
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param id
     * @return Document
     */
    public Document get(String id) {
        return collection.find(eq("_id", new ObjectId(id))).first();
    }

    /**
     *
     * @param obj
     */
    public void post(Document obj) {
        collection.insertOne(obj);
    }

    /**
     *
     * @param filter
     * @return
     */
    public List<Document> list(Bson filter) {
        return collection.find(eq(filter)).into(new ArrayList<>());
    }

    /**
     *
     * @param filter
     * @param data
     */
    public void patch(Bson filter, Bson data) {
        collection.updateOne(filter, data);
    }

    /**
     * Delete by id
     * @param id ObjectId
     */
    public void delete(String id) {
        collection.findOneAndDelete(eq("_id", new ObjectId(id)));
    }
}

