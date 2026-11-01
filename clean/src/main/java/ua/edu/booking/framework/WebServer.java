package ua.edu.booking.framework;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ua.edu.booking.adapter.http.HttpController;
import ua.edu.booking.adapter.http.HttpResult;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Фреймворк-шар для HTTP: лише байти, заголовки й розбір query. */
public class WebServer {

    private final HttpController controller;

    public WebServer(HttpController controller) {
        this.controller = controller;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.start();
        System.out.println("HTTP: http://localhost:" + port + "/bookings");
    }

    private void handle(HttpExchange ex) throws IOException {
        HttpResult result = controller.handle(ex.getRequestMethod(), ex.getRequestURI().getPath(), query(ex));
        byte[] bytes = (result.json() + "\n").getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(result.status(), bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> query(HttpExchange ex) {
        Map<String, String> result = new HashMap<>();
        String raw = ex.getRequestURI().getRawQuery();
        if (raw == null) return result;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) result.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return result;
    }
}
