package com.lundih.android.popularmovies;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;
import static androidx.coordinatorlayout.widget.CoordinatorLayout.*;

public class MovieDetailsActivity extends AppCompatActivity {
    private int movieID;
    private Movie movie;
    private CoordinatorLayout coordinatorLayout;
    private AppBarLayout appBarLayout;
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
    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;
    private FloatingActionButton fabFavourite;
    private FavouriteMoviesDatabase favouriteMoviesDatabase;
    CustomViewModel viewModel;
    private boolean isFavourite = false;
    static final int ADD_MOVIE = 1;
    static final int REMOVE_MOVIE = 2;
    static final int UPDATE_MOVIE = 3;
    static final int CHECK_IF_MOVIE_EXISTS_IN_DB = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        appBarLayout = findViewById(R.id.appBarLayout);
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
        fabFavourite = findViewById(R.id.fabFavourite);
        recyclerViewTrailers = findViewById(R.id.recyclerViewTrailers);
        recyclerViewReviews = findViewById(R.id.recyclerviewReviews);
        Button buttonRetry = findViewById(R.id.buttonRetryMovieFetchingMovieDetails);
        TextView textViewIconsAttribution = findViewById(R.id.textViewIconsAttribution);
        // Set hyperlink in the textView to be clickable
        textViewIconsAttribution.setMovementMethod(LinkMovementMethod.getInstance());

        favouriteMoviesDatabase = FavouriteMoviesDatabase.getInstance(this);

        showLoading();

        viewModel = new ViewModelProvider(this).get(CustomViewModel.class);

        if (getIntent() != null) {
            movieID = Objects.requireNonNull(getIntent().getExtras()).getInt(getString(R.string.intent_key_movie_id));
            try { // Check if the movie already exists in the database as a favourite
                isFavourite = new FavouriteTask(CHECK_IF_MOVIE_EXISTS_IN_DB).execute().get();
            } catch (ExecutionException | InterruptedException e) {
                isFavourite = false;
                e.printStackTrace();
            }
            collapsingToolbarLayout.setTitle(getIntent().getExtras().getString(getString(R.string.intent_key_movie_title)));
            refreshMovie(isFavourite);
            appBarLayout.post(new Runnable() {
                @Override
                public void run() {
                    int heightPx = imageViewMoviePoster.getHeight();
                    setAppBarOffset(heightPx/3);
                }
            });
        }
        buttonRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavourite) refreshFavouriteMovieFromAPI();
                else refreshMovie(false);
            }
        });

        if (isFavourite)
            fabFavourite.setImageResource(R.drawable.ic_unfavourite);

        fabFavourite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavourite) removeFavouriteMovie();
                else addFavouriteMovie();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_movie_details_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_share ){
            String movieDetails = null;
            if (movie != null){
                movieDetails = "";
                movieDetails = movieDetails.concat(movie.getTitle() + " " + movie.getReleaseDate().substring(0,4) + getString(R.string.new_line));
                if (movie.getTrailer() != null && movie.getTrailer().size() > 0) {
                    for (Trailer trailer: movie.getTrailer()){
                        if (trailer.getSite().toLowerCase().equals(getString(R.string.value_youtube_for_comparison))) {
                            movieDetails = movieDetails.concat(getString(R.string.new_line) + trailer.getTitle() +
                            getString(R.string.new_line) + getString(R.string.url_base_youtube_web) + trailer.getKey() + getString(R.string.new_line));
                        }
                    }
                }
            }
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(getString(R.string.intent_type_share));
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.message_share_subject));
            shareIntent.putExtra(Intent.EXTRA_TEXT, movieDetails);
            if (shareIntent.resolveActivity(getPackageManager()) != null && movieDetails != null)
                startActivity(shareIntent);
            else
                Toast.makeText(this, getString(R.string.message_no_app_to_share), Toast.LENGTH_SHORT).show();

            return true;
        } else if (item.getItemId() == R.id.menu_refresh_movie_details) {
            if (isFavourite) refreshFavouriteMovieFromAPI();
            else refreshMovie(false);
        }

        return super.onOptionsItemSelected(item);
    }

    // Allow user to close MoviesDetailsActivity and move to the previous activity using 'up' button
    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return true;
    }

    @SuppressLint("StaticFieldLeak")
    class BackgroundTask extends AsyncTask <Void, Void, String> {
        private final Context context;
        private final int movieID;
        private String trailersResponse = null;
        private String reviewsResponse = null;
        final ImageView imageViewMoviePoster;
        final TextView textViewRuntime;
        final RatingBar ratingBarUserRating;
        final TextView textViewUserRating;
        final TextView textViewReleaseDate;
        final TextView textViewGenre;
        final TextView textViewTagLine;
        final TextView textViewPlotSynopsis;
        final RecyclerView recyclerViewTrailers;
        final RecyclerView recyclerViewReviews;
        private final boolean isFavouriteMovie;
        private Movie movieDetails; // For details of a favourite movie
        private List<Trailer> trailers; // For trailers of a favourite movie
        private List<Review> reviews; // For reviews of a favourite movie

        BackgroundTask(Context context, int movieID, ImageView poster, TextView runtime, RatingBar userRating,
                       TextView releaseDate, TextView rating, TextView genre, TextView tagLine, TextView plotSynopsis,
                       RecyclerView trailers, RecyclerView reviews, boolean isFavouriteMovie) {
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
            this.recyclerViewTrailers = trailers;
            this.recyclerViewReviews = reviews;
            this.isFavouriteMovie = isFavouriteMovie;
        }

        @Override
        protected void onPreExecute() {
            fabFavourite.setVisibility(GONE);
            if (!isFavouriteMovie) {
                if (viewModel.getMovie() == null)
                    if (NetworkUtils.checkInternetConnectionState(context) && NetworkUtils.canPing())
                        showLoading();
                    else {
                        // Cancel this instance of AsyncTask since there's no internet anyways
                        showNotFound(getString(R.string.message_no_internet), R.drawable.ic_no_internet);
                        this.cancel(true);
                    }
            } else {
                showLoading();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            String detailsResponse = null;
            if (!isFavouriteMovie) { // If this is not a favourite movie, make a call to API
                try {
                    // Fetch movie details
                    detailsResponse = NetworkUtils.fetchMovieData(context, movieID, NetworkUtils.REQUEST_FOR_DETAILS);
                } catch (IOException e) {
                    e.printStackTrace();
                    detailsResponse = null;
                }
                try {
                    // Fetch trailers
                    trailersResponse = NetworkUtils.fetchMovieData(context, movieID, NetworkUtils.REQUEST_FOR_TRAILERS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    // Fetch reviews
                    reviewsResponse = NetworkUtils.fetchMovieData(context, movieID, NetworkUtils.REQUEST_FOR_REVIEWS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else { // If this is a favourite movie, get data from the database
                movieDetails = favouriteMoviesDatabase.movieDAO().retrieveFavouriteMovie(movieID);
                trailers = favouriteMoviesDatabase.movieDAO().retrieveFavouriteTrailers(movieID);
                reviews = favouriteMoviesDatabase.movieDAO().retrieveFavouriteReviews(movieID);
            }

            return detailsResponse;
        }

        @Override
        protected void onPostExecute(String detailsResponse) {
            if (detailsResponse != null && !detailsResponse.equals("")) { // Movie info received from the API
                movie = JSONUtils.extractMovieDetails(context, detailsResponse, trailersResponse, reviewsResponse);
                setUpViews();
                showMovieDetails();
                viewModel.setMovie(movie);
                if (isFavourite) new FavouriteTask(UPDATE_MOVIE).execute();
            } else if (detailsResponse == null && movieDetails != null && viewModel.getMovie() == null){ // Movie info received from the database
                movie = new Movie(movieDetails.getMovieID(), movieDetails.getTitle(), movieDetails.getMoviePoster(),
                        movieDetails.getRuntime(), movieDetails.getUserRating(), movieDetails.getReleaseDate(),
                        movieDetails.getGenre(), movieDetails.getTagLine(), movieDetails.getPlotSynopsis(), trailers, reviews);
                setUpViews();
                showMovieDetails();
                viewModel.setMovie(movie);
            } else if (detailsResponse == null && viewModel.getMovie() != null) { // Receive movie info from ViewModel
                movie = viewModel.getMovie();
                setUpViews();
                showMovieDetails();
            } else {
                showNotFound(getString(R.string.message_error_encountered), R.drawable.ic_error);
            }
        }

        private void setUpViews(){
            Picasso.get().load(context.getString(R.string.url_base_image) +
                    PreferenceManager.getDefaultSharedPreferences(context).getString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality_medium)) +
                    movie.getMoviePoster())
                    .placeholder(R.drawable.ic_image_place_holder)
                    .into(imageViewMoviePoster);
            if (movie.getRuntime() > 0) {
                int hours = movie.getRuntime()/60;
                int minutes = movie.getRuntime()%60;
                String runtime;
                if (hours > 0)
                    runtime = hours + getString(R.string.text_runtime_hours) + getString(R.string.white_space) + minutes + getString(R.string.text_runtime_minutes);
                else
                    runtime = minutes + getString(R.string.text_runtime_minutes);
                textViewRuntime.setText(runtime);
            }
            if (movie.getUserRating()>0) {
                ratingBarUserRating.setRating((float) movie.getUserRating());
                String rating = Double.toString(movie.getUserRating());
                textViewUserRating.setText(rating);
            }
            if (movie.getReleaseDate()!=null && !movie.getReleaseDate().equals(""))
                textViewReleaseDate.setText(movie.getReleaseDate());
            if (movie.getGenre() != null)
                if (movie.getGenre().size() > 0) {
                    String genres = "";
                    for (int i = 0; i < movie.getGenre().size(); i++) {
                        if (i != movie.getGenre().size() - 1)
                            genres = genres.concat(movie.getGenre().get(i)) + getString(R.string.comma);
                        else
                            genres = genres.concat(movie.getGenre().get(i));
                    }
                    textViewGenre.setText(genres);
                }
            if (movie.getTagLine() != null && !movie.getTagLine().equals(""))
                textViewTagLine.setText(movie.getTagLine());
            if (movie.getPlotSynopsis() != null && !movie.getPlotSynopsis().equals("")) {
                textViewPlotSynopsis.setText(movie.getPlotSynopsis());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    textViewPlotSynopsis.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                }
            }
            if (movie.getTrailer() != null && movie.getTrailer().size() > 0) {
                TrailerAdapter trailerAdapter = new TrailerAdapter(context, movie.getTrailer(), movie.getMoviePoster());
                // Let trailers scroll horizontally
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                recyclerViewTrailers.setLayoutManager(layoutManager);
                recyclerViewTrailers.setAdapter(trailerAdapter);
            }
            if (movie.getReview() != null && movie.getReview().size() > 0) {
                ReviewAdapter reviewAdapter = new ReviewAdapter(context, movie.getReview());
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
                recyclerViewReviews.setLayoutManager(layoutManager);
                recyclerViewReviews.setAdapter(reviewAdapter);
            }
            fabFavourite.setVisibility(VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class FavouriteTask extends AsyncTask<Void, Void, Boolean>{
        final int action;

        FavouriteTask(int action){
            this.action = action;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            switch (action){
                case 1: /* Insert movie details, trailers and reviews into the database */
                    favouriteMoviesDatabase.movieDAO().insertFavouriteMovie(
                            new Movie(movie.getMovieID(), movie.getTitle(), movie.getMoviePoster(),
                                    movie.getRuntime(), movie.getUserRating(), movie.getReleaseDate(),
                                    movie.getGenre(), movie.getTagLine(), movie.getPlotSynopsis()));
                    for (Trailer trailer: movie.getTrailer()) {
                        favouriteMoviesDatabase.movieDAO().insertFavouriteTrailer(trailer);
                    }
                    for (Review review: movie.getReview()) {
                        favouriteMoviesDatabase.movieDAO().insertFavouriteReview(review);
                    }
                    break;
                case 2: /* Delete movie details, trailers and reviews from the database */
                    favouriteMoviesDatabase.movieDAO().deleteFavouriteMovie(
                            new Movie(movie.getMovieID(), movie.getTitle(), movie.getMoviePoster(),
                                    movie.getRuntime(), movie.getUserRating(), movie.getReleaseDate(),
                                    movie.getGenre(), movie.getTagLine(), movie.getPlotSynopsis()));
                    for (Trailer trailer: movie.getTrailer())  {
                        favouriteMoviesDatabase.movieDAO().deleteFavouriteTrailer(trailer);
                    }
                    for (Review review: movie.getReview()) {
                        favouriteMoviesDatabase.movieDAO().deleteFavouriteReview(review);
                    }
                    break;
                case 3: /* Update movie details, trailers and reviews */
                    favouriteMoviesDatabase.movieDAO().updateFavouriteMovie(
                            new Movie(movie.getMovieID(), movie.getTitle(), movie.getMoviePoster(),
                                    movie.getRuntime(), movie.getUserRating(), movie.getReleaseDate(),
                                    movie.getGenre(), movie.getTagLine(), movie.getPlotSynopsis()));
                    for (Trailer trailer: movie.getTrailer()) {
                        favouriteMoviesDatabase.movieDAO().updateFavouriteTrailer(trailer);
                    }
                    for (Review review: movie.getReview()) {
                        favouriteMoviesDatabase.movieDAO().updateFavouriteReview(review);
                    }
                    break;
                case 4: /* Query for a movie ID to see if that movie exists in the database */
                    return favouriteMoviesDatabase.movieDAO().retrieveMovieID(movieID) == movieID;
                default:
                    break;
            }

            return null;
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

    private void addFavouriteMovie() {
        isFavourite = true;
        fabFavourite.setImageResource(R.drawable.ic_unfavourite);
        new FavouriteTask(ADD_MOVIE).execute();
        Snackbar.make(coordinatorLayout, movie.getTitle() + getString(R.string.message_movie_added_to_favourites), Snackbar.LENGTH_LONG).show();
    }

    private void removeFavouriteMovie() {
        isFavourite = false;
        fabFavourite.setImageResource(R.drawable.ic_favourite);
        new FavouriteTask(REMOVE_MOVIE).execute();
        Snackbar.make(coordinatorLayout, movie.getTitle() + getString(R.string.message_movie_removed_from_favourites), Snackbar.LENGTH_LONG).show();
    }

    private void refreshMovie(boolean isFavouriteMovie) {
        new BackgroundTask(MovieDetailsActivity.this, movieID,
                imageViewMoviePoster, textViewRuntime, ratingBarUserRating, textViewReleaseDate, textViewUserRating,
                textViewGenre, textViewTagLine, textViewPlotSynopsis, recyclerViewTrailers, recyclerViewReviews, isFavouriteMovie).execute();
    }

    private void refreshFavouriteMovieFromAPI() {
        // Force isFavourite to false so details of a favourite movie are updated from the API rather than from the database
        // This is useful in getting new reviews or trailers uploaded after the movie was saved locally
        refreshMovie(false);
    }

    private void setAppBarOffset(int offsetPx){
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior != null)
            behavior.onNestedPreScroll(coordinatorLayout, appBarLayout, null, 0, offsetPx, new int[]{0, 0}, 0);
    }
}