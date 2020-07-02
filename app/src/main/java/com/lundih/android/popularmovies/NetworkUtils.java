package com.lundih.android.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

class NetworkUtils {
    static final int REQUEST_FOR_DETAILS = 1;
    static final int REQUEST_FOR_TRAILERS = 2;
    static final int REQUEST_FOR_REVIEWS = 3;

    // Ping a server to ensure the internet connection works
    static boolean canPing() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();

            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Check to see if there is an internet connection
    static boolean checkInternetConnectionState(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isAvailable = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            isAvailable =  (capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)));
        }

        return isAvailable;
    }

    static String fetchMovieData(Context context, String sortBy) throws IOException {
        return getResponseFromHttpUrl(formURLFromUri(buildUri(context, sortBy)));
    }

    static String fetchMovieData(Context context, int movieID, int requestFor) throws IOException {
        return getResponseFromHttpUrl(formURLFromUri(buildUri(context, movieID, requestFor)));
    }

    /**
     * Build URL to query TMDB for the list of movies
     * @param sortBy is used to choose the criteria for sorting the list of movies
     * */
    private static Uri buildUri(Context context, String sortBy) {
        return Uri.parse(context.getString(R.string.url_base_movie) + context.getString(R.string.url_list_of_movies)).buildUpon()
                .appendQueryParameter(context.getString(R.string.url_key_api), context.getString(R.string.api_key_value))
                .appendQueryParameter(context.getString(R.string.url_key_sort), sortBy)
                .build();
    }

    /**
     * Build URL to query TMDB for movie details, trailers or reviews
     * @param movieID is used to get details about the movie selected from the grid
     * */
    private static Uri buildUri(Context context, int movieID, int requestFor) {
        String uriString = context.getString(R.string.url_base_movie) + context.getString(R.string.url_single_movie) + movieID;
        switch (requestFor) {
            case 1:
                break;
            case 2:
                uriString += context.getString(R.string.url_movie_trailers);
                break;
            case 3:
                uriString += context.getString(R.string.url_movie_reviews);
                break;
            default:
                uriString = "";
                break;
        }

        return Uri.parse(uriString).buildUpon()
                .appendQueryParameter(context.getString(R.string.url_key_api), context.getString(R.string.api_key_value))
                .build();
    }

    // Receive a URI as an return URL
    private static URL formURLFromUri(Uri uri) {
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    // Return result from HTTP response
    private static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}