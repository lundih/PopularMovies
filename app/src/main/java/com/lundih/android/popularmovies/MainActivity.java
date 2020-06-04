package com.lundih.android.popularmovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String sortBy;
    private String imageQuality;
    private SharedPreferences sharedPreferences;
    private MenuItem menuItemSortByPopularity;
    private MenuItem menuItemSortByUserRating;
    private MenuItem menuItemImageStandardQuality;
    private MenuItem menuItemImageHighQuality;
    private GridView gridViewMovies;
    private ProgressBar progressBarFetchingMovies;
    private ConstraintLayout constraintLayoutDataNotFound;
    private TextView textViewDataNotFound;
    private ImageView imageViewDataNotFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sortBy = sharedPreferences.getString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity));
        imageQuality = sharedPreferences.getString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality));

        // Create an action bar
        Toolbar toolbar = findViewById(R.id.toolbarMainActivity);
        setSupportActionBar(toolbar);

        gridViewMovies = findViewById(R.id.gridViewMovies);
        progressBarFetchingMovies = findViewById(R.id.progressBarFetchingMainActivityMovies);
        constraintLayoutDataNotFound =findViewById(R.id.constraintLayoutDataNotFound);
        textViewDataNotFound = findViewById(R.id.textViewDataNotFound);
        imageViewDataNotFound = findViewById(R.id.imageViewDataNotFound);
        Button buttonRetry = findViewById(R.id.buttonRetryMovieFetching);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this, sortBy, gridViewMovies);
                backgroundTask.execute();
            }
        });

        // Allow the GridView to interact with the toolbar when scrolling
        gridViewMovies.setNestedScrollingEnabled(true);

        BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this, sortBy, gridViewMovies);
        backgroundTask.execute();
    }

    @SuppressLint("StaticFieldLeak")
    class BackgroundTask extends AsyncTask<Void, Void, String > {
        private final Context context;
        private final String sortBy;
        private final GridView gridView;

        BackgroundTask(Context context, String sortBy, GridView gridViewMovies) {
            this.context = context;
            this.sortBy = sortBy;
            this.gridView = gridViewMovies;
        }

        @Override
        protected void onPreExecute() {
            // Check if there's internet connection
            if (NetworkUtils.checkInternetConnectionState(context) && NetworkUtils.canPing())
                // Show the appropriate views when loading the movies
                showLoading();
            else {
                // Cancel this instance of AsyncTask since there's no internet anyways
                showNotFound(getString(R.string.message_no_internet), R.drawable.ic_no_internet);
                this.cancel(true);
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = null;
            try {
                response = NetworkUtils.fetchMovieData(context, sortBy);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null && !response.equals("")) {
                // Show appropriate views after movies are loaded
                showFetchedMovies();
                List<Movie> movies = JSONUtils.extractMovies(context, response);
                CustomAdapter customAdapter = new CustomAdapter(context, movies);
                gridView.setAdapter(customAdapter);
            } else {
                showNotFound(getString(R.string.message_error_encountered), R.drawable.ic_error);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main_activity, menu);

        menuItemSortByPopularity = menu.findItem(R.id.menu_sort_by_popularity);
        menuItemSortByUserRating = menu.findItem(R.id.menu_sort_by_user_rating);
        menuItemImageStandardQuality = menu.findItem(R.id.menu_standard_quality_image);
        menuItemImageHighQuality = menu.findItem(R.id.menu_high_quality_image);
        // Check from SharedPreferences to know the last sort and image quality criteria and set to that during options menu creation
        if (sortBy.equals(getString(R.string.sort_value_popularity)))
            menuItemSortByPopularity.setChecked(true);
        else if (sortBy.equals(getString(R.string.sort_value_user_rating)))
            menuItemSortByUserRating.setChecked(true);
        if (imageQuality.equals(getString(R.string.url_image_quality)))
            menuItemImageStandardQuality.setChecked(true);
        else if (imageQuality.equals(getString(R.string.url_image_quality_high)))
            menuItemImageHighQuality.setChecked(true);

        return true;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_sort_by_popularity) {
            // Write sort criteria to SharedPreferences
            sharedPreferences.edit().putString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity)).apply();
            sortBy = sharedPreferences.getString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity));
            menuItemSortByPopularity.setChecked(true);
            BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this, getString(R.string.sort_value_popularity), gridViewMovies);
            backgroundTask.execute();

            return true;
        } else if (id == R.id.menu_sort_by_user_rating) {
            sharedPreferences.edit().putString(getString(R.string.url_key_sort), getString(R.string.sort_value_user_rating)).apply();
            sortBy = sharedPreferences.getString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity));
            menuItemSortByUserRating.setChecked(true);
            BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this, getString(R.string.sort_value_user_rating), gridViewMovies);
            backgroundTask.execute();

            return  true;
        } else if (id == R.id.menu_standard_quality_image) {
            sharedPreferences.edit().putString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality)).commit();
            imageQuality = sharedPreferences.getString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality));
            menuItemImageStandardQuality.setChecked(true);
            BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this, sortBy, gridViewMovies);
            backgroundTask.execute();

            return true;
        } else if (id == R.id.menu_high_quality_image) {
            sharedPreferences.edit().putString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality_high)).commit();
            imageQuality = sharedPreferences.getString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality));
            menuItemImageHighQuality.setChecked(true);
            BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this, sortBy, gridViewMovies);
            backgroundTask.execute();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Display the relevant view when in the movie-fetching state
    private void showLoading() {
        gridViewMovies.setVisibility(View.GONE);
        constraintLayoutDataNotFound.setVisibility(View.GONE);
        progressBarFetchingMovies.setVisibility(View.VISIBLE);
    }

    // Display relevant views when movies are obtained successfully
    private void showFetchedMovies() {
        progressBarFetchingMovies.setVisibility(View.GONE);
        constraintLayoutDataNotFound.setVisibility(View.GONE);
        gridViewMovies.setVisibility(View.VISIBLE);
    }

    // Display the relevant views when unable to obtain movies
    private void showNotFound(String infoMessage, int imageResource) {
        gridViewMovies.setVisibility(View.GONE);
        progressBarFetchingMovies.setVisibility(View.GONE);
        constraintLayoutDataNotFound.setVisibility(View.VISIBLE);
        textViewDataNotFound.setText(infoMessage);
        imageViewDataNotFound.setImageResource(imageResource);
    }
}
