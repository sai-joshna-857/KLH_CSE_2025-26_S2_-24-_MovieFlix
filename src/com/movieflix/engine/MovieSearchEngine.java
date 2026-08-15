package com.movieflix.engine;

import com.movieflix.model.Movie;
import com.movieflix.util.CustomList;

public class MovieSearchEngine {
    private CustomList<Movie> database;

    public MovieSearchEngine() {
        this.database = new CustomList<>();
    }

    public void addMovie(Movie movie) {
        database.add(movie);
    }

    public CustomList<Movie> searchByTitle(String query) {
        CustomList<Movie> results = new CustomList<>();
        for (int i = 0; i < database.size(); i++) {
            Movie movie = database.get(i);
            if (KMPMatcher.search(movie.getTitle(), query)) {
                results.add(movie);
            }
        }
        return results;
    }
}
