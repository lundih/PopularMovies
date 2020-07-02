package com.lundih.android.popularmovies;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenreConverter {
    @TypeConverter
    public static String fromList(List<String> genreList) {
        // Convert a list of genres to a string for storage in the database
        String genresString = "";
        if (genreList != null)
            for (int i = 0; i < genreList.size(); i++) {
                if(i != genreList.size() - 1)
                    genresString = genresString.concat(genreList.get(i)).concat(MainActivity.getTheContext().getString(R.string.comma));
                else
                    genresString = genresString.concat(genreList.get(i));
            }

        return genresString;
    }

    @TypeConverter
    public static List<String> fromString(String genresString) {
        // Convert the string of genres in the database to a list
        List<String> genreList = null;
        if (genresString != null && !genresString.equals("")) {
            String[] genres = genresString.split(MainActivity.getTheContext().getString(R.string.comma));
            genreList = new ArrayList<>(Arrays.asList(genres));
        }

        return genreList;
    }
}
