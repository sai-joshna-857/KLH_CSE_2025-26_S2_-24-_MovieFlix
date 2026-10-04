package com.movieflix;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.movieflix.engine.*;
import com.movieflix.model.Movie;
import com.movieflix.util.CustomList;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WebServer {
    private static MovieSearchEngine engine;

    public static void main(String[] args) throws IOException {
        engine = new MovieSearchEngine();
        engine.loadMovies("data/movies.csv");

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new UIHandler());
        server.createContext("/api/movies", new MoviesApiHandler());
        server.createContext("/api/search", new SearchApiHandler());
        server.createContext("/api/similar", new SimilarityApiHandler());
        server.createContext("/api/vibe", new VibeApiHandler());
        server.createContext("/api/schedule", new ScheduleApiHandler());

        server.setExecutor(null);
        System.out.println("==================================================");
        System.out.println(" MovieFlix Live at: http://localhost:" + port);
        System.out.println("==================================================");
        server.start();
    }

    static class UIHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            String html = getMovieFlixUI();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class MoviesApiHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            CustomList<Movie> all = engine.getAllMovies();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < all.size(); i++) {
                if (i > 0) json.append(",");
                json.append(movieToJson(all.get(i)));
            }
            json.append("]");
            sendJson(exchange, json.toString());
        }
    }

    static class SearchApiHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String query = "";
            String rawQuery = exchange.getRequestURI().getQuery();
            if (rawQuery != null && rawQuery.startsWith("q=")) {
                query = URLDecoder.decode(rawQuery.substring(2), StandardCharsets.UTF_8).trim();
            }

            CustomList<Movie> all = engine.getAllMovies();
            CustomList<Movie> matched = new CustomList<>();

            if (!query.isEmpty()) {
                for (int i = 0; i < all.size(); i++) {
                    Movie m = all.get(i);
                    boolean match = KMPMatcher.search(m.getTitle(), query).found
                                 || KMPMatcher.search(m.getActors(), query).found
                                 || KMPMatcher.search(m.getGenre(), query).found
                                 || KMPMatcher.search(m.getDirector(), query).found;
                    if (match) {
                        matched.add(m);
                    }
                }

                if (matched.size() == 0) {
                    int minDst = Integer.MAX_VALUE;
                    Movie bestSuggestion = null;
                    for (int i = 0; i < all.size(); i++) {
                        Movie m = all.get(i);
                        int dst = EditDistance.compute(query.toLowerCase(), m.getTitle().toLowerCase());
                        if (dst < minDst) {
                            minDst = dst;
                            bestSuggestion = m;
                        }
                    }
                    if (bestSuggestion != null && minDst <= Math.max(2, query.length() / 2)) {
                        matched.add(bestSuggestion);
                    }
                }
            } else {
                for (int i = 0; i < all.size(); i++) {
                    matched.add(all.get(i));
                }
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < matched.size(); i++) {
                if (i > 0) json.append(",");
                json.append(movieToJson(matched.get(i)));
            }
            json.append("]");
            sendJson(exchange, json.toString());
        }
    }

    static class SimilarityApiHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            int targetId = 101;
            String rawQuery = exchange.getRequestURI().getQuery();
            if (rawQuery != null && rawQuery.startsWith("id=")) {
                try { targetId = Integer.parseInt(rawQuery.substring(3)); } catch (Exception ignored) {}
            }

            CustomList<Movie> all = engine.getAllMovies();
            Movie base = null;
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getId() == targetId) {
                    base = all.get(i);
                    break;
                }
            }
            if (base == null && all.size() > 0) base = all.get(0);

            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (int i = 0; i < all.size(); i++) {
                Movie candidate = all.get(i);
                if (candidate.getId() == base.getId()) continue;
                if (candidate.getGenre().equalsIgnoreCase(base.getGenre())) {
                    if (!first) json.append(",");
                    json.append(movieToJson(candidate));
                    first = false;
                }
            }
            json.append("]");
            sendJson(exchange, json.toString());
        }
    }

    static class VibeApiHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String mood = "Action";
            String rawQuery = exchange.getRequestURI().getQuery();
            if (rawQuery != null && rawQuery.startsWith("mood=")) {
                mood = URLDecoder.decode(rawQuery.substring(5), StandardCharsets.UTF_8).trim();
            }

            CustomList<Movie> all = engine.getAllMovies();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (int i = 0; i < all.size(); i++) {
                Movie m = all.get(i);
                if (m.getGenre().toLowerCase().contains(mood.toLowerCase())) {
                    if (!first) json.append(",");
                    json.append(movieToJson(m));
                    first = false;
                }
            }
            json.append("]");
            sendJson(exchange, json.toString());
        }
    }

    static class ScheduleApiHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            double maxHours = 6.0;
            String rawQuery = exchange.getRequestURI().getQuery();
            if (rawQuery != null && rawQuery.startsWith("hours=")) {
                try { maxHours = Double.parseDouble(rawQuery.substring(6)); } catch (Exception ignored) {}
            }

            CustomList<Movie> all = engine.getAllMovies();
            List<KnapsackApproximation.Item> items = new ArrayList<>();
            for (int i = 0; i < all.size(); i++) {
                Movie m = all.get(i);
                items.add(new KnapsackApproximation.Item(2.0, m.getRating()));
            }

            double optimalScore = KnapsackApproximation.greedyApproximation(items, maxHours);
            String json = "{\"max_hours\":" + maxHours + ", \"optimized_score\":" + optimalScore + "}";
            sendJson(exchange, json);
        }
    }

    private static String movieToJson(Movie m) {
        return "{\"id\":" + m.getId() +
                ",\"title\":\"" + escape(m.getTitle()) + "\"" +
                ",\"genre\":\"" + escape(m.getGenre()) + "\"" +
                ",\"year\":" + m.getYear() +
                ",\"rating\":" + m.getRating() +
                ",\"language\":\"" + escape(m.getLanguage()) + "\"" +
                ",\"description\":\"" + escape(m.getDescription()) + "\"" +
                ",\"director\":\"" + escape(m.getDirector()) + "\"" +
                ",\"actors\":\"" + escape(m.getActors()) + "\"" +
                ",\"popularity\":" + m.getPopularity() +
                ",\"poster\":\"" + escape(m.getPosterUrl()) + "\"}";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, b.length);
        try (OutputStream os = exchange.getResponseBody()) { 
            os.write(b); 
        }
    }

    private static String getMovieFlixUI() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>MovieFlix - 100 Verified Blockbusters</title>\n" +
                "  <style>\n" +
                "    :root {\n" +
                "      --primary: #ff7a00;\n" +
                "      --primary-hover: #e06900;\n" +
                "      --bg: #0b0d10;\n" +
                "      --surface: #14171d;\n" +
                "      --surface-light: #1e222a;\n" +
                "      --border: rgba(255, 255, 255, 0.08);\n" +
                "      --text: #f0f2f5;\n" +
                "      --text-muted: #8b929a;\n" +
                "    }\n" +
                "    * { margin:0; padding:0; box-sizing:border-box; font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }\n" +
                "    body { background-color: var(--bg); color: var(--text); min-height: 100vh; overflow-x: hidden; }\n" +
                "    header {\n" +
                "      display: flex; align-items: center; justify-content: space-between;\n" +
                "      padding: 16px 48px; background: rgba(11, 13, 16, 0.95); backdrop-filter: blur(12px);\n" +
                "      position: sticky; top: 0; z-index: 100; border-bottom: 1px solid var(--border);\n" +
                "    }\n" +
                "    .logo-area { display: flex; align-items: center; gap: 24px; }\n" +
                "    .brand { font-size: 26px; font-weight: 900; color: #fff; letter-spacing: -0.5px; text-decoration: none; }\n" +
                "    .brand span { color: var(--primary); }\n" +
                "    .location-badge { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--text-muted); background: var(--surface); padding: 5px 12px; border-radius: 20px; border: 1px solid var(--border); }\n" +
                "    nav { display: flex; align-items: center; gap: 24px; font-size: 14px; font-weight: 600; }\n" +
                "    nav a { color: var(--text-muted); text-decoration: none; transition: 0.2s; cursor: pointer; }\n" +
                "    nav a:hover, nav a.active { color: #fff; }\n" +
                "    .search-bar { position: relative; width: 340px; }\n" +
                "    .search-bar input {\n" +
                "      width: 100%; background: var(--surface); border: 1px solid var(--border); border-radius: 24px;\n" +
                "      padding: 9px 18px 9px 38px; color: #fff; font-size: 13px; outline: none; transition: 0.2s;\n" +
                "    }\n" +
                "    .search-bar input:focus { border-color: var(--primary); background: var(--surface-light); }\n" +
                "    .search-bar svg { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); fill: var(--text-muted); width: 14px; height: 14px; }\n" +
                "    .hero-container { position: relative; padding: 24px 48px 0; max-width: 1440px; margin: 0 auto; }\n" +
                "    .hero-card {\n" +
                "      position: relative; border-radius: 16px; overflow: hidden; min-height: 480px;\n" +
                "      background: #111419; display: flex;\n" +
                "      border: 1px solid var(--border); box-shadow: 0 20px 40px rgba(0,0,0,0.6);\n" +
                "    }\n" +
                "    .hero-backdrop {\n" +
                "      position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover;\n" +
                "      filter: brightness(0.55) contrast(1.1); transition: opacity 0.4s ease;\n" +
                "    }\n" +
                "    .hero-gradient { position: absolute; inset: 0; background: linear-gradient(90deg, #0b0d10 0%, rgba(11,13,16,0.85) 45%, rgba(11,13,16,0.3) 100%); }\n" +
                "    .hero-content {\n" +
                "      position: relative; z-index: 2; width: 60%; padding: 44px; display: flex; flex-direction: column;\n" +
                "      justify-content: flex-end;\n" +
                "    }\n" +
                "    .hero-rank { color: var(--primary); font-size: 24px; font-weight: 900; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 1px; }\n" +
                "    .hero-title { font-size: 44px; font-weight: 900; line-height: 1.1; margin-bottom: 12px; color: #fff; }\n" +
                "    .hero-meta { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; font-size: 13px; }\n" +
                "    .rating-badge { background: #ffbb00; color: #000; font-weight: 800; padding: 3px 8px; border-radius: 4px; display: flex; align-items: center; gap: 4px; }\n" +
                "    .hero-desc { color: #c0c6cc; font-size: 14px; line-height: 1.6; margin-bottom: 24px; max-width: 540px; }\n" +
                "    .hero-actions { display: flex; gap: 14px; }\n" +
                "    .btn-primary { background: var(--primary); color: #fff; border: none; padding: 12px 28px; border-radius: 24px; font-weight: 700; cursor: pointer; font-size: 14px; transition: 0.2s; }\n" +
                "    .btn-primary:hover { background: var(--primary-hover); transform: translateY(-2px); }\n" +
                "    .btn-secondary { background: rgba(255,255,255,0.1); backdrop-filter: blur(8px); color: #fff; border: 1px solid rgba(255,255,255,0.15); padding: 12px 24px; border-radius: 24px; font-weight: 700; cursor: pointer; font-size: 14px; transition: 0.2s; }\n" +
                "    .btn-secondary:hover { background: rgba(255,255,255,0.2); }\n" +
                "    .hero-sidebar {\n" +
                "      position: relative; z-index: 2; width: 40%; padding: 40px; border-left: 1px solid rgba(255,255,255,0.06);\n" +
                "      background: rgba(16, 19, 24, 0.5); backdrop-filter: blur(12px); display: flex; flex-direction: column; justify-content: center;\n" +
                "    }\n" +
                "    .sidebar-section-title { font-size: 12px; font-weight: 700; color: var(--primary); text-transform: uppercase; letter-spacing: 1.5px; margin-bottom: 16px; }\n" +
                "    .cast-grid { display: flex; flex-direction: column; gap: 10px; margin-bottom: 24px; }\n" +
                "    .person-pill {\n" +
                "      background: rgba(255, 255, 255, 0.05); border: 1px solid var(--border);\n" +
                "      border-radius: 8px; padding: 10px 14px; display: flex;\n" +
                "      justify-content: space-between; align-items: center;\n" +
                "    }\n" +
                "    .person-name { font-size: 13px; font-weight: 700; color: #fff; }\n" +
                "    .person-role { font-size: 11px; font-weight: 700; color: var(--primary); text-transform: uppercase; letter-spacing: 0.5px; }\n" +
                "    .vibe-card {\n" +
                "      position: absolute; bottom: 30px; right: 60px; z-index: 10; background: rgba(20, 23, 29, 0.95);\n" +
                "      border: 1px solid rgba(255, 122, 0, 0.3); border-radius: 14px; padding: 18px 22px; width: 320px;\n" +
                "      box-shadow: 0 16px 32px rgba(0,0,0,0.8); backdrop-filter: blur(16px);\n" +
                "    }\n" +
                "    .vibe-header { display: flex; align-items: center; gap: 8px; font-size: 11px; font-weight: 800; color: var(--primary); text-transform: uppercase; margin-bottom: 4px; }\n" +
                "    .vibe-title { font-size: 15px; font-weight: 700; color: #fff; margin-bottom: 6px; }\n" +
                "    .vibe-desc { font-size: 12px; color: var(--text-muted); line-height: 1.4; margin-bottom: 14px; }\n" +
                "    .vibe-actions { display: flex; gap: 8px; }\n" +
                "    .btn-vibe { background: var(--primary); color: #fff; border: none; padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: 700; cursor: pointer; flex: 1; }\n" +
                "    .section { max-width: 1440px; margin: 40px auto 0; padding: 0 48px; }\n" +
                "    .section-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 18px; }\n" +
                "    .section-title { font-size: 22px; font-weight: 800; color: #fff; letter-spacing: -0.3px; }\n" +
                "    .section-subtitle { font-size: 13px; color: var(--text-muted); margin-top: 2px; }\n" +
                "    .movie-scroll { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 22px; }\n" +
                "    .movie-poster-card {\n" +
                "      background: var(--surface); border-radius: 12px; overflow: hidden; border: 1px solid var(--border);\n" +
                "      cursor: pointer; transition: transform 0.25s, border-color 0.25s; display: flex; flex-direction: column;\n" +
                "    }\n" +
                "    .movie-poster-card:hover { transform: translateY(-6px); border-color: rgba(255, 122, 0, 0.5); }\n" +
                "    .poster-media { height: 290px; background: #1a1e24; position: relative; overflow: hidden; }\n" +
                "    .poster-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s ease; }\n" +
                "    .movie-poster-card:hover .poster-img { transform: scale(1.05); }\n" +
                "    .poster-tag { position: absolute; top: 10px; left: 10px; background: rgba(0,0,0,0.75); backdrop-filter: blur(6px); padding: 3px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; color: var(--primary); z-index: 2; }\n" +
                "    .poster-info { padding: 14px; display: flex; flex-direction: column; flex: 1; justify-content: space-between; }\n" +
                "    .poster-title { font-size: 15px; font-weight: 700; color: #fff; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }\n" +
                "    .poster-genre { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; }\n" +
                "    .poster-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; font-weight: 600; color: #a1aab3; border-top: 1px solid var(--border); padding-top: 8px; }\n" +
                "    footer { margin-top: 60px; padding: 32px 48px; border-top: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: var(--text-muted); }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<header>\n" +
                "  <div class=\"logo-area\">\n" +
                "    <a href=\"/\" class=\"brand\">Movie<span>Flix</span></a>\n" +
                "    <div class=\"location-badge\">\n" +
                "      <svg width=\"12\" height=\"12\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M12 2a8 8 0 0 0-8 8c0 5.25 8 12 8 12s8-6.75 8-12a8 8 0 0 0-8-8z\"/><circle cx=\"12\" cy=\"10\" r=\"3\"/></svg>\n" +
                "      100 Masterpieces Archive\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <nav>\n" +
                "    <a href=\"#\" class=\"active\">Boxoffice</a>\n" +
                "  </nav>\n" +
                "  <div class=\"search-bar\">\n" +
                "    <svg viewBox=\"0 0 24 24\"><circle cx=\"11\" cy=\"11\" r=\"8\" stroke=\"currentColor\" stroke-width=\"2\" fill=\"none\"/><line x1=\"21\" y1=\"21\" x2=\"16.65\" y2=\"16.65\" stroke=\"currentColor\" stroke-width=\"2\"/></svg>\n" +
                "    <input type=\"text\" id=\"searchInput\" placeholder=\"Search with KMP & Edit Distance...\" oninput=\"onSearch(this.value)\">\n" +
                "  </div>\n" +
                "</header>\n" +
                "<div class=\"hero-container\">\n" +
                "  <div class=\"hero-card\" id=\"spotlightCard\">\n" +
                "    <img id=\"heroBackdrop\" class=\"hero-backdrop\" src=\"https://image.tmdb.org/t/p/w1280/hkBaDkMWbLaf8B1rsqGqQCKyxmr.jpg\" alt=\"Hero Backdrop\">\n" +
                "    <div class=\"hero-gradient\"></div>\n" +
                "    <div class=\"hero-content\">\n" +
                "      <div class=\"hero-rank\" id=\"heroRank\">#1 SPOTLIGHT</div>\n" +
                "      <h1 class=\"hero-title\" id=\"heroTitle\">The Dark Knight</h1>\n" +
                "      <div class=\"hero-meta\">\n" +
                "        <div class=\"rating-badge\" id=\"heroRating\">★ 9.0</div>\n" +
                "        <span id=\"heroYear\">2008</span> • \n" +
                "        <span id=\"heroGenre\">Action</span> • \n" +
                "        <span id=\"heroLang\">English</span>\n" +
                "      </div>\n" +
                "      <p class=\"hero-desc\" id=\"heroDesc\">Batman battles the Joker to save Gotham City from organized chaos.</p>\n" +
                "      <div class=\"hero-actions\">\n" +
                "        <button class=\"btn-primary\" onclick=\"findSimilarCurrent()\">Explore Similar</button>\n" +
                "        <button class=\"btn-secondary\" onclick=\"document.getElementById('movieGrid').scrollIntoView({behavior:'smooth'})\">Browse All 100</button>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "    <div class=\"hero-sidebar\">\n" +
                "      <div class=\"sidebar-section-title\">Director & Cast</div>\n" +
                "      <div class=\"cast-grid\" id=\"heroCastList\"></div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <div class=\"vibe-card\" id=\"vibePrompt\">\n" +
                "    <div class=\"vibe-header\">✨ MovieFlix Matcher</div>\n" +
                "    <div class=\"vibe-title\">What's Your Vibe?</div>\n" +
                "    <div class=\"vibe-desc\">Pick your mood and our engine curates the ideal movie for your evening right now.</div>\n" +
                "    <div class=\"vibe-actions\">\n" +
                "      <button class=\"btn-vibe\" onclick=\"askVibe()\">Try Now ✨</button>\n" +
                "      <button class=\"btn-secondary\" style=\"padding:6px 12px; font-size:11px;\" onclick=\"document.getElementById('vibePrompt').style.display='none'\">Not Now</button>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n" +
                "<section class=\"section\">\n" +
                "  <div class=\"section-header\">\n" +
                "    <div>\n" +
                "      <h2 class=\"section-title\" id=\"shelfTitle\">Top 100 Archive</h2>\n" +
                "      <p class=\"section-subtitle\" id=\"shelfSubtitle\">Click any movie to spotlight it above</p>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <div class=\"movie-scroll\" id=\"movieGrid\"></div>\n" +
                "</section>\n" +
                "<footer>\n" +
                "  <div>&copy; 2026 MovieFlix Media Inc. All Rights Reserved.</div>\n" +
                "  <div style=\"color:var(--primary); font-weight:600;\">Advanced Algorithms Powered (KMP, DP, MaxFlow)</div>\n" +
                "</footer>\n" +
                "<script>\n" +
                "  let currentHero = null;\n" +
                "  let allMovies = [];\n" +
                "  const backdrops = {\n" +
                "    'The Dark Knight': 'https://image.tmdb.org/t/p/w1280/hkBaDkMWbLaf8B1rsqGqQCKyxmr.jpg',\n" +
                "    'Inception': 'https://image.tmdb.org/t/p/w1280/s3TBrRGB1iav7gFOCNx3H31MoES.jpg',\n" +
                "    'Interstellar': 'https://image.tmdb.org/t/p/w1280/xJHokMbljvjADYdit5fK5VQsXEG.jpg'\n" +
                "  };\n" +
                "  async function init() {\n" +
                "    const res = await fetch('/api/movies');\n" +
                "    allMovies = await res.json();\n" +
                "    if (allMovies.length > 0) {\n" +
                "      setSpotlight(allMovies[0]);\n" +
                "      renderShelf(allMovies, 'Top 100 Archive', 'Click any movie to spotlight it above');\n" +
                "    }\n" +
                "  }\n" +
                "  function setSpotlight(movie) {\n" +
                "    currentHero = movie;\n" +
                "    const backdropImg = document.getElementById('heroBackdrop');\n" +
                "    backdropImg.src = backdrops[movie.title] || movie.poster;\n" +
                "    document.getElementById('heroTitle').innerText = movie.title;\n" +
                "    document.getElementById('heroRating').innerText = '★ ' + movie.rating;\n" +
                "    document.getElementById('heroYear').innerText = movie.year;\n" +
                "    document.getElementById('heroGenre').innerText = movie.genre;\n" +
                "    document.getElementById('heroLang').innerText = movie.language;\n" +
                "    document.getElementById('heroDesc').innerText = movie.description;\n" +
                "    const castContainer = document.getElementById('heroCastList');\n" +
                "    castContainer.innerHTML = '';\n" +
                "    castContainer.appendChild(createPersonCard(movie.director, 'Director'));\n" +
                "    const actorNames = movie.actors.split(';');\n" +
                "    actorNames.forEach((act) => {\n" +
                "      if (act.trim()) castContainer.appendChild(createPersonCard(act.trim(), 'Actor'));\n" +
                "    });\n" +
                "  }\n" +
                "  function createPersonCard(name, role) {\n" +
                "    const div = document.createElement('div');\n" +
                "    div.className = 'person-pill';\n" +
                "    div.innerHTML = '<div class=\"person-name\">' + name + '</div><div class=\"person-role\">' + role + '</div>';\n" +
                "    return div;\n" +
                "  }\n" +
                "  function renderShelf(movies, title, subtitle) {\n" +
                "    document.getElementById('shelfTitle').innerText = title;\n" +
                "    document.getElementById('shelfSubtitle').innerText = subtitle;\n" +
                "    const grid = document.getElementById('movieGrid');\n" +
                "    grid.innerHTML = '';\n" +
                "    if (movies.length === 0) {\n" +
                "      grid.innerHTML = '<p style=\"color:var(--text-muted); grid-column: 1/-1; padding: 20px 0;\">No movies found matching your query.</p>';\n" +
                "      return;\n" +
                "    }\n" +
                "    movies.forEach(m => {\n" +
                "      const card = document.createElement('div');\n" +
                "      card.className = 'movie-poster-card';\n" +
                "      card.onclick = () => {\n" +
                "        setSpotlight(m);\n" +
                "        window.scrollTo({top: 0, behavior: 'smooth'});\n" +
                "      };\n" +
                "      card.innerHTML = '<div class=\"poster-media\" style=\"background: linear-gradient(135deg, #1e222a 0%, #111419 100%); display: flex; align-items: center; justify-content: center; position: relative;\">' +\n" +
                "        '<div class=\"poster-tag\">' + m.genre + '</div>' +\n" +
                "        '<img class=\"poster-img\" src=\"' + m.poster + '\" alt=\"' + m.title + '\" loading=\"lazy\" style=\"width:100%; height:100%; object-fit:cover; position:absolute; inset:0;\">' +\n" +
                "        '<div style=\"padding: 20px; text-align: center; color: #8b929a; font-weight: 800; font-size: 14px;\">' + m.title + '</div>' +\n" +
                "      '</div>' +\n" +
                "      '<div class=\"poster-info\"><div>' +\n" +
                "      '<div class=\"poster-title\" title=\"' + m.title + '\">' + m.title + '</div>' +\n" +
                "      '<div class=\"poster-genre\">' + m.language + ' • ' + m.year + '</div></div>' +\n" +
                "      '<div class=\"poster-footer\"><span style=\"color:#ffbb00;\">★ ' + m.rating + '</span><span>' + m.director + '</span></div></div>';\n" +
                "      grid.appendChild(card);\n" +
                "    });\n" +
                "  }\n" +
                "  let searchDebounce = null;\n" +
                "  function onSearch(val) {\n" +
                "    clearTimeout(searchDebounce);\n" +
                "    searchDebounce = setTimeout(async () => {\n" +
                "      const res = await fetch('/api/search?q=' + encodeURIComponent(val));\n" +
                "      const results = await res.json();\n" +
                "      renderShelf(results, val ? 'Results for \"' + val + '\" (KMP / Edit Distance)' : 'Top 100 Archive', results.length + ' movies available');\n" +
                "    }, 150);\n" +
                "  }\n" +
                "  async function askVibe() {\n" +
                "    const mood = prompt('What genre or vibe are you in the mood for? (e.g. Sci-Fi, Crime, Action, Drama, Animation)', 'Sci-Fi');\n" +
                "    if (!mood) return;\n" +
                "    const res = await fetch('/api/vibe?mood=' + encodeURIComponent(mood));\n" +
                "    const results = await res.json();\n" +
                "    renderShelf(results, 'Vibe: ' + mood, 'Curated for your current mood');\n" +
                "    if (results.length > 0) setSpotlight(results[0]);\n" +
                "  }\n" +
                "  async function findSimilarCurrent() {\n" +
                "    if (!currentHero) return;\n" +
                "    const res = await fetch('/api/similar?id=' + currentHero.id);\n" +
                "    const results = await res.json();\n" +
                "    renderShelf(results, 'More Like \"' + currentHero.title + '\"', 'Recommendations based on shared themes and director');\n" +
                "    window.scrollTo({top: 520, behavior: 'smooth'});\n" +
                "  }\n" +
                "  init();\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }
}