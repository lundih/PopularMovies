package com.lundih.android.popularmovies;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MovieDAO {
    // For in case the user wants to refresh the movie list manually
    @Query("SELECT * FROM favouriteMovies ORDER BY movieTitle")
    List<Movie> retrieveFavouriteMovies();

    // Use live data to update favourite movies
    @Query("SELECT * FROM favouriteMovies ORDER BY movieTitle")
    LiveData<List<Movie>> retrieveFavouriteMoviesLive();

    @Query("SELECT movieID FROM favouriteMovies WHERE movieID = :requestedMovieID ")
    int retrieveMovieID(int requestedMovieID);

    @Query("SELECT * FROM favouriteMovies WHERE movieID = :requestedMovieID")
    Movie retrieveFavouriteMovie(int requestedMovieID);

    @Query("SELECT * FROM favouriteTrailers WHERE movieID = :trailerMovieID")
    List<Trailer> retrieveFavouriteTrailers(int trailerMovieID);

    @Query("SELECT * FROM favouriteReviews WHERE movieID = :reviewMovieID")
    List<Review> retrieveFavouriteReviews(int reviewMovieID);

    @Insert
    void insertFavouriteMovie(Movie movie);

    @Insert
    void insertFavouriteTrailer(Trailer trailer);

    @Insert
    void insertFavouriteReview(Review review);

    @Update (onConflict = OnConflictStrategy.REPLACE)
    void updateFavouriteMovie(Movie movie);

    @Update (onConflict = OnConflictStrategy.REPLACE)
    void updateFavouriteTrailer(Trailer trailer);

    @Update (onConflict = OnConflictStrategy.REPLACE)
    void updateFavouriteReview(Review review);

    @Delete
    void deleteFavouriteMovie(Movie movie);

    @Delete
    void deleteFavouriteTrailer(Trailer trailer);

    @Delete
    void deleteFavouriteReview(Review review);

}