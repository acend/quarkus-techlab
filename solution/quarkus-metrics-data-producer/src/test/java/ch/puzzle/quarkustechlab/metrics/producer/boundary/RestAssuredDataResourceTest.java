package ch.puzzle.quarkustechlab.metrics.producer.boundary;

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