package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static ru.practicum.moviehub.store.MoviesStore.addRecord;
import static ru.practicum.moviehub.store.MoviesStore.getListElementById;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8";

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendPostMovies(HttpExchange ex) throws IOException {
        InputStream inputStream = ex.getRequestBody();
        String reqAsString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        Gson gson = new Gson();
        Movie request = gson.fromJson(reqAsString, Movie.class);

        Headers requestHeaders = ex.getRequestHeaders();
        List<String> contentTypeValues = requestHeaders.get("Content-type");

        if(contentTypeValues.contains(CT_JSON) && request.getTitle() != null && request.getYear() != null) {
            Integer id = addRecord(request.getTitle(), request.getYear());
            sendJson(ex, 201, gson.toJson(getListElementById(id)));
        }
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }
}