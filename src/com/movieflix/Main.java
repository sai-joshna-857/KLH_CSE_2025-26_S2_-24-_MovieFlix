package com.movieflix;

import com.movieflix.engine.MovieSearchEngine;
import com.movieflix.model.Movie;
import com.movieflix.util.CustomList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MovieSearchEngine engine = new MovieSearchEngine();
        
        // Load the document corpus
        engine.loadCorpus("data/movies.txt");

        Scanner scanner = new Scanner(System.in);
        System.out.println("=============================================");
        System.out.println("   MovieFlix KMP Pattern Matcher on Corpus   ");
        System.out.println("=============================================");

        while (true) {
            System.out.print("\nEnter pattern to search (or 'exit' to quit): ");
            String pattern = scanner.nextLine().trim();

            if (pattern.equalsIgnoreCase("exit")) break;

            long startTime = System.nanoTime();
            CustomList<Movie> results = engine.searchByPattern(pattern);
            long endTime = System.nanoTime();

            System.out.println("Found " + results.size() + " matches in " + ((endTime - startTime) / 1_000_000.0) + " ms:");
            for (int i = 0; i < results.size(); i++) {
                System.out.println(" -> " + results.get(i));
            }
        }
        scanner.close();
    }
}