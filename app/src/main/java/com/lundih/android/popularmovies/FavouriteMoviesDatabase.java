package com.lundih.android.popularmovies;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Movie.class, Trailer.class, Review.class}, version = 1, exportSchema = false)
@TypeConverters(GenreConverter.class)
public abstract class FavouriteMoviesDatabase extends RoomDatabase {
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME ="popularMoviesFavourites";
    private static FavouriteMoviesDatabase thisInstance;

    public static FavouriteMoviesDatabase getInstance(Context context) {
        if (thisInstance == null) {
            synchronized (LOCK) {
                thisInstance = Room.databaseBuilder(context.getApplicationContext(), FavouriteMoviesDatabase.class, FavouriteMoviesDatabase.DATABASE_NAME).build();
            }
        }

        return  thisInstance;
    }

    public abstract MovieDAO movieDAO();
}
