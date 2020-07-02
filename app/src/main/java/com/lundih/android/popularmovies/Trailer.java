package com.lundih.android.popularmovies;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favouriteTrailers")
public class Trailer {
    private int movieID;
    @PrimaryKey
    @NonNull private String trailerID;
    @ColumnInfo(name = "trailerTitle")
    private String title;
    private String key;
    private String site;
    private String type;

    Trailer(int movieID, String trailerID, String title, String key, String site, String type){
        this.movieID = movieID;
        this.trailerID = trailerID;
        this.title = title;
        this.key = key;
        this.site = site;
        this.type = type;
    }

    public int getMovieID() {
        return movieID;
    }

    public String getTrailerID() {
        return trailerID;
    }

    public String getTitle() {
        return title;
    }

    public String getKey() {
        return key;
    }

    public String getSite() {
        return site;
    }

    public String getType() {
        return type;
    }
}
