---
title: "3.2 Testing Quarkus applications"
linkTitle: "3.2 Testing Quarkus applications"
weight: 320
sectionnumber: 3.2
description: >
  This section covers testing in a Quarkus application.
---


## {{% param sectionnumber %}}.1: Testing in a Quarkus application

In the last few chapters you learned how to implement a simple REST API. What would software engineering be without testing. Before starting with writing your first tests make sure you have the following two dependencies in your project:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
```

Quarkus supports and suggests using JUnit 5, because of this make sure the version for the Surefire Maven Plugin is set - the default version does not support JUnit 5. When creating a new Quarkus application with the help of Maven you will automatically see generated test classes in your project. Let's examine them for a second.

```java
package ch.puzzle.quarkustechlab;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("Hello from Quarkus REST"));
    }
}
```

If you are familiar with the JUnit 5 framework there are no big surprises here. The annotation `@QuarkusTest` instructs JUnit to start the application before the tests. The above shown test simply tests if the GET request to the `/hello` endpoint returns a response with HTTP status code 200 and body `"Hello from Quarkus REST"` - nothing fancy.

You can run your tests using Maven:

```s
./mvnw test
```


### {{% param sectionnumber %}}.1.1: Multi-module projects or external modules

At build time Quarkus relies heavily on [Jandex](https://github.com/wildfly/jandex) to discover classes and annotations. CDI bean discovery is one example of the usage of Jandex at build time. Due to the optimization at build time configuration of this discovery is key. Especially when working with external modules or multi-module projects a bit of preparation is needed.


#### {{% param sectionnumber %}}.1.1.1: Multi-module projects

By default Quarkus will not discover any CDI beans inside another module. To enable CDI bean discovery in a multi-module project you can include the `jandex-maven-plugin` in your submodules.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.jboss.jandex</groupId>
      <artifactId>jandex-maven-plugin</artifactId>
      <version>1.0.7</version>
      <executions>
        <execution>
          <id>make-index</id>
          <goals>
            <goal>jandex</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

If your parent module already configured the `quarkus-maven-plugin` the CDI bean discovery will work out of the box!


#### {{% param sectionnumber %}}.1.1.2: External modules

If you're planning to use external modules, you will need to make these modules known to the indexing process at build time. You can do this either by including the Jandex plugin to the external module (obviously only works when you can modify them) or you can use the configuration property `quarkus.index-dependency` inside the application.properties. If you want to dive deeper into the CDI context check out the [documentation](https://quarkus.io/guides/cdi-reference#bean_discovery) of CDI in Quarkus.


## {{% param sectionnumber %}}.2: Basics


### {{% param sectionnumber %}}.2.1: Configuration

By default Quarkus will listen on port `8080`, when running tests it defaults to `8081`. This allows us to have the application running and run tests in parallel.
You can configure the test port with the properties `quarkus.http.test-port` for HTTP and `quarkus.http.test-ssl-port` for HTTPS in your application.properties:

```s
quarkus.http.test-port=8083
quarkus.http.test-ssl-port=8446
```

The RestAssured integration automatically updates the default port used by RestAssured before the tests are run.

When using RestAssured in your tests you can manually override the default response timeouts of 30 seconds by modifying the `quarkus.http.test-timeout` property:

```s
quarkus.http.test-timeout=10s
```


### {{% param sectionnumber %}}.2.2: Injecting an URI

To use a different client you can directly inject the URL into the test. The `@TestHTTPResource` allows us to inject and create said URLs. Take a look at the following example serving the static HTML file at `src/main/resources/META-INF/resources/index.html`:

```java
package ch.puzzle.quarkustechlab;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class StaticContentTest {

    @TestHTTPResource("index.html") 
    URL url;

    @Test
    public void testIndexHtml() throws Exception {
        try (InputStream in = url.openStream()) {
            String contents = readStream(in);
            Assertions.assertTrue(contents.contains("<title>getting-started"));
        }
    }

    private static String readStream(InputStream in) throws IOException {
        byte[] data = new byte[1024];
        int r;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((r = in.read(data)) > 0) {
            out.write(data, 0, r);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
```

The annotation `@TestHTTPResource` allows us to inject either an URI, URL or String representation of the desired URL.


## {{% param sectionnumber %}}.3: Testing specific endpoints

Both RESTassured and `@TestHTTPResource` allow us to specify the endpoint class which we want to test, instead of hardcoding a path into our tests.


### {{% param sectionnumber %}}.3.1: TestHTTPResource

You can the use the `TestHTTPEndpoint` annotation to specify the endpoint path, and the path will be extracted from the provided endpoint. If you also specify a value for the `TestHTTPResource` endpoint it will be appended to the end of the endpoint path.

Let's take a look at an example for our `data-producer` project:

```java
package ch.puzzle.quarkustechlab.restproducer.boundary;

import ch.puzzle.quarkustechlab.restproducer.entity.SensorMeasurement;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@QuarkusTest
public class DataResourceTest {

    @TestHTTPEndpoint(DataResource.class)
    @TestHTTPResource
    URL url;

    @Test
    @DisplayName("returns data from /data endpoint of DataResource.class")
    public void testData() throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(url.toURI()).build();
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SensorMeasurement measurement = new ObjectMapper().readValue(response.body(), SensorMeasurement.class);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(measurement.data),
                () -> Assertions.assertEquals(response.statusCode(), 200)
        );

    }
}
```

This simple tests injects us an `URL` object pointing to the `DataResource.class` endpoint. When using different resources inside the `DataResource.class` you can simply add the path to the `@TestHTTPResource` value field:

```java
    @TestHTTPEndpoint(DataResource.class)
    @TestHTTPResource("hello")
    URL url;
```


### {{% param sectionnumber %}}.3.2: RestAssured

When using RestAssured you can similarly control the base path by annotating the test class or method with `TestHTTPEndpoint(...)`. Recreating the test from above would look something like this:

```java

package ch.puzzle.quarkustechlab.restproducer.boundary;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;

@QuarkusTest
@TestHTTPEndpoint(DataResource.class)
public class RestAssuredDataResourceTest {

    @Test
    @DisplayName("RestAssured: returns data from /data endpoint of DataResource.class")
    public void testData() {
        when().get()
                .then()
                .statusCode(200)
                .body(CoreMatchers.isA(String.class));
    }
}

```

As you can see the RestAssured way is pretty clean and straight forward.


## {{% param sectionnumber %}}.4: Mock support in Quarkus

Quarkus supports mocking CDI beans with two different approaches. You can use the known CDI alternatives to mock classes for all test classes, or you can use the `QuarkusMock` to mock out beans on a per test basis.


### {{% param sectionnumber %}}.4.1: CDI `@Alternative`

If you would like to mock with a class from the `src/test/java` source, you can annotate the class with `@Alternative` and `@Priority(1)`. Let's pretend we want to mock the class `DummyService` in the following test:

```java

@ApplicationScoped
public class DummyService {

    public String dummy() {
        return "dummy";
    }
}

```

We can create the following alternative bean in our test module:

```java

@ApplicationScoped
@Alternative
@Priority(1)
public class MockDummyService extends DummyService {

    @Override
    public String dummy() {
        return "ima mock";
    }
}

```

For quality of life reasons there is a stereotype defined `io.quarkus.test.Mock` which can be used to declare a class `@Alternative @Priority(1) @Dependent`.

```java

@ApplicationScoped
@Mock
public class MockDummyService extends DummyService {

    @Override
    public String dummy() {
        return "ima mock";
    }
}

```

{{% alert title="Native Image" color="warning" %}}
This approach does not work when testing with native images, keep this in mind.
{{% /alert %}}


### {{% param sectionnumber %}}.4.2: Mocking with QuarkusMock

The alternative to using the above mentioned approach, Quarkus provides a class `io.quarkus.test.junit.QuarkusMock` to mock out classes for specific scopes in a test. We can use the class to inject mocks in single `@Test` methods or define the mock in the `@BeforeAll` to make it available for the entire class.

This method can be used to mock any normal scoped CDI bean (`@ApplicationScoped`, `@RequestScoped`, etc.), basically every scope except `@Singleton` and `@Dependent`. Take a look at the Quarkus example from the docs:

```java

@QuarkusTest
public class MockTestCase {

    @Inject
    MockableBean1 mockableBean1;

    @Inject
    MockableBean2 mockableBean2;

    @BeforeAll
    public static void setup() {
        MockableBean1 mock = Mockito.mock(MockableBean1.class);
        Mockito.when(mock.greet("Stuart")).thenReturn("A mock for Stuart");
        QuarkusMock.installMockForType(mock, MockableBean1.class);  
    }

    @Test
    public void testBeforeAll() {
        Assertions.assertEquals("A mock for Stuart", mockableBean1.greet("Stuart"));
        Assertions.assertEquals("Hello Stuart", mockableBean2.greet("Stuart"));
    }

    @Test
    public void testPerTestMock() {
        QuarkusMock.installMockForInstance(new BonjourGreeter(), mockableBean2); 
        Assertions.assertEquals("A mock for Stuart", mockableBean1.greet("Stuart"));
        Assertions.assertEquals("Bonjour Stuart", mockableBean2.greet("Stuart"));
    }

    @ApplicationScoped
    public static class MockableBean1 {

        public String greet(String name) {
            return "Hello " + name;
        }
    }

    @ApplicationScoped
    public static class MockableBean2 {

        public String greet(String name) {
            return "Hello " + name;
        }
    }

    public static class BonjourGreeter extends MockableBean2 {
        @Override
        public String greet(String name) {
            return "Bonjour " + name;
        }
    }
}

```

In the first approach from the example above (`MockTestCase::setup`), we register a `Mockito` mock for the defined class using the familiar Mockito API to define its behaviour. In the second approach (`MockTestCase::testPerTestMock`) we install a defined mock class into our test.

You can simplify the second approach by using the `quarkus-junit5-mockito` dependency. The annotation `@io.quarkus.test.junit.mockito.InjectMock` will do the same trick. The example from above can be replaced by the following code:

```java

@QuarkusTest
public class MockTestCase {

    @InjectMock
    MockableBean1 mockableBean1; 

    @InjectMock
    MockableBean2 mockableBean2;

    @BeforeEach
    public void setup() {
        Mockito.when(mockableBean1.greet("Stuart")).thenReturn("A mock for Stuart"); 
    }

    @Test
    public void firstTest() {
        Assertions.assertEquals("A mock for Stuart", mockableBean1.greet("Stuart"));
        Assertions.assertEquals(null, mockableBean2.greet("Stuart")); 
    }

    @Test
    public void secondTest() {
        Mockito.when(mockableBean2.greet("Stuart")).thenReturn("Bonjour Stuart"); 
        Assertions.assertEquals("A mock for Stuart", mockableBean1.greet("Stuart"));
        Assertions.assertEquals("Bonjour Stuart", mockableBean2.greet("Stuart"));
    }

    @ApplicationScoped
    public static class MockableBean1 {

        public String greet(String name) {
            return "Hello " + name;
        }
    }

    @ApplicationScoped
    public static class MockableBean2 {

        public String greet(String name) {
            return "Hello " + name;
        }
    }
}

```

You can use the normal Mockito API you're already familiar with. Injecting Spies into a test class is possible with the `@io.quarkus.test.junit.mockito.InjectSpy` annotation provided.


### {{% param sectionnumber %}}.4.3: Mocking RestClients

We are already familiar with the `@RegisterRestClient` annotation from the REST example in the previous chapter. Combining the `@InjectMock` with the `@RestClient` annotation will provide us with a mock rest client available for testing. The only thing you have to alter is that the registered rest client interface must be within the regular scope, so you might have to annotate your interface with `@ApplicationScoped`.

Example:

```java

@Path("/")
@ApplicationScoped
@RegisterRestClient
public interface GreetingService {

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    String hello();
}

```

```java

@QuarkusTest
public class GreetingResourceTest {

    @InjectMock
    @RestClient 
    GreetingService greetingService;

    @Test
    public void testHelloEndpoint() {
        Mockito.when(greetingService.hello()).thenReturn("hello from mockito");

        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("hello from mockito"));
    }

}

```
