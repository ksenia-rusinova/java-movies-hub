package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static ru.practicum.moviehub.api.ErrorResponse.*;
import static ru.practicum.moviehub.store.MoviesStore.*;

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
        try {
            Movie request = gson.fromJson(reqAsString, Movie.class);

            Headers requestHeaders = ex.getRequestHeaders();
            List<String> contentTypeValues = requestHeaders.get("Content-type");

            if(contentTypeValues.contains(CT_JSON)){
                if((request.getTitle() != null && request.getYear() != null) &&
                        request.getTitle().length() < 100 &&
                        (request.getYear() >= 1888 && request.getYear() <= (LocalDate.now().getYear() + 1))){
                    Integer id = addRecord(request.getTitle(), request.getYear());
                    sendJson(ex, 201, gson.toJson(getListElementById(id)));
                } else if(request.getTitle() == null || request.getTitle().isEmpty()) {
                    String[] details = {"название не должно быть пустым"};
                    sendJson(ex, 422, gson.toJson(getError("Ошибка валидации", details)));
                } else if(request.getTitle().length() > 100) {
                    String[] details = {"название не должно содержать более 100 символов"};
                    sendJson(ex, 422, gson.toJson(getError("Ошибка валидации", details)));
                } else if(request.getYear() < 1888 || request.getYear() > (LocalDate.now().getYear() + 1)) {
                    String[] details = {"year должен быть в диапазоне от 1888 до текущего года + 1"};
                    sendJson(ex, 422, gson.toJson(getError("Ошибка валидации", details)));
                }
            } else {
                String[] details = {"некорректное значение заголовка Content-Type"};
                sendJson(ex, 415, gson.toJson(getError("Ошибка валидации", details)));
            }
        } catch (JsonSyntaxException e) {
            String[] details = {"JSON некорректен по синтаксису или его структура не подходит для Movie"};
            sendJson(ex, 422, gson.toJson(getError("Ошибка валидации", details)));
        }
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }
}