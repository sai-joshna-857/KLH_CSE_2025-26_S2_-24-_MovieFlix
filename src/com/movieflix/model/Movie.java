package com.movieflix.model;

public class Movie {
    private int id;
    private String title;
    private String genre;
    private int year;
    private double rating;
    private String language;
    private String description;
    private String director;
    private String actors;
    private int popularity;
    private String posterUrl;

    public Movie(int id, String title, String genre, int year, double rating, 
                 String language, String description, String director, String actors, int popularity, String posterUrl) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.rating = rating;
        this.language = language;
        this.description = description;
        this.director = director;
        this.actors = actors;
        this.popularity = popularity;
        this.posterUrl = posterUrl;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getYear() { return year; }
    public double getRating() { return rating; }
    public String getLanguage() { return language; }
    public String getDescription() { return description; }
    public String getDirector() { return director; }
    public String getActors() { return actors; }
    public int getPopularity() { return popularity; }
    public String getPosterUrl() { return posterUrl; }

    @Override
    public String toString() {
        return "[" + id + "] " + title + " (" + year + ") | " + genre + " | Rating: " + rating;
    }
}