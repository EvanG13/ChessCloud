package org.example.entities;

import com.google.gson.annotations.Expose;
import org.bson.Document;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public abstract class DataTransferObject {

  @Expose protected String id;

  /**
   * !Please do NOT remove this. The DynamoDB enhanced client requires having a default constructor
   */
  public DataTransferObject() {}

  public DataTransferObject(String id) {
    this.id = id;
  }

  @DynamoDbPartitionKey
  @DynamoDbAttribute(value = "id")
  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Convert object to a BSON Document
   *
   * @return BSON Document
   */
  public abstract Document toDocument();

  /**
   * Converts the Object to a Json string
   *
   * @return Json string
   */
  public abstract String toResponseJson();
}
