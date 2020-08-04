package com.lundih.android.popularmovies;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class JSONUtils {
    // Receive String from HTTP response and create Movie objects from it
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

    static Movie extractMovieDetails(Context context, String detailsResponse, String trailersResponse, String reviewsResponse) {
        Movie movie = null;
        try {
            JSONObject movieDetailsResponse = new JSONObject(detailsResponse);
            int movieID = movieDetailsResponse.optInt(context.getString(R.string.json_key_id));
            String title = movieDetailsResponse.optString(context.getString(R.string.json_key_title));
            String moviePoster = movieDetailsResponse.optString(context.getString(R.string.json_key_poster_path));
            int runtime = movieDetailsResponse.optInt(context.getString(R.string.json_key_runtime));
            double userRating = movieDetailsResponse.optDouble(context.getString(R.string.json_key_user_rating));
            String releaseDate = movieDetailsResponse.optString(context.getString(R.string.json_key_release_date));
            JSONArray genres = movieDetailsResponse.optJSONArray(context.getString(R.string.json_key_array_genres));
            String tagline = movieDetailsResponse.optString(context.getString(R.string.json_key_tag_line));
            String plotSynopsis = movieDetailsResponse.optString(context.getString(R.string.json_key_plot_synopsis));
            List<String> genreList = new ArrayList<>();
            for (int i = 0; i < genres.length(); i++) {
                genreList.add(genres.optJSONObject(i).optString(context.getString(R.string.json_key_genre)));
            }

            JSONObject movieTrailersResponse = new JSONObject(trailersResponse);
            JSONArray trailers = movieTrailersResponse.optJSONArray(context.getString(R.string.json_key_array_results));
            List<Trailer> trailerList = new ArrayList<>();
            for (int i = 0; i < trailers.length(); i++) {
                String trailerID = trailers.optJSONObject(i).optString(context.getString(R.string.json_key_id));
                String trailerTitle = trailers.optJSONObject(i).optString(context.getString(R.string.json_key_trailer_title));
                String key = trailers.optJSONObject(i).optString(context.getString(R.string.json_key_trailer_key));
                String site = trailers.optJSONObject(i).optString(context.getString(R.string.json_key_trailer_site));
                String type = trailers.optJSONObject(i).optString(context.getString(R.string.json_key_trailer_type));

                trailerList.add(new Trailer(movieID, trailerID, trailerTitle, key, site, type));
            }

            JSONObject movieReviewsResponse = new JSONObject(reviewsResponse);
            JSONArray reviews = movieReviewsResponse.optJSONArray(context.getString(R.string.json_key_array_results));
            List<Review> reviewList = new ArrayList<>();
            for (int i = 0; i < reviews.length(); i++) {
                String reviewID = reviews.optJSONObject(i).optString(context.getString(R.string.json_key_id));
                String author = reviews.optJSONObject(i).optString(context.getString(R.string.json_key_review_author));
                String content = reviews.optJSONObject(i).optString(context.getString(R.string.json_key_review_content));

                reviewList.add(new Review(movieID, reviewID, author, content));
            }

            movie = new Movie(movieID, title, moviePoster, runtime, userRating, releaseDate, genreList, tagline, plotSynopsis, trailerList, reviewList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movie;
    }

    // Check if the response returned has any movies in it
    static  boolean moviesAvailable(Context context, String response) {
        try {
            JSONObject movieListResponse = new JSONObject(response);
            JSONArray results = movieListResponse.getJSONArray(context.getString(R.string.json_key_array_results));
            if (results.length() > 0) return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
