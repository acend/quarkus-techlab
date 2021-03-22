package ch.puzzle.producer.entity;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.StreamSupport;

public class SensorMeasurement {

    public Long id;
    public Double data;
    public Instant time;

    public SensorMeasurement() {
        this.data = Math.random();
        this.time = Instant.now();
    }

    public SensorMeasurement(Row row) {
        this.id = row.getLong("id");
        this.data = row.getDouble("data");
        this.time = Instant.from(row.getOffsetDateTime("time"));
    }

    public static Uni<SensorMeasurement> findById(PgPool client, Long id) {
        return client.preparedQuery("SELECT id, data, time from sensormeasurements where id = $1").execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? new SensorMeasurement(iterator.next()) : null);
    }

    public static Uni<SensorMeasurement> getLatest(PgPool client) {
        return client.query("SELECT id, data, time from sensormeasurements where time = (SELECT max(time) from sensormeasurements) limit 1").execute()
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? new SensorMeasurement(iterator.next()) : null);
    }

    public static Uni<SensorMeasurement> getAverage(PgPool client) {
        return client.query("SELECT 0 as id, avg(data) as data, NOW() as time from sensormeasurements").execute()
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? new SensorMeasurement(iterator.next()) : null);
    }

    public Uni<SensorMeasurement> save(PgPool client) {
        return client.preparedQuery("INSERT INTO sensormeasurements (data, time) VALUES ($1, $2) RETURNING (id, data, time)")
                .execute(Tuple.of(data, time.atOffset(ZoneOffset.UTC)))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? this : null);
    }

    public static Multi<SensorMeasurement> findAll(PgPool client) {
        return client.query("SELECT id, data, time from sensormeasurements").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().items(() -> StreamSupport.stream(set.spliterator(), false)))
                .onItem().transform(SensorMeasurement::new);
    }
}
