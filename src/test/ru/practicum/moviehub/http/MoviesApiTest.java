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
import java.util.List;

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

    /**
     * GET /movies
     */
    ///возвращает пустой список, если нет фильмов
    @Test
    void getMovies_whenEmpty_returnEmptyArray() throws Exception {
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

    /// возвращает список с ранее добавленными фильмами
    @Test
    void getMovies_whenListNotEmpty_returnList() throws Exception {
        Gson gson = new Gson();

        //добавляем 1-й фильм
        Movie request1 = new Movie();
        request1.setTitle("Сумерки");
        request1.setYear(2008);

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request1), StandardCharsets.UTF_8))
                .build();

        client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        //добавляем 2-й фильм
        Movie request2 = new Movie();
        request2.setTitle("Сумерки. Сага. Новолуние");
        request2.setYear(2009);

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request2), StandardCharsets.UTF_8))
                .build();

        client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        //тест метода GET /movies
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        List<Movie> body = gson.fromJson(resp.body().trim(), new ListOfMoviesTypeToken().getType());

        assertEquals(1, body.get(0).getId());
        assertEquals("Сумерки", body.get(0).getTitle());
        assertEquals(2008, body.get(0).getYear());

        assertEquals(2, body.get(1).getId());
        assertEquals("Сумерки. Сага. Новолуние", body.get(1).getTitle());
        assertEquals(2009, body.get(1).getYear());
    }

    /**
     * POST /movies
     */
    ///добавляет фильм при корректных данных
    @Test
    void postMovies_addMovie_dataIsValid() throws Exception {
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

        assertEquals(201, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertNotNull(body.getId());
        assertEquals("Сумерки", body.getTitle());
        assertEquals(2008, body.getYear());
    }

    @Test
    void postMovies_addSeveralMovies_dataIsValid() throws Exception {
        Gson gson = new Gson();

        //добавляем 1-й фильм
        Movie request1 = new Movie();
        request1.setTitle("Сумерки");
        request1.setYear(2008);

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request1), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp1 =
                client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp1.statusCode());

        String contentTypeHeaderValue =
                resp1.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body1 = gson.fromJson(resp1.body().trim(), Movie.class);
        assertNotNull(body1.getId());
        assertEquals("Сумерки", body1.getTitle());
        assertEquals(2008, body1.getYear());

        //добавляем 2-й фильм
        Movie request2 = new Movie();
        request2.setTitle("Сумерки. Сага. Новолуние");
        request2.setYear(2009);

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request2), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp2.statusCode());

        String contentTypeHeaderValue2 =
                resp2.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue2);

        Movie body2 = gson.fromJson(resp2.body().trim(), Movie.class);
        assertNotNull(body2.getId());
        assertEquals("Сумерки. Сага. Новолуние", body2.getTitle());
        assertEquals(2009, body2.getYear());
    }

    /// возвращает ошибку при пустом title
    @Test
    void postMovies_whenTitleEmpty_returnError() throws Exception {
        Gson gson = new Gson();
        Movie request = new Movie();
        request.setYear(2009);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("название не должно быть пустым", body.getDetails()[0]);
    }

    /// возвращает ошибку при слишком длинном title (> 100 символов)
    @Test
    void postMovies_whenTitleLongerThan100Characters_returnError() throws Exception {
        Gson gson = new Gson();
        Movie request = new Movie();
        request.setTitle("сумеркисумсумеркисумсумеркисумсумеркисумсумеркисумсумеркисумсумеркисумсумеркисумсумеркисумсумеркисуме");
        request.setYear(2009);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("название не должно содержать более 100 символов", body.getDetails()[0]);
    }

    /// возвращает ошибку при неверном year (меньше 1888 или больше текущего года + 1)
    @Test
    void postMovies_whenYearLessThan1888_returnError() throws Exception {
        Gson gson = new Gson();
        Movie request = new Movie();
        request.setTitle("Сумерки");
        request.setYear(1887);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("year должен быть в диапазоне от 1888 до текущего года + 1", body.getDetails()[0]);
    }

    @Test
    void postMovies_whenYearMoreThanCurrentPlusOne_returnError() throws Exception {
        Gson gson = new Gson();
        Movie request = new Movie();
        request.setTitle("Сумерки");
        request.setYear(2028);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("year должен быть в диапазоне от 1888 до текущего года + 1", body.getDetails()[0]);
    }

    /// возвращает ошибку при неправильном Content-Type
    @Test
    void postMovies_whenIncorrectContentType_returnError() throws Exception {
        Gson gson = new Gson();
        Movie request = new Movie();
        request.setTitle("Сумерки");
        request.setYear(2008);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("некорректное значение заголовка Content-Type", body.getDetails()[0]);
    }

    /// возвращает ошибку при некорректном JSON
    @Test
    void postMovies_whenIncorrectJson_returnError() throws Exception {
        Gson gson = new Gson();
        String json = "\"title\":\"Сумерки\",\"year\":2008}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("JSON некорректен по синтаксису или его структура не подходит для Movie", body.getDetails()[0]);
    }

    /**
     * GET /movies/{id}
     */
    ///возвращает фильм по существующему id
    @Test
    void getMoviesId_returnMovieByExistingId() throws Exception {
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

        if (resp.statusCode() == 201) {
            Movie body = gson.fromJson(resp.body().trim(), Movie.class);
            Integer id = body.getId();

            ///тест метода GET /movies/{id}
            HttpRequest reqGetMoviesId = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/movies/" + id))
                    .GET()
                    .build();

            HttpResponse<String> respGetMoviesId =
                    client.send(reqGetMoviesId, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertEquals(200, respGetMoviesId.statusCode());

            String contentTypeHeaderValue =
                    respGetMoviesId.headers().firstValue("Content-Type").orElse("");
            assertEquals(CT_JSON, contentTypeHeaderValue);

            Movie bodyGetMoviesId = gson.fromJson(respGetMoviesId.body().trim(), Movie.class);
            assertEquals(id, bodyGetMoviesId.getId());
            assertEquals("Сумерки", bodyGetMoviesId.getTitle());
            assertEquals(2008, bodyGetMoviesId.getYear());
        }
    }

    /// возвращает ошибку, если фильм не найден
    @Test
    void getMoviesId_movieNotFoundById_returnError() throws Exception {
        Gson gson = new Gson();

        Integer id = 20;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("фильм по id = " + id + " не найден", body.getDetails()[0]);
    }

    /// возвращает ошибку, если id не число
    @Test
    void getMoviesId_idNotNumber_returnError() throws Exception {
        Gson gson = new Gson();

        String id = "id";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("некорректный id = " + id + ", id должен состоять только из цифр", body.getDetails()[0]);
    }

    /**
     * DELETE /movies/{id}
     */
    ///удаляет фильм по существующему id
    @Test
    void deleteMoviesId_deleteMovieByExistingId() throws Exception {
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

        if (resp.statusCode() == 201) {
            Movie body = gson.fromJson(resp.body().trim(), Movie.class);
            Integer id = body.getId();

            ///тест метода DELETE /movies/{id}
            HttpRequest reqDeleteMoviesId = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/movies/" + id))
                    .DELETE()
                    .build();

            HttpResponse<String> respDeleteMoviesId =
                    client.send(reqDeleteMoviesId, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertEquals(204, respDeleteMoviesId.statusCode());

            String contentTypeHeaderValue =
                    respDeleteMoviesId.headers().firstValue("Content-Type").orElse("");
            assertEquals(CT_JSON, contentTypeHeaderValue);

            String bodyDeleteMoviesId = respDeleteMoviesId.body().trim();
            assertTrue(bodyDeleteMoviesId.isEmpty());
        }
    }

    /// возвращает ошибку, если фильм не найден
    @Test
    void deleteMoviesId_movieNotFoundById_returnError() throws Exception {
        Gson gson = new Gson();

        Integer id = 20;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("фильм по id = " + id + " не удалось удалить, тк id не найден", body.getDetails()[0]);
    }

    /// возвращает ошибку, если id не число
    @Test
    void deleteMoviesId_idNotNumber_returnError() throws Exception {
        Gson gson = new Gson();

        String id = "id";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("некорректный id = " + id + ", id должен состоять только из цифр", body.getDetails()[0]);
    }

    /**
     * GET /movies?year=YYYY
     */
    ///возвращает фильмы указанного года
    @Test
    void getMoviesYear_returnListOfFilmsFromSelectedYear() throws Exception {
        Gson gson = new Gson();

        //добавляем 1-й фильм
        Movie request1 = new Movie();
        request1.setTitle("Сумерки");
        request1.setYear(2008);

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request1), StandardCharsets.UTF_8))
                .build();

        client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        //добавляем 2-й фильм
        Movie request2 = new Movie();
        request2.setTitle("Сумерки. Сага. Новолуние");
        request2.setYear(2009);

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request2), StandardCharsets.UTF_8))
                .build();

        client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        //тест метода GET /movies?year=YYYY
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2008"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        List<Movie> body = gson.fromJson(resp.body().trim(), new ListOfMoviesTypeToken().getType());

        assertEquals(1, body.size());
        assertEquals(1, body.get(0).getId());
        assertEquals("Сумерки", body.get(0).getTitle());
        assertEquals(2008, body.get(0).getYear());
    }

    /// возвращает пустой список, если фильмов с таким годом нет
    @Test
    void getMoviesYear_filmsNotFoundForYear_returnEmptyList() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2008"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    /// возвращает ошибку, если параметр year не число
    @Test
    void getMoviesYear_yearNotNumber_returnError() throws Exception {
        Gson gson = new Gson();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=zero"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        Movie body = gson.fromJson(resp.body().trim(), Movie.class);
        assertEquals("Ошибка валидации", body.getError());
        assertEquals("некорректный параметр запроса — 'year', year = zero", body.getDetails()[0]);
    }

    /// при неподдерживаемом HTTP-методе возвращается 405 Method Not Allowed
    @Test
    void putMovies() throws Exception {
        Gson gson = new Gson();

        Movie request = new Movie();
        request.setTitle("Сумерки");
        request.setYear(2008);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(request), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue);

        assertEquals("", resp.body().trim());
    }
}