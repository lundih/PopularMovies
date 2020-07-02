package com.lundih.android.popularmovies;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "favouriteMovies")
class Movie {
    @PrimaryKey
    private final int movieID;
    @ColumnInfo(name="movieTitle")
    private final String title;
    private final String moviePoster;
    private final int runtime;
    private final double userRating;
    private final String releaseDate;
    private final List<String> genre;
    private final String tagLine;
    private final String plotSynopsis;
    @Ignore private final List<Trailer> trailer;
    @Ignore private final List<Review> review;

    @Ignore Movie(int movieID, String title, String moviePoster) {
        this.movieID = movieID;
        this.title = title;
        this.moviePoster = moviePoster;
        this.runtime = -1;
        this.userRating = -1;
        this.releaseDate = null;
        this.genre = null;
        this.tagLine = null;
        this.plotSynopsis = null;
        this.trailer = null;
        this.review = null;
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
        this.trailer = null;
        this.review = null;
    }

    @Ignore Movie(int movieID, String title, String moviePoster, int runtime, double userRating, String releaseDate, List<String> genre, String tagLine, String plotSynopsis, List<Trailer> trailer, List<Review> review) {
        this.movieID = movieID;
        this.title = title;
        this.moviePoster = moviePoster;
        this.runtime = runtime;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.tagLine = tagLine;
        this.plotSynopsis = plotSynopsis;
        this.trailer = trailer;
        this.review = review;
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

    List<Trailer> getTrailer() {
        return trailer;
    }

    List<Review> getReview(){
        return review;
    }
}

