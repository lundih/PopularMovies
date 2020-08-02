package com.lundih.android.popularmovies;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class CustomViewModel extends AndroidViewModel {
    private LiveData<List<Movie>> favouriteMovies;
    private List<Movie> popularMovies = null;
    private List<Movie> highestRatedMovies = null;
    private List<Movie> trendingDailyMovies = null;
    private Movie movie = null;

    public CustomViewModel(@NonNull Application application) {
        super(application);

        FavouriteMoviesDatabase favouriteMoviesDatabase = FavouriteMoviesDatabase.getInstance(this.getApplication());
        favouriteMovies = favouriteMoviesDatabase.movieDAO().retrieveFavouriteMoviesLive();
    }

    public LiveData<List<Movie>> getFavouriteMovies() {
        return favouriteMovies;
    }

    public List<Movie> getPopularMovies() {
        return popularMovies;
    }

    public List<Movie> getHighestRatedMovies() {
        return highestRatedMovies;
    }

    public List<Movie> getTrendingDailyMovies() {
        return trendingDailyMovies;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setPopularMovies(List<Movie> popularMovies) {
        this.popularMovies = popularMovies;
    }

    public void setHighestRatedMovies(List<Movie> highestRatedMovies) {
        this.highestRatedMovies = highestRatedMovies;
    }

    public void setTrendingDailyMovies(List<Movie> trendingDailyMovies) {
        this.trendingDailyMovies = trendingDailyMovies;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
