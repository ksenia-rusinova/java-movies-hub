package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import static ru.practicum.moviehub.store.MoviesStore.getAll;

public class MoviesHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            sendJson(ex, 200, getAll().toString());
        } else if (method.equalsIgnoreCase("POST")) {
            sendPostMovies(ex);
        }
    }
}
