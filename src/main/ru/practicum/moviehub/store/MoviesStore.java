package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoviesStore {
    private static Integer nextId = 0;
    private static final Map<Integer, Movie> store = new HashMap<>();

    public static Integer addRecord(String title, Integer year) {
        Movie movie = new Movie();

        nextId = nextId + 1;
        movie.setId(nextId);
        movie.setTitle(title);
        movie.setYear(year);
        store.put(nextId, movie);

        return nextId;
    }

    public static List<Movie> getAll() {
        return new ArrayList<>(store.values());
    }

    public static Movie getListElementById(Integer id) {
        return store.get(id);
    }

    public static void deleteListElementById(Integer id) {
        store.remove(id);
    }
}