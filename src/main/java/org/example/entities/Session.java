package org.example.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Session extends DataTransferObject {
    @Expose String userId;

    public Session(String id, String userId) {
        super(id);
        this.userId = userId;
    }

    public static Session fromDocument(Document sessionDocument) {
        return new Session(
                    sessionDocument.getString("_id"),
                    sessionDocument.getString("userId"));
    }

    public Document toDocument() {
        return new Document("_id", new ObjectId(id));
    }

    public String toString(){
        return this.id + " " + this.userId;
    }

    @Override
    public String toResponseJson() {
        Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        return gsonBuilder.toJson(this, Session.class);
    }
}

