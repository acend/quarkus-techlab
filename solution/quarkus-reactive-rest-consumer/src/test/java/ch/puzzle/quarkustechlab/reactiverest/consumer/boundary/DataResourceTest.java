package ch.puzzle.quarkustechlab.reactiverest.consumer.boundary;

import ch.puzzle.quarkustechlab.reactiverest.consumer.entity.SensorMeasurement;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class DataResourceTest {

    @InjectMock
    @RestClient
    DataService mock;

    @Test
    public void testHelloEndpoint() {
        LocalDate localDate = LocalDate.parse("2024-01-01");
        LocalDateTime localDateTime = localDate.atStartOfDay();
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);

        SensorMeasurement measurement = new SensorMeasurement();
        measurement.id = 1L;
        measurement.data = 3.5d;
        measurement.time = instant;

        Mockito.when(mock.findAll()).thenReturn(Uni.createFrom().item(List.of(measurement)));

        given()
          .when().get("/data")
          .then()
             .statusCode(200)
             .body(is("[{\"id\":1,\"data\":3.5,\"time\":\"2024-01-01T00:00:00Z\"}]"));
    }
}