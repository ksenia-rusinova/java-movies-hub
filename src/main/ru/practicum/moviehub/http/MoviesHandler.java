package ru.practicum.moviehub.http;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore moviesStore = new MoviesStore();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        URI requestURI = ex.getRequestURI();
        String[] splitStrings = requestURI.getPath().split("/");

        if (method.equalsIgnoreCase("GET")) {
            String query = requestURI.getQuery();

            if (splitStrings.length == 3) {
                try {
                    Integer.parseInt(splitStrings[2].trim());
                    sendGetMoviesId(ex, splitStrings);
                } catch (NumberFormatException e) {
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.setError("Ошибка валидации");
                    errorResponse.setDetails(new String[]{"некорректный id = " + splitStrings[2].trim() + ", id должен состоять только из цифр"});

                    sendJson(ex, 400, gson.toJson(errorResponse));
                }
            } else if (query != null && !query.isEmpty()) {
                String year = requestURI.getQuery().split("=")[1];
                try {
                    Integer.parseInt(year.trim());
                    sendJson(ex, 200, gson.toJson(moviesStore.getMovieFromListByYear(Integer.parseInt(year.trim()))));
                } catch (NumberFormatException e) {
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.setError("Ошибка валидации");
                    errorResponse.setDetails(new String[]{"некорректный параметр запроса — 'year', year = " + year});

                    sendJson(ex, 400, gson.toJson(errorResponse));
                }
            } else {
                sendJson(ex, 200, gson.toJson(moviesStore.getAll()));
            }
        } else if (method.equalsIgnoreCase("POST")) {
            sendPostMovies(ex);
        } else if (method.equalsIgnoreCase("DELETE")) {
            try {
                Integer.parseInt(splitStrings[2].trim());
                sendDeleteMoviesId(ex, splitStrings);
            } catch (NumberFormatException e) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setError("Ошибка валидации");
                errorResponse.setDetails(new String[]{"некорректный id = " + splitStrings[2].trim() + ", id должен состоять только из цифр"});

                sendJson(ex, 400, gson.toJson(errorResponse));
            }
        } else {
            sendNoContent(ex, 405);
        }
    }

    private void sendPostMovies(HttpExchange ex) throws IOException {
        InputStream inputStream = ex.getRequestBody();
        String reqAsString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        try {
            Movie request = gson.fromJson(reqAsString, Movie.class);

            Headers requestHeaders = ex.getRequestHeaders();
            List<String> contentTypeValues = requestHeaders.get("Content-type");

            if (contentTypeValues != null && !contentTypeValues.contains(CT_JSON)) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setError("Ошибка валидации");
                errorResponse.setDetails(new String[]{"некорректное значение заголовка Content-Type"});

                sendJson(ex, 415, gson.toJson(errorResponse));
                return;
            }

            if (request.getTitle() == null || request.getTitle().isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setError("Ошибка валидации");
                errorResponse.setDetails(new String[]{"название не должно быть пустым"});

                sendJson(ex, 422, gson.toJson(errorResponse));
                return;
            }

            if (request.getTitle().length() > 100) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setError("Ошибка валидации");
                errorResponse.setDetails(new String[]{"название не должно содержать более 100 символов"});

                sendJson(ex, 422, gson.toJson(errorResponse));
                return;
            }

            if (request.getYear() < 1888 || request.getYear() > (LocalDate.now().getYear() + 1)) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setError("Ошибка валидации");
                errorResponse.setDetails(new String[]{"year должен быть в диапазоне от 1888 до текущего года + 1"});

                sendJson(ex, 422, gson.toJson(errorResponse));
                return;
            }

            Integer id = moviesStore.addRecord(request.getTitle(), request.getYear());
            sendJson(ex, 201, gson.toJson(moviesStore.getMovieFromListById(id)));

        } catch (JsonSyntaxException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setError("Ошибка валидации");
            errorResponse.setDetails(new String[]{"JSON некорректен по синтаксису или его структура не подходит для Movie"});

            sendJson(ex, 422, gson.toJson(errorResponse));
        }
    }

    private void sendGetMoviesId(HttpExchange ex, String[] uri) throws IOException {
        if (moviesStore.getMovieFromListById(Integer.parseInt(uri[2])) != null) {
            sendJson(ex, 200, gson.toJson(moviesStore.getMovieFromListById(Integer.parseInt(uri[2]))));
        } else {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setError("Ошибка валидации");
            errorResponse.setDetails(new String[]{"фильм по id = " + uri[2] + " не найден"});

            sendJson(ex, 404, gson.toJson(errorResponse));
        }
    }

    private void sendDeleteMoviesId(HttpExchange ex, String[] uri) throws IOException {
        if (moviesStore.getMovieFromListById(Integer.parseInt(uri[2])) != null) {
            moviesStore.deleteListElementById(Integer.parseInt(uri[2]));
            sendNoContent(ex, 204);
        } else {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setError("Ошибка валидации");
            errorResponse.setDetails(new String[]{"фильм по id = " + uri[2] + " не удалось удалить, тк id не найден"});

            sendJson(ex, 404, gson.toJson(errorResponse));
        }
    }
}
