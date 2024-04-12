package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Tag("dependency-check")
public class AWSSDKDependencyTest {

    AwsCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();

    @DisplayName("Checks if Lambda Runtime Dependency is installed")
    @Test
    void testLambdaClientDependency() {
        try {
            Class.forName("com.amazonaws.services.lambda.runtime.Context");


            assertTrue(true, "Lambda runtime dependency is available");
        } catch (ClassNotFoundException e) {
            // If the import fails, the dependency is not available
            fail("Dependency not installed");
        }
    }

    @DisplayName("Checks if S3 Client Dependency is installed")
    @Test
    void testS3ClientDependency() {
        final S3Client s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .build();

        assertNotNull(s3Client);

        s3Client.close();
    }

    @DisplayName("Checks if API Gateway Client Dependency is installed")
    @Test
    void testAPIGatewayClientDependency() {
        final ApiGatewayClient apiGatewayClient = ApiGatewayClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .build();

        assertNotNull(apiGatewayClient);

        apiGatewayClient.close();
    }

    @DisplayName("Checks if DynamoDB Client Dependency is installed")
    @Test
    void testDynamoDBClientDependency() {
        final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .build();

        assertNotNull(dynamoDbClient);

        dynamoDbClient.close();
    }
}
