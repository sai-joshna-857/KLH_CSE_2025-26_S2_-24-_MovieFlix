package com.movieflix;

import com.movieflix.engine.MovieSearchEngine;
import com.movieflix.model.Movie;
import com.movieflix.util.CustomList;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebServer {
    private static MovieSearchEngine engine;

    public static void main(String[] args) throws IOException {
        engine = new MovieSearchEngine();

        // Sample movie database
        engine.addMovie(new Movie(1, "Interstellar", "Sci-Fi", "Matthew McConaughey", "Explorers travel through a wormhole in space."));
        engine.addMovie(new Movie(2, "Inception", "Sci-Fi, Action", "Leonardo DiCaprio", "A thief enters the dreams of others to steal secrets."));
        engine.addMovie(new Movie(3, "The Dark Knight", "Action, Crime", "Christian Bale", "Batman faces the Joker in Gotham City."));
        engine.addMovie(new Movie(4, "Dangal", "Biography, Sport", "Aamir Khan", "Former wrestler trains his daughters."));
        engine.addMovie(new Movie(5, "RRR", "Action, Drama", "Ram Charan, Jr. NTR", "A tale of two legendary revolutionaries."));

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new MovieHandler());
        server.setExecutor(null);

        System.out.println("==================================================");
        System.out.println(" MovieFlix Web UI running at: http://localhost:8080");
        System.out.println("==================================================");
        server.start();
    }

    static class MovieHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = "";
            String rawQuery = exchange.getRequestURI().getQuery();
            if (rawQuery != null && rawQuery.startsWith("query=")) {
                query = rawQuery.substring(6).replace("+", " ");
            }

            CustomList<Movie> results = engine.searchByTitle(query);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>MovieFlix Search</title>")
                .append("<style>")
                .append("body { font-family: 'Segoe UI', sans-serif; background-color: #141414; color: #fff; margin: 0; padding: 40px; }")
                .append(".container { max-width: 800px; margin: 0 auto; }")
                .append("h1 { color: #E50914; font-size: 2.5rem; margin-bottom: 20px; }")
                .append(".search-box { display: flex; gap: 10px; margin-bottom: 30px; }")
                .append("input[type=text] { flex: 1; padding: 12px 16px; border-radius: 4px; border: 1px solid #333; background: #222; color: #fff; font-size: 16px; }")
                .append("button { padding: 12px 24px; background-color: #E50914; color: #fff; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; font-size: 16px; }")
                .append(".card { background: #1f1f1f; padding: 20px; border-radius: 8px; margin-bottom: 15px; border-left: 4px solid #E50914; }")
                .append(".title { font-size: 1.25rem; font-weight: bold; margin-bottom: 6px; }")
                .append(".meta { color: #aaa; font-size: 0.9rem; margin-bottom: 10px; }")
                .append(".desc { color: #ddd; font-size: 0.95rem; line-height: 1.4; }")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<h1>MovieFlix</h1>")
                .append("<form method='GET' action='/' class='search-box'>")
                .append("<input type='text' name='query' placeholder='Search movies using KMP matching...' value='").append(query).append("'>")
                .append("<button type='submit'>Search</button>")
                .append("</form>")
                .append("<h3>Results (").append(results.size()).append(" matches)</h3>");

            for (int i = 0; i < results.size(); i++) {
                Movie m = results.get(i);
                html.append("<div class='card'>")
                    .append("<div class='title'>").append(m.getTitle()).append("</div>")
                    .append("<div class='meta'><b>Genre:</b> ").append(m.getGenre()).append(" | <b>Cast:</b> ").append(m.getCast()).append("</div>")
                    .append("<div class='desc'>").append(m.getDescription()).append("</div>")
                    .append("</div>");
            }

            html.append("</div></body></html>");

            byte[] responseBytes = html.toString().getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }
}