package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

import io.smallrye.common.annotation.NonBlocking;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

@ApplicationScoped
public class ConfigSourceJsonRPCService {

    @Inject
    Config config;

    @NonBlocking
    public JsonArray getAll() {
        var array = new JsonArray();
        config.getConfigSources().forEach(cs -> {
            array.add(getJsonRepresentationForConfigSource(cs));
        });

        return array;
    }

    private JsonObject getJsonRepresentationForConfigSource(ConfigSource c) {
        var properties = new JsonArray();
        c.getProperties().forEach((prop, value) -> {
            properties.add(new JsonObject().put("key", prop).put("value", value));
        });

        return new JsonObject()
                .put("name", c.getName())
                .put("size", c.getProperties().size())
                .put("ordinal", c.getOrdinal())
                .put("properties", properties);
    }
}