package com.movieflix.model;

public class Movie {
    private int id;
    private String title;
    private String genre;
    private String cast;
    private String description;

    public Movie(int id, String title, String genre, String cast, String description) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.cast = cast;
        this.description = description;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getCast() { return cast; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("[%d] %s | Genre: %s | Cast: %s", id, title, genre, cast);
    }
}
