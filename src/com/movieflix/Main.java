package com.movieflix;

import com.movieflix.engine.MovieSearchEngine;
import com.movieflix.model.Movie;
import com.movieflix.util.CustomList;

public class Main {
    public static void main(String[] args) {
        MovieSearchEngine engine = new MovieSearchEngine();

        // Adding sample movie records
        engine.addMovie(new Movie(1, "Interstellar", "Sci-Fi", "Matthew McConaughey", "Explorers travel through a wormhole in space."));
        engine.addMovie(new Movie(2, "Inception", "Sci-Fi", "Leonardo DiCaprio", "A thief enters the dreams of others."));
        engine.addMovie(new Movie(3, "The Dark Knight", "Action", "Christian Bale", "Batman faces the Joker in Gotham City."));

        System.out.println("=== KMP Title Search for 'Dark' ===");
        CustomList<Movie> results = engine.searchByTitle("Dark");
        for (int i = 0; i < results.size(); i++) {
            System.out.println(results.get(i));
        }
    }
}