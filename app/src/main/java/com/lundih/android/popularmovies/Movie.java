package com.lundih.android.popularmovies;

import java.util.List;

class Movie {
    private final int movieID;
    private final String title;
    private final String moviePoster;
    private final int runtime;
    private final double userRating;
    private final String releaseDate;
    private final List<String> genre;
    private final String tagLine;
    private final String plotSynopsis;

    Movie(int movieID, String title, String moviePoster) {
        this.movieID = movieID;
        this.title = title;
        this.moviePoster = moviePoster;
        this.runtime = -1;
        this.userRating = -1;
        this.releaseDate = null;
        this.genre = null;
        this.tagLine = null;
        this.plotSynopsis = null;
    }

    Movie(int movieID, String title, String moviePoster, int runtime, double userRating, String releaseDate, List<String> genre, String tagLine, String plotSynopsis) {
        this.movieID = movieID;
        this.title = title;
        this.moviePoster = moviePoster;
        this.runtime = runtime;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.tagLine = tagLine;
        this.plotSynopsis = plotSynopsis;
    }

    int getMovieID() {
        return movieID;
    }

    String getTitle() {
        return title;
    }

    String getMoviePoster() {
        return moviePoster;
    }

    int getRuntime() {
        return runtime;
    }

    double getUserRating() {
        return userRating;
    }

    String getReleaseDate() {
        return releaseDate;
    }

    List<String> getGenre() {
        return genre;
    }

    String getTagLine() {
        return tagLine;
    }

    String getPlotSynopsis() {
        return plotSynopsis;
    }
}

