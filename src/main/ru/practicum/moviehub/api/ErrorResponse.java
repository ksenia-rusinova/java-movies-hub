package ru.practicum.moviehub.api;

import ru.practicum.moviehub.model.Movie;

public class ErrorResponse {

    public static Movie getError(String error, String[] details) {
        Movie movie = new Movie();
        movie.setError(error);
        movie.setDetails(details);
        return movie;
    }
}