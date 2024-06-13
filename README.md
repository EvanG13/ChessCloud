# ChessCloud

# Project Setup

This project utilizes **[Apache Maven](https://maven.apache.org/)** for managing `Java` dependencies

### Install Dependencies

with tests: ``mvn clean install``\
skip tests: ``mvn clean install -DskipTests``

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
5. ``mvn install``
6. Reload Project and the new dependencies should be visible inside your `External Libraries` directory
7. Please commit the `pom.xml` file as its own **separate** commit. We do not want it lost within your large PR
8. Enjoy :sunglasses:

### Environment Variables

In order for MongoDB to work, you **must** create a `.env` file within your root directory. It then must include your
connection id and port number. Example below:

```text
MONGODB_CONNECTION_STRING=fakeconnectionstring
PORT=00000
```

### Check that dependencies are installed correctly

`` mvn test -Dgroups=dependency-check``

## Testing

This project utilizes **[JUnit 5](https://junit.org/junit5/docs/current/user-guide/)** for Unit and Integration tests

### Running Tests

#### 1. Command line

- all tests - ``mvn test``
- target tag - ``mvn test -Dgroups=foo-tag-name``

#### 2. IDE

1. Open up `Project` view in your IDE and expand the `Test` directory (it will be identical to the `src` file structure)
2. `Right click` on the `Java` directory inside it
3. Selection the option, `Run 'tests' in Java`