package com.lundih.android.popularmovies;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class JSONUtils {
    // Receive String fro HTTP response and create Movie objects from it
    static List<Movie> extractMovies(Context context, String response) {
        List<Movie> movieList = new ArrayList<>();
        try {
            JSONObject movieListResponse = new JSONObject(response);
            JSONArray results = movieListResponse.getJSONArray(context.getString(R.string.json_key_array_results));
            for (int i = 0; i < results.length(); i++) {
                int movieID = results.optJSONObject(i).optInt(context.getString(R.string.json_key_id));
                String title = results.optJSONObject(i).optString(context.getString(R.string.json_key_title));
                String moviePoster = results.optJSONObject(i).optString(context.getString(R.string.json_key_poster_path));

                movieList.add(new Movie(movieID, title, moviePoster));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movieList;
    }

    static Movie extractMovieDetails(Context context, String response) {
        Movie movie = null;
        try {
            JSONObject movieResponse = new JSONObject(response);
            int movieID = movieResponse.optInt(context.getString(R.string.json_key_id));
            String title = movieResponse.optString(context.getString(R.string.json_key_title));
            String moviePoster = movieResponse.optString(context.getString(R.string.json_key_poster_path));
            int runtime = movieResponse.optInt(context.getString(R.string.json_key_runtime));
            double userRating = movieResponse.optInt(context.getString(R.string.json_key_user_rating));
            String releaseDate = movieResponse.optString(context.getString(R.string.json_key_release_date));
            JSONArray genres = movieResponse.optJSONArray(context.getString(R.string.json_key_array_genres));
            String tagline = movieResponse.optString(context.getString(R.string.json_key_tag_line));
            String plotSynopsis = movieResponse.optString(context.getString(R.string.json_key_plot_synopsis));

            List<String> genreList = new ArrayList<>();
            for (int i = 0; i < genres.length(); i++) {
                genreList.add(genres.optJSONObject(i).optString(context.getString(R.string.json_key_genre)));
            }

            movie = new Movie(movieID, title, moviePoster, runtime, userRating, releaseDate, genreList, tagline, plotSynopsis);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movie;
    }
}
