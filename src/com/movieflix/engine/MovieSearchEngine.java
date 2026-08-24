package com.movieflix.engine;

import com.movieflix.model.Movie;
import com.movieflix.util.CustomList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MovieSearchEngine {
    private CustomList<Movie> database;

    public MovieSearchEngine() {
        this.database = new CustomList<>();
    }

    public void addMovie(Movie movie) {
        database.add(movie);
    }

    public void loadCorpus(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    int id = Integer.parseInt(parts[0].trim());
                    String title = parts[1].trim();
                    String genre = parts[2].trim();
                    String cast = parts[3].trim();
                    String description = parts[4].trim();
                    database.add(new Movie(id, title, genre, cast, description));
                }
            }
            System.out.println("Corpus successfully loaded: " + database.size() + " records.");
        } catch (IOException e) {
            System.err.println("Error reading corpus file: " + e.getMessage());
        }
    }

    public CustomList<Movie> searchByPattern(String query) {
        CustomList<Movie> results = new CustomList<>();
        for (int i = 0; i < database.size(); i++) {
            Movie movie = database.get(i);
            // KMP pattern search over Title, Cast, and Description text fields
            if (KMPMatcher.search(movie.getTitle(), query) || 
                KMPMatcher.search(movie.getCast(), query) || 
                KMPMatcher.search(movie.getDescription(), query)) {
                results.add(movie);
            }
        }
        return results;
    }
}