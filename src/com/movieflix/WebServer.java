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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class WebServer {
    private static MovieSearchEngine engine;

    public static void main(String[] args) throws IOException {
        engine = new MovieSearchEngine();
        engine.loadCorpus("data/movies.txt");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new IndexHandler());
        server.createContext("/search", new SearchHandler());
        server.setExecutor(null);
        System.out.println("MovieFlix Web UI running at http://localhost:8080");
        server.start();
    }

    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>"
                    + "<html lang='en'>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<title>MovieFlix</title>"
                    + "<style>"
                    + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #0f172a; color: #f8fafc; margin: 0; padding: 40px; display: flex; flex-direction: column; align-items: center; }"
                    + ".container { width: 100%; max-width: 850px; background: #1e293b; padding: 30px; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }"
                    + "h1 { color: #e11d48; margin: 0 0 20px 0; font-size: 2.2rem; }"
                    + ".search-box { display: flex; gap: 10px; margin-bottom: 25px; }"
                    + "input[type='text'] { flex: 1; padding: 12px 16px; border-radius: 8px; border: 1px solid #475569; background: #0f172a; color: white; font-size: 1rem; outline: none; }"
                    + "input[type='text']:focus { border-color: #e11d48; }"
                    + "button { background: #e11d48; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-weight: bold; cursor: pointer; transition: 0.2s; }"
                    + "button:hover { background: #be123c; }"
                    + ".card { background: #0f172a; border-left: 4px solid #e11d48; padding: 16px; margin-bottom: 12px; border-radius: 4px; }"
                    + ".card h3 { margin: 0 0 6px 0; color: #f1f5f9; }"
                    + ".meta { font-size: 0.9rem; color: #94a3b8; margin-bottom: 8px; }"
                    + ".desc { font-size: 0.95rem; color: #cbd5e1; line-height: 1.4; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<h1>MovieFlix</h1>"
                    + "<div class='search-box'>"
                    + "<input type='text' id='query' placeholder='Search movies, actors, genres, or descriptions...' oninput='performSearch()'>"
                    + "<button onclick='performSearch()'>Search</button>"
                    + "</div>"
                    + "<div id='results'></div>"
                    + "</div>"
                    + "<script>"
                    + "function performSearch() {"
                    + "  const q = document.getElementById('query').value;"
                    + "  fetch('/search?q=' + encodeURIComponent(q))"
                    + "    .then(res => res.json())"
                    + "    .then(data => {"
                    + "      let html = '';"
                    + "      data.forEach(m => {"
                    + "        html += `<div class='card'>"
                    + "          <h3>${m.title}</h3>"
                    + "          <div class='meta'><strong>Genre:</strong> ${m.genre} | <strong>Cast:</strong> ${m.cast}</div>"
                    + "          <div class='desc'>${m.description}</div>"
                    + "        </div>`;"
                    + "      });"
                    + "      if(data.length === 0) html = '<p style=\"color:#94a3b8;\">No movies found matching pattern.</p>';"
                    + "      document.getElementById('results').innerHTML = html;"
                    + "    });"
                    + "}"
                    + "performSearch();"
                    + "</script>"
                    + "</body>"
                    + "</html>";

            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = "";
            String rawQuery = exchange.getRequestURI().getRawQuery();
            if (rawQuery != null && rawQuery.startsWith("q=")) {
                query = URLDecoder.decode(rawQuery.substring(2), StandardCharsets.UTF_8.name());
            }

            CustomList<Movie> matches = engine.searchByPattern(query);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < matches.size(); i++) {
                Movie m = matches.get(i);
                json.append("{")
                    .append("\"id\":").append(m.getId()).append(",")
                    .append("\"title\":\"").append(escapeJson(m.getTitle())).append("\",")
                    .append("\"genre\":\"").append(escapeJson(m.getGenre())).append("\",")
                    .append("\"cast\":\"").append(escapeJson(m.getCast())).append("\",")
                    .append("\"description\":\"").append(escapeJson(m.getDescription())).append("\"")
                    .append("}");
                if (i < matches.size() - 1) json.append(",");
            }
            json.append("]");

            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }

        private String escapeJson(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}