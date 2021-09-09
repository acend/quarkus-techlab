package ch.puzzle.quarkustechlab.restproducer.boundary;

import ch.puzzle.quarkustechlab.restproducer.entity.SensorMeasurement;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.json.bind.JsonbBuilder;
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
        System.out.println(response.body());
        SensorMeasurement measurement = JsonbBuilder.create().fromJson(response.body(), SensorMeasurement.class);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(measurement.data),
                () -> Assertions.assertEquals(response.statusCode(), 200)
        );
    }
}
