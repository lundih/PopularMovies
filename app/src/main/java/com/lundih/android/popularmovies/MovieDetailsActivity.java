package com.lundih.android.popularmovies;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;

import static androidx.coordinatorlayout.widget.CoordinatorLayout.*;

public class MovieDetailsActivity extends AppCompatActivity {
    private int movieID;
    private String imageQuality;
    private CoordinatorLayout coordinatorLayout;
    private ProgressBar progressBarFetchingMovie;
    private ConstraintLayout constraintLayoutMovieNotFound;
    private TextView textViewMovieNotFound;
    private ImageView imageViewMovieNotFound;
    private ImageView imageViewMoviePoster;
    private TextView textViewRuntime;
    private RatingBar ratingBarUserRating;
    private TextView textViewUserRating;
    private TextView textViewReleaseDate;
    private TextView textViewGenre;
    private TextView textViewTagLine;
    private TextView textViewPlotSynopsis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Set the toolbar as the action bar to allow for adding the 'up' button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        imageQuality = sharedPreferences.getString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality));

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        progressBarFetchingMovie = findViewById(R.id.progressBarFetchingMovieDetailsMovie);
        constraintLayoutMovieNotFound = findViewById(R.id.constraintLayoutDataNotFoundMovieDetails);
        textViewMovieNotFound = findViewById(R.id.textViewDataNotFoundMovieDetails);
        imageViewMovieNotFound = findViewById(R.id.imageViewDataNotFoundMovieDetails);


        imageViewMoviePoster = findViewById(R.id.imageViewMovieDetailsPoster);
        textViewRuntime = findViewById(R.id.textViewRuntime);
        ratingBarUserRating = findViewById(R.id.ratingBarUserRating);
        textViewUserRating = findViewById(R.id.textViewUserRating);
        textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        textViewGenre = findViewById(R.id.textViewGenre);
        textViewTagLine = findViewById(R.id.textViewTagLine);
        textViewPlotSynopsis = findViewById(R.id.textViewPlotSynopsis);
        Button buttonRetry = findViewById(R.id.buttonRetryMovieFetchingMovieDetails);
        buttonRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BackgroundTask backgroundTask = new BackgroundTask(MovieDetailsActivity.this, movieID, imageViewMoviePoster, textViewRuntime, ratingBarUserRating, textViewReleaseDate, textViewUserRating,
                        textViewGenre, textViewTagLine, textViewPlotSynopsis);
                backgroundTask.execute();
            }
        });

        if (getIntent() != null) {
            movieID = Objects.requireNonNull(getIntent().getExtras()).getInt(getString(R.string.intent_key_movie_id));
            collapsingToolbarLayout.setTitle(getIntent().getExtras().getString(getString(R.string.intent_key_movie_title)));

            BackgroundTask backgroundTask = new BackgroundTask(MovieDetailsActivity.this, movieID, imageViewMoviePoster, textViewRuntime, ratingBarUserRating, textViewReleaseDate, textViewUserRating,
              textViewGenre, textViewTagLine, textViewPlotSynopsis);
            backgroundTask.execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class BackgroundTask extends AsyncTask <Void, Void, String> {
        private final Context context;
        private final int movieID;
        final ImageView imageViewMoviePoster;
        final TextView textViewRuntime;
        final RatingBar ratingBarUserRating;
        final TextView textViewUserRating;
        final TextView textViewReleaseDate;
        final TextView textViewGenre;
        final TextView textViewTagLine;
        final TextView textViewPlotSynopsis;

        BackgroundTask(Context context, int movieID, ImageView poster, TextView runtime, RatingBar userRating, TextView releaseDate, TextView rating, TextView genre, TextView tagLine, TextView plotSynopsis) {
            this.context = context;
            this.movieID = movieID;
            this.imageViewMoviePoster = poster;
            this.textViewRuntime = runtime;
            this.ratingBarUserRating = userRating;
            this.textViewUserRating = rating;
            this.textViewReleaseDate = releaseDate;
            this.textViewGenre = genre;
            this.textViewTagLine = tagLine;
            this.textViewPlotSynopsis = plotSynopsis;
        }

        @Override
        protected void onPreExecute() {
            if (NetworkUtils.checkInternetConnectionState(context) && NetworkUtils.canPing())
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
                response = NetworkUtils.fetchMovieData(context, movieID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null && !response.equals("")) {
                Movie movie = JSONUtils.extractMovieDetails(context, response);
                showMovieDetails();
                if (movie.getMoviePoster() != null && !movie.getMoviePoster().equals(""))
                    Picasso.get().load(getString(R.string.url_base_image) + imageQuality + movie.getMoviePoster()).into(imageViewMoviePoster);
                if (movie.getRuntime() > 0) {
                    String runtime = movie.getRuntime() + getString(R.string.text_runtime_unit);
                    textViewRuntime.setText(runtime);
                }
                if (movie.getUserRating()>0) {
                    ratingBarUserRating.setRating((float) movie.getUserRating());
                    String rating = Double.toString(movie.getUserRating());
                    textViewUserRating.setText(rating);
                }
                if (movie.getReleaseDate()!=null && !movie.getReleaseDate().equals(""))
                    textViewReleaseDate.setText(movie.getReleaseDate());
                if (movie.getGenre().size() > 0) {
                    String genres = "";
                    for (int i = 0; i < movie.getGenre().size(); i++) {
                        if (i != movie.getGenre().size() - 1)
                            genres += movie.getGenre().get(i) + getString(R.string.comma);
                        else
                            genres += movie.getGenre().get(i);
                    }
                    textViewGenre.setText(genres);
                }
                if (movie.getTagLine() != null && !movie.getTagLine().equals(""))
                    textViewTagLine.setText(movie.getTagLine());
                if (movie.getPlotSynopsis() != null && !movie.getPlotSynopsis().equals(""))
                textViewPlotSynopsis.setText(movie.getPlotSynopsis());
            } else {
                showNotFound(getString(R.string.message_error_encountered), R.drawable.ic_error);
            }
        }
    }

    private void showLoading() {
        coordinatorLayout.setVisibility(View.GONE);
        constraintLayoutMovieNotFound.setVisibility(View.GONE);
        progressBarFetchingMovie.setVisibility(View.VISIBLE);
    }

    private void showMovieDetails() {
        progressBarFetchingMovie.setVisibility(View.GONE);
        constraintLayoutMovieNotFound.setVisibility(View.GONE);
        coordinatorLayout.setVisibility(View.VISIBLE);
    }

    private void showNotFound(String infoMessage, int imageResource) {
        coordinatorLayout.setVisibility(View.GONE);
        progressBarFetchingMovie.setVisibility(View.GONE);
        constraintLayoutMovieNotFound.setVisibility(View.VISIBLE);
        textViewMovieNotFound.setText(infoMessage);
        imageViewMovieNotFound.setImageResource(imageResource);
    }

    // Allow user to close MoviesDetailsActivity and move to the previous activity using 'up' button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}