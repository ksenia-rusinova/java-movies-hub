package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;

public class MoviesStore {
    private Integer nextId = 0;
    private final Map<Integer, Movie> store = new HashMap<>();

    public Integer addRecord(String title, Integer year) {
        Movie movie = new Movie();

        nextId = nextId + 1;
        movie.setId(nextId);
        movie.setTitle(title);
        movie.setYear(year);
        store.put(nextId, movie);

        return nextId;
    }

    public List<Movie> getAll() {
        return new ArrayList<>(store.values());
    }

    public Movie getMovieFromListById(Integer id) {
        return store.get(id);
    }

    public void deleteListElementById(Integer id) {
        store.remove(id);
    }

    public List<Movie> getMovieFromListByYear(Integer queryYear) {
        return store.values().stream()
                .filter(movie -> movie.getYear().equals(queryYear)).toList();
    }
}