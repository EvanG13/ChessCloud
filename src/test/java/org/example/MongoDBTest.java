package org.example;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.bson.Document;

import static org.junit.jupiter.api.Assertions.*;

public class MongoDBTest {

    @DisplayName("Test dotenv")
    @Test
    void testDotenv() {

        assertTrue(Files.exists(Paths.get(".env")),
                "⚠\uFE0F Please create a .env file containing your mongodb connection string and mongodb port number ⚠\uFE0F"
        );

        Dotenv dotenv = Dotenv.load();

        final String connectionString = dotenv.get("MONGODB_CONNECTION_STRING");
        assertNotNull(
                connectionString,
                "⚠\uFE0F MongoDB connection string is null. Make sure to run 'mvn install' and set the environment variable 'MONGODB_CONNECTION_STRING' in your .env file. ⚠\uFE0F"
        );

        final String port = dotenv.get("PORT");
        assertNotNull(
                port,
                "⚠\uFE0F MongoDB connection string is null. Make sure to run 'mvn install' and set the environment variable 'PORT' in your .env file. ⚠\uFE0F"
        );
    }


    @DisplayName("Test MongoDB connection")
    @Test
    void testMongoDBConnection() {
        Dotenv dotenv = Dotenv.load();

        final String connectionString = dotenv.get("MONGODB_CONNECTION_STRING");

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("admin");
                Document pingResult = database.runCommand(new Document("ping", 1));

                assertNotNull(pingResult);
                assertEquals(1, pingResult.getInteger("ok"), "Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
}
