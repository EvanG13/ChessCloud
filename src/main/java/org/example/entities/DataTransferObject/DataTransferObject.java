package org.example.entities.DataTransferObject;

import org.bson.Document;

public abstract class DataTransferObject {
    protected String id;

    public DataTransferObject(String id) {
        this.id = id;
    }

    /**
     * Convert object to a BSON Object
     * @return BSON Object
     */
    public abstract Document toDocument();
}
