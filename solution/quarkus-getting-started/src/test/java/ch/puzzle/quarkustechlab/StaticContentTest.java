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
            Assertions.assertTrue(contents.contains("<title>quarkus-getting-started"));
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