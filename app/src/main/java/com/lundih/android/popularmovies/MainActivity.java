package com.lundih.android.popularmovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private String sortBy;
    private String imageQuality;
    private int columnWidth;
    static final int STANDARD_COLUMN_COUNT_WIDTH = 200;
    static final int HIGHER_COLUMN_COUNT_WIDTH = 150;
    private final double landscapeColumnWidthMultiplier = 1.2;
    private SharedPreferences sharedPreferences;
    private MenuItem menuItemFavourites;
    private MenuItem menuItemSortByPopularity;
    private MenuItem menuItemSortByUserRating;
    private MenuItem menuItemSortByTrendingDaily;
    private MenuItem menuItemSearch;
    private MenuItem menuItemImageLowQuality;
    private MenuItem menuItemImageMediumQuality;
    private MenuItem menuItemImageHighQuality;
    private MenuItem menuItemColumnCountStandard;
    private MenuItem menuItemColumnCountHigher;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewMovies;
    private ProgressBar progressBarFetchingMovies;
    private ConstraintLayout constraintLayoutDataNotFound;
    private TextView textViewDataNotFound;
    private ImageView imageViewDataNotFound;
    private ConstraintLayout constraintLayoutSearch;
    private EditText editTextSearch;
    private Toolbar toolbar;
    private FavouriteMoviesDatabase favouriteMoviesDatabase;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    CustomViewModel customViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sortBy = sharedPreferences.getString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity));
        imageQuality = sharedPreferences.getString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality_medium));
        columnWidth = sharedPreferences.getInt(getString(R.string.key_column_count_shared_pref), STANDARD_COLUMN_COUNT_WIDTH);

        favouriteMoviesDatabase = FavouriteMoviesDatabase.getInstance(getApplicationContext());
        mContext = getApplicationContext();
        customViewModel = new ViewModelProvider(this).get(CustomViewModel.class);

        constraintLayoutSearch = findViewById(R.id.constraintLayoutSearch);
        constraintLayoutSearch.setVisibility(View.GONE);

        // Create an action bar
        toolbar = findViewById(R.id.toolbarMainActivity);
        setSupportActionBar(toolbar);
        String title = getString(R.string.menu_sort_popularity);
        if (sortBy.equals(getString(R.string.sort_value_popularity))) title = getString(R.string.menu_sort_popularity);
        else if (sortBy.equals(getString(R.string.sort_value_user_rating))) title = getString(R.string.menu_sort_user_rating);
        else if (sortBy.equals(getString(R.string.sort_value_favourites))) title = getString(R.string.menu_favourites);
        else if (sortBy.equals(getString(R.string.sort_value_trending_daily))) title = getString(R.string.menu_sort_trending_daily);
        else if (sortBy.equals(getString(R.string.sort_value_search))) title =  getString(R.string.menu_search);
        toolbar.setTitle(title);

        recyclerViewMovies = findViewById(R.id.recyclerViewMovies);
        progressBarFetchingMovies = findViewById(R.id.progressBarFetchingMainActivityMovies);
        constraintLayoutDataNotFound =findViewById(R.id.constraintLayoutDataNotFound);
        textViewDataNotFound = findViewById(R.id.textViewDataNotFound);
        imageViewDataNotFound = findViewById(R.id.imageViewDataNotFound);
        editTextSearch = findViewById(R.id.editTextSearch);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.colorAccent));
        Button buttonRetry = findViewById(R.id.buttonRetryMovieFetching);
        ImageButton imageButtonSearch = findViewById(R.id.imageButtonSearch);
        final ImageButton imageButtonClearQuery = findViewById(R.id.imageButtonClearQuery);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sortBy.equals(getString(R.string.sort_value_search))) {
                    refreshMovieList();
                }else {
                    if (!editTextSearch.getText().toString().equals("")) {
                        searchMovie(editTextSearch.getText().toString());
                    }
                }
            }
        });
        imageButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextSearch.getText().toString().equals("")) {
                    searchMovie(editTextSearch.getText().toString());
                }
            }
        });
        // Set an action for the search key in the soft keyboard
        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!editTextSearch.getText().toString().equals("")) {
                        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
                        searchMovie(editTextSearch.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });
        // Hide or show the clear query button depending on whether there is text in the editText
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!editTextSearch.getText().toString().equals("")) imageButtonClearQuery.setVisibility(View.VISIBLE);
                else imageButtonClearQuery.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        imageButtonClearQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearch.setText("");
            }
        });

        setUpLiveDataFavouriteMovies();
        if (!sortBy.equals(getString(R.string.sort_value_search))) {
            if (!sortBy.equals(getString(R.string.sort_value_favourites))) refreshMovieList();
        } else {
            showFetchedMovies();
            recyclerViewMovies.setVisibility(View.GONE);
            constraintLayoutSearch.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        if (!sortBy.equals(getString(R.string.sort_value_search))) {
            refreshMovieList();
        }else {
            if (!editTextSearch.getText().toString().equals("")) {
                searchMovie(editTextSearch.getText().toString());
            } else {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main_activity, menu);
        // Set the divider for different groups in the menu
        MenuCompat.setGroupDividerEnabled(menu, true);

        menuItemFavourites = menu.findItem(R.id.menu_favourites);
        menuItemSortByPopularity = menu.findItem(R.id.menu_sort_by_popularity);
        menuItemSortByUserRating = menu.findItem(R.id.menu_sort_by_user_rating);
        menuItemSortByTrendingDaily = menu.findItem(R.id.menu_sort_by_trending_daily);
        menuItemSearch = menu.findItem(R.id.menu_search);
        menuItemImageLowQuality = menu.findItem(R.id.menu_low_quality_image);
        menuItemImageMediumQuality = menu.findItem(R.id.menu_medium_quality_image);
        menuItemImageHighQuality = menu.findItem(R.id.menu_high_quality_image);
        menuItemColumnCountStandard = menu.findItem(R.id.menu_standard_column_count);
        menuItemColumnCountHigher = menu.findItem(R.id.menu_higher_column_count);
        // Check from SharedPreferences to know the last criteria and set to that
        if (sortBy.equals(getString(R.string.sort_value_popularity)))
            menuItemSortByPopularity.setChecked(true);
        else if (sortBy.equals(getString(R.string.sort_value_user_rating)))
            menuItemSortByUserRating.setChecked(true);
        else if (sortBy.equals(getString(R.string.sort_value_trending_daily)))
            menuItemSortByTrendingDaily.setChecked(true);
        else if (sortBy.equals(getString(R.string.sort_value_favourites)))
            menuItemFavourites.setChecked(true);
        else if (sortBy.equals(getString(R.string.sort_value_search)))
            menuItemSearch.setChecked(true);

        if (imageQuality.equals(getString(R.string.url_image_quality_low)))
            menuItemImageLowQuality.setChecked(true);
        else if (imageQuality.equals(getString(R.string.url_image_quality_medium)))
            menuItemImageMediumQuality.setChecked(true);
        else if (imageQuality.equals(getString(R.string.url_image_quality_high)))
            menuItemImageHighQuality.setChecked(true);

        if (columnWidth == STANDARD_COLUMN_COUNT_WIDTH)
            menuItemColumnCountStandard.setChecked(true);
        else if (columnWidth == HIGHER_COLUMN_COUNT_WIDTH)
            menuItemColumnCountHigher.setChecked(true);

        return true;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_sort_by_popularity) {
            sharedPreferences.edit().putString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity)).commit();
            sortBy = sharedPreferences.getString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity));
            menuItemSortByPopularity.setChecked(true);
            toolbar.setTitle(getString(R.string.menu_sort_popularity));
            constraintLayoutSearch.setVisibility(View.GONE);
            refreshMovieList();

            return true;
        } else if (id == R.id.menu_sort_by_user_rating) {
            sharedPreferences.edit().putString(getString(R.string.url_key_sort), getString(R.string.sort_value_user_rating)).commit();
            sortBy = sharedPreferences.getString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity));
            menuItemSortByUserRating.setChecked(true);
            toolbar.setTitle(getString(R.string.menu_sort_user_rating));
            constraintLayoutSearch.setVisibility(View.GONE);
            refreshMovieList();

            return true;
        } else if (id == R.id.menu_sort_by_trending_daily) {
            sharedPreferences.edit().putString(getString(R.string.url_key_sort), getString(R.string.sort_value_trending_daily)).commit();
            sortBy = sharedPreferences.getString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity));
            menuItemSortByTrendingDaily.setChecked(true);
            toolbar.setTitle(getString(R.string.menu_sort_trending_daily));
            constraintLayoutSearch.setVisibility(View.GONE);
            refreshMovieList();

            return true;
        } else if (id == R.id.menu_favourites) {
            sharedPreferences.edit().putString(getString(R.string.url_key_sort), getString(R.string.sort_value_favourites)).commit();
            sortBy = sharedPreferences.getString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity));
            menuItemFavourites.setChecked(true);
            toolbar.setTitle(getString(R.string.menu_favourites));
            constraintLayoutSearch.setVisibility(View.GONE);
            refreshMovieList();

            return  true;
        }else if (id == R.id.menu_search) {
            sharedPreferences.edit().putString(getString(R.string.url_key_sort), getString(R.string.sort_value_search)).commit();
            sortBy = sharedPreferences.getString(getString(R.string.url_key_sort), getString(R.string.sort_value_popularity));
            menuItemSearch.setChecked(true);
            toolbar.setTitle(getString(R.string.menu_search));
            recyclerViewMovies.setVisibility(View.GONE);
            constraintLayoutSearch.setVisibility(View.VISIBLE);

            return true;
        }else if (id == R.id.menu_low_quality_image) {
            sharedPreferences.edit().putString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality_low)).commit();
            menuItemImageLowQuality.setChecked(true);
            if (!sortBy.equals(getString(R.string.sort_value_search))) refreshMovieList();

            return true;
        } else if (id == R.id.menu_medium_quality_image) {
            sharedPreferences.edit().putString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality_medium)).commit();
            menuItemImageMediumQuality.setChecked(true);
            if (!sortBy.equals(getString(R.string.sort_value_search))) refreshMovieList();

            return true;
        } else if (id == R.id.menu_high_quality_image) {
            sharedPreferences.edit().putString(getString(R.string.key_image_resolution_shared_pref), getString(R.string.url_image_quality_high)).commit();
            menuItemImageHighQuality.setChecked(true);
            if (!sortBy.equals(getString(R.string.sort_value_search))) refreshMovieList();

            return true;
        } else if (id == R.id.menu_standard_column_count) {
            sharedPreferences.edit().putInt(getString(R.string.key_column_count_shared_pref), STANDARD_COLUMN_COUNT_WIDTH).commit();
            columnWidth = sharedPreferences.getInt(getString(R.string.key_column_count_shared_pref), STANDARD_COLUMN_COUNT_WIDTH);
            menuItemColumnCountStandard.setChecked(true);
            if (!sortBy.equals(getString(R.string.sort_value_search))) refreshMovieList();

            return true;
        } else if (id == R.id.menu_higher_column_count) {
            sharedPreferences.edit().putInt(getString(R.string.key_column_count_shared_pref), HIGHER_COLUMN_COUNT_WIDTH).commit();
            columnWidth = sharedPreferences.getInt(getString(R.string.key_column_count_shared_pref), STANDARD_COLUMN_COUNT_WIDTH);
            menuItemColumnCountHigher.setChecked(true);
            if (!sortBy.equals(getString(R.string.sort_value_search))) refreshMovieList();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    class BackgroundTask extends AsyncTask<Void, Void, String > {
        private final Context context;
        private final String sortBy;
        private final RecyclerView recyclerView;
        private List<Movie> favouriteMovies = null;
        private String query = "";

        BackgroundTask(Context context, String sortBy, RecyclerView recyclerViewMovies) {
            this.context = context;
            this.sortBy = sortBy;
            this.recyclerView = recyclerViewMovies;
        }

        BackgroundTask(Context context, String sortBy, RecyclerView recyclerViewMovies, String query) {
            this.context = context;
            this.sortBy = sortBy;
            this.recyclerView = recyclerViewMovies;
            this.query = query;
        }

        @Override
        protected void onPreExecute() {
            // Show the appropriate views when loading the movies
            showLoading();
            if (!sortBy.equals(getString(R.string.sort_value_favourites))) {
                // Check if there's internet connection
                if (!(NetworkUtils.checkInternetConnectionState(context) && NetworkUtils.canPing())) {
                    // If there is no internet connection and the ViewModel does not have movies,
                    //let users know they need an internet connection
                    if ((sortBy.equals(getString(R.string.sort_value_popularity)) && customViewModel.getPopularMovies() == null) ||
                            (sortBy.equals(getString(R.string.sort_value_user_rating)) && customViewModel.getHighestRatedMovies() == null) ||
                            (sortBy.equals(getString(R.string.sort_value_trending_daily)) && customViewModel.getTrendingDailyMovies() == null) ||
                            (sortBy.equals(getString(R.string.sort_value_search)) && customViewModel.getSearchedMovies() == null)) {
                        showNotFound(getString(R.string.message_no_internet), R.drawable.ic_no_internet);
                        // Cancel this task... there's no internet anyways
                        this.cancel(true);
                    }
                }
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = null;
            if (sortBy.equals(getString(R.string.sort_value_favourites))){
                // Get favourite movies from the database
                favouriteMovies = favouriteMoviesDatabase.movieDAO().retrieveFavouriteMovies();

                return null;
            } else {
                try {
                    if (sortBy.equals(context.getString(R.string.sort_value_search))) {
                        response = NetworkUtils.fetchMovieData(context, sortBy, query);
                    } else
                        response = NetworkUtils.fetchMovieData(context, sortBy);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null && !response.equals("") && !sortBy.equals(getString(R.string.sort_value_favourites))) {
                // Movies received from API
                if (JSONUtils.moviesAvailable(context, response)) {
                    List<Movie> movies = JSONUtils.extractMovies(context, response);
                    setUpRecyclerView(movies);
                    // Show appropriate views after movies are loaded
                    showFetchedMovies();
                    // Cache movies received from API in View Model
                    if (sortBy.equals(getString(R.string.sort_value_popularity)))
                        customViewModel.setPopularMovies(movies);
                    else if (sortBy.equals(getString(R.string.sort_value_user_rating)))
                        customViewModel.setHighestRatedMovies(movies);
                    else if (sortBy.equals(context.getString(R.string.sort_value_trending_daily)))
                        customViewModel.setTrendingDailyMovies(movies);
                    else if (sortBy.equals(context.getString(R.string.sort_value_search)))
                        customViewModel.setSearchedMovies(movies);
                } else {
                    if (!sortBy.equals(getString(R.string.sort_value_search)))
                        showNotFound(getString(R.string.message_no_movies_found), R.drawable.ic_close);
                    else
                        showNotFound(getString(R.string.message_no_search_results), R.drawable.ic_close);
                }
            } else if (response == null && sortBy.equals(getString(R.string.sort_value_popularity)) && customViewModel.getPopularMovies() != null){
                // Get popular movies from ViewModel
                setUpRecyclerView(customViewModel.getPopularMovies());
                showFetchedMovies();
            } else if (response == null && sortBy.equals(getString(R.string.sort_value_user_rating)) && customViewModel.getHighestRatedMovies() != null) {
                // Get highest rated movies from ViewModel
                setUpRecyclerView(customViewModel.getHighestRatedMovies());
                showFetchedMovies();
            } else if (response == null && sortBy.equals(getString(R.string.sort_value_trending_daily)) && customViewModel.getTrendingDailyMovies() != null) {
                // Get trending_daily movies from ViewModel
                setUpRecyclerView(customViewModel.getTrendingDailyMovies());
                showFetchedMovies();
            } else if (response == null && sortBy.equals(getString(R.string.sort_value_search)) && customViewModel.getSearchedMovies() != null) {
                // Get searched movies from ViewModel
                setUpRecyclerView(customViewModel.getSearchedMovies());
                showFetchedMovies();
            }else {
                // Get movies from database when refreshing manually
                if (favouriteMovies != null && !favouriteMovies.isEmpty()) {
                    setUpRecyclerView(favouriteMovies);
                    showFetchedMovies();
                } else {
                    if (!sortBy.equals(getString(R.string.sort_value_favourites))) {
                        showNotFound(getString(R.string.message_error_encountered), R.drawable.ic_error);
                    } else {
                        showNotFound(getString(R.string.message_no_favourites), R.drawable.ic_unfavourite);
                    }
                }
            }
        }

        private void setUpRecyclerView(List<Movie> movies) {
            int columnWidthDp = columnWidth;
            MoviesAdapter moviesAdapter = new MoviesAdapter(context, movies);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                columnWidthDp *= landscapeColumnWidthMultiplier;
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context, ColumnNumberCalcUtils.calculate(context, columnWidthDp));
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(moviesAdapter);
        }
    }

    public static Context getTheContext(){
        return mContext;
    }

    // Display the relevant view when in the movie-fetching state
    private void showLoading() {
        recyclerViewMovies.setVisibility(View.GONE);
        constraintLayoutDataNotFound.setVisibility(View.GONE);
        progressBarFetchingMovies.setVisibility(View.VISIBLE);
    }

    // Display relevant views when movies are obtained successfully
    private void showFetchedMovies() {
        progressBarFetchingMovies.setVisibility(View.GONE);
        constraintLayoutDataNotFound.setVisibility(View.GONE);
        recyclerViewMovies.setVisibility(View.VISIBLE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    // Display the relevant views when unable to obtain movies
    private void showNotFound(String infoMessage, int imageResource) {
        recyclerViewMovies.setVisibility(View.GONE);
        progressBarFetchingMovies.setVisibility(View.GONE);
        constraintLayoutDataNotFound.setVisibility(View.VISIBLE);
        textViewDataNotFound.setText(infoMessage);
        imageViewDataNotFound.setImageResource(imageResource);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void refreshMovieList() {
        new BackgroundTask(MainActivity.this, sortBy, recyclerViewMovies).execute();
    }

    private void searchMovie(String query) {
        new BackgroundTask(MainActivity.this, sortBy, recyclerViewMovies, query).execute();
    }

    // Get favourite movies from LiveData within the ViewModel
    private void setUpLiveDataFavouriteMovies() {
        customViewModel.getFavouriteMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if (sortBy.equals(getString(R.string.sort_value_favourites))) {
                    if (!movies.isEmpty()) {
                        int columnWidthDp = columnWidth;
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                            columnWidthDp *= landscapeColumnWidthMultiplier;
                        recyclerViewMovies.setLayoutManager(new GridLayoutManager(MainActivity.this, ColumnNumberCalcUtils.calculate(MainActivity.this, columnWidthDp)));
                        recyclerViewMovies.setAdapter(new MoviesAdapter(MainActivity.this, movies));
                        showFetchedMovies();
                    } else {
                        showNotFound(getString(R.string.message_no_favourites), R.drawable.ic_unfavourite);
                    }
                }
            }
        });
    }
}