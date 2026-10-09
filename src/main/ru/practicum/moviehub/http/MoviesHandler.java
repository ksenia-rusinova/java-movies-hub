package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URI;

import static ru.practicum.moviehub.api.ErrorResponse.getError;
import static ru.practicum.moviehub.store.MoviesStore.getAll;
import static ru.practicum.moviehub.store.MoviesStore.getMovieFromListByYear;

public class MoviesHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        URI requestURI = ex.getRequestURI();
        String[] splitStrings = requestURI.getPath().split("/");

        if (method.equalsIgnoreCase("GET")) {
            Gson gson = new Gson();

            String query = requestURI.getQuery();

            if (splitStrings.length == 3) {
                try {
                    Integer.parseInt(splitStrings[2].trim());
                    sendGetMoviesId(ex, splitStrings);
                } catch (NumberFormatException e) {
                    String[] details = {"некорректный id = " + splitStrings[2].trim() + ", id должен состоять только из цифр"};
                    sendJson(ex, 400, gson.toJson(getError("Ошибка валидации", details)));
                }
            } else if (query != null && !query.isEmpty()) {
                String year = requestURI.getQuery().split("=")[1];
                try {
                    Integer.parseInt(year.trim());
                    sendJson(ex, 200, gson.toJson(getMovieFromListByYear(Integer.parseInt(year.trim()))));
                } catch (NumberFormatException e) {
                    String[] details = {"некорректный параметр запроса — 'year', year = " + year};
                    sendJson(ex, 400, gson.toJson(getError("Ошибка валидации", details)));
                }
            } else {
                sendJson(ex, 200, gson.toJson(getAll()));
            }
        } else if (method.equalsIgnoreCase("POST")) {
            sendPostMovies(ex);
        } else if (method.equalsIgnoreCase("DELETE")) {
            try {
                Integer.parseInt(splitStrings[2].trim());
                sendDeleteMoviesId(ex, splitStrings);
            } catch (NumberFormatException e) {
                Gson gson = new Gson();

                String[] details = {"некорректный id = " + splitStrings[2].trim() + ", id должен состоять только из цифр"};
                sendJson(ex, 400, gson.toJson(getError("Ошибка валидации", details)));
            }
        } else {
            sendNoContent(ex, 405);
        }
    }
}
