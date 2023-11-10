package ch.puzzle.quarkustechlab.reactiverest.producer.control;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DBInit {

    private final PgPool client;
    private final boolean schemaCreate;
    private static final Logger log = LoggerFactory.getLogger(DBInit.class.getName());

    public DBInit(PgPool client, @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") boolean schemaCreate) {
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            log.info("Initializing Database");
            initDb();
        }
    }

    private void initDb() {
        client.query("DROP TABLE IF EXISTS sensormeasurements").execute()
                .flatMap(r -> client.query("CREATE TABLE sensormeasurements (id SERIAL PRIMARY KEY, data DOUBLE PRECISION, time TIMESTAMP WITH TIME ZONE DEFAULT NOW()::timestamp)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.1)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.2)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.3)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.4)").execute())
                .await().indefinitely();
    }
}
