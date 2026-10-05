package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.practicum.moviehub.model.Movie;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.moviehub.http.BaseHttpHandler.CT_JSON;

public class MoviesApiTest {

    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;

    @BeforeEach
    void beforeEach() {
        server = new MoviesServer();
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterEach
    void afterEach() {
        server.stop();
    }

    /*
    GET /movies
    */
    ///возвращает пустой список, если нет фильмов
    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    ///возвращает список с ранее добавленными фильмами

    /*
    POST /movies
    */
    ///добавляет фильм при корректных данных
    @Test
    void postMovies_successData() throws Exception {
        Gson gson = new Gson();
        Movie request = new Movie();
        request.setTitle("Сумерки");
        request.setYear(2008);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertNotNull(body.getId());
        assertEquals("Сумерки", body.getTitle());
        assertEquals(2008, body.getYear());
    }
}