package com.lundih.android.popularmovies;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "favouriteReviews")
public class Review {
    private int movieID;
    @PrimaryKey
    @NonNull private String reviewID;
    @ColumnInfo (name = "reviewAuthor")
    private String author;
    @ColumnInfo (name = "reviewContent")
    private String content;

    Review(int movieID, String reviewID, String author, String content){
        this.movieID = movieID;
        this.reviewID = reviewID;
        this.author = author;
        this.content = content;
    }

    public int getMovieID() {
        return movieID;
    }

    public String getReviewID() {
        return reviewID;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
