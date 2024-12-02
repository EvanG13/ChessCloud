# ChessCloud

# Project Setup

This project utilizes **[Apache Maven](https://maven.apache.org/)** for managing `Java` dependencies

### Install Dependencies

with tests: ``mvn clean package``\
skip tests: ``mvn clean package -DskipTests``

### Steps for adding a dependency

1. Search for and select your desired package within [MVN Repository](https://mvnrepository.com/)
2. Copy the package's `Dependecy Declaration`:
   ```xml
   <dependency>
      <groupId>foo.id</groupId>
      <artifactId>foo-artifact-id</artifactId>
      <version>5.10.2</version>
   </dependency>
   ```
3. Open up the `pom.xml` file at the `root` of the project
4. Paste the new dependency at the bottom of the `<dependencies>` section
5. ``mvn clean package``
6. Reload Project and the new dependencies should be visible inside your `External Libraries` directory
7. Please commit the `pom.xml` file as its own **separate** commit. We do not want it lost within your large PR
8. Enjoy :sunglasses:

### Environment Variables

In order for MongoDB to work, you **must** create a `.env` file within `src/main/resources`. It then must include the
secret
key, frontend url and connection string (standard or SRV format allowed).

```dotenv
MONGODB_CONNECTION_STRING=fakeconnectionstring
AWS_REGION=foo-region
AWS_STAGE=foo-stage
WEB_SOCKET_BACKEND_ENDPOINT=foowebsocketendpoint
REST_BACKEND_ENDPOINT=foorestendpoint
```

### Database Migrations
To enable database migrations and be able to apply existing changesets, you **must** create a `liquibase.properties` file within `src/main/resources`.
It must then include the MongoDB connection string in [**standard format only**](https://www.mongodb.com/docs/manual/reference/connection-string/#standard-connection-string-format).
```properties
url=mongodb://fakeconnectionstring
```
You can create a new changeset file from the template using:
- ``mvn antrun:run@make-changeset``
- ``mvn antrun:run@make-changeset -DchangesetName=example``

You can apply all existing (and not yet applied) changesets using the command: ``mvn liquibase:update``

You can rollback changesets using the command (may need to run in CMD.exe): ``mvn liquibase:rollback -Dliquibase.rollbackCount=1``

## Testing

This project utilizes **[REST Assured](https://rest-assured.io/)** for Integration Tests and
**[JUnit 5](https://junit.org/junit5/docs/current/user-guide/)** for both Unit and
Integration tests

### Running Tests

#### 1. Command line

- All tests - ``mvn verify``
- unit tests - ``mvn test``
- integration tests - ``mvn failsafe:integration-test``

#### 2. IDE

1. Open up `Project` view in your IDE and expand the `Test` directory (it will be identical to the `src` file structure)
2. `Right click` on the `Java` directory inside it
3. Selection the option, `Run 'tests' in Java`