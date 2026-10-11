package com.movieflix.engine;

import com.movieflix.dataset.TMDBFetcher;
import com.movieflix.model.Movie;
import com.movieflix.util.CustomList;

public class MovieSearchEngine {
    private CustomList<Movie> movies;

    public MovieSearchEngine() {
        this.movies = new CustomList<>();
    }

    public void loadMovies(String dataFilePath) {
        this.movies = TMDBFetcher.fetchTop100Movies();
        System.out.println("[MovieSearchEngine] Successfully loaded " + movies.size() + " movies into CustomList.");
    }

    public CustomList<Movie> getAllMovies() {
        return movies;
    }
}