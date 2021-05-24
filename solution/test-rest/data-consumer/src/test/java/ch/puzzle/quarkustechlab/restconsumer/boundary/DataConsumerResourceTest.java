package ch.puzzle.quarkustechlab.restconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.when;

@QuarkusTest
class DataConsumerResourceTest {

    @RestClient
    @InjectMock
    DataProducerService dataProducerService;

    @Test
    @DisplayName("should return data at /data")
    public void dataTest() {
        Mockito.when(dataProducerService.getSensorMeasurement()).thenReturn(new SensorMeasurement());
        when().get("/data")
                .then()
                .statusCode(200)
                .body(CoreMatchers.isA(String.class));
        Mockito.verify(dataProducerService, Mockito.times(1)).getSensorMeasurement();
    }
}