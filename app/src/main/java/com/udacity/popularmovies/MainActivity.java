package com.udacity.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.udacity.popularmovies.data.MovieContract;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.utils.GridSpacingItemDecoration;
import com.udacity.popularmovies.utils.NetworkUtils;
import com.udacity.popularmovies.utils.TheMovieDBJsonUtils;

import java.net.URL;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, MovieAdapter.MovieAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String MOST_POPULAR_KEY = "p";
    public static final String TOP_RATED_KEY = "t";
    public static final String FAVORITES_KEY = "f";

    //ButterKnife Binding
    @BindView(R.id.recyclerview_main)
    RecyclerView mRecyclerView;

    /*
     * The ProgressBar that will indicate to the user that we are loading data. It will be
     * hidden when no data is loading.
     */
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;


    private MovieAdapter mMovieAdapter;

    private static final String MOVIES_TYPE_TEXT_KEY = "movies_type";

    private static String mMoviesType = MOST_POPULAR_KEY;

    private static final int MOVIES_LOADER_ID = 0;

    private static final int FAVORITES_LOADER_ID = 1;

    private static String mLanguage;

    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        /*
         * GridLayoutManager to show the movie posters
         */
        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns());

        /* Set the layoutManager on mRecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /* Add column spacing */
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_layout_margin);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, false, 0));

        /*
         * Use this setting to improve performance because we know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        // set mMovieAdapter equal to a new MovieAdapter
        mMovieAdapter = new MovieAdapter(this, MainActivity.this, mMoviesType);

        /* attaches adapter to the RecyclerView in layout. */
        mRecyclerView.setAdapter(mMovieAdapter);

        // If savedInstanceState is not null and contains MOVIES_TYPE_TEXT_KEY, set mMoviesType with the value
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MOVIES_TYPE_TEXT_KEY)) {
                mMoviesType = savedInstanceState
                        .getString(MOVIES_TYPE_TEXT_KEY);
            }
        }

        // Stetho integration, to view database in chrome inspect.
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

        setupSharedPreferences();

        if (NetworkUtils.isConnected(getBaseContext())) {
            // Initialize the AsyncTaskLoader
            if (mMoviesType.equals(FAVORITES_KEY)) {
                getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, favoritesLoaderListener);
            } else {
                getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
            }
        } else {
            showErrorMessage(getBaseContext().getResources().getString(R.string.network_error_message));
        }

        setActivityTitle();

        Log.d(TAG, "onCreate: registering preference changed listener");

        /*
         * Register MainActivity as an OnPreferenceChangedListener to receive a callback when a
         * SharedPreference has changed.
         */
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);


    }

    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String language = sharedPreferences.getString(getResources().getString(R.string.pref_language_key), getResources().getString(R.string.pref_language_english_key));
        mLanguage = language.equals(getBaseContext().getResources().getString(R.string.pref_language_device_key)) ? Locale.getDefault().getLanguage() : language;
        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }


    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param movie The movie for the poster that was clicked
     */
    @Override
    public void onClick(Movie movie) {
        Context context = this;
        // Launch the DetailActivity using an explicit Intent
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("Movie", movie);
        intentToStartDetailActivity.putExtra("favorites", mMoviesType.equals(FAVORITES_KEY));
        // Use startActivityForResult to get result if back is pressed
        startActivityForResult(intentToStartDetailActivity, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Check if favorites have been removed, in case reload favorites
                boolean refresh_favorites = data.getBooleanExtra("flag_refresh", false);
                if (refresh_favorites) {
                    getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, favoritesLoaderListener);
                }
            }
        }
    }

    /**
     * This method will make the loading indicator visible and
     * hide the movies data.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showLoading() {
        /* Show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
        // Hide mRecyclerView
        mRecyclerView.setVisibility(View.INVISIBLE);
    }


    /**
     * This method will make the View for the movies data visible and
     * hide the loading indicator.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMoviesDataView() {
        /* First, hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        // Finally, show mRecyclerView
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible while
     * hide the loading indicator.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     *
     * @param errorMessage the error message to show
     */

    private void showErrorMessage(String errorMessage) {
        /* First, hide loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        showAlertDialog(errorMessage);
    }

    private final LoaderManager.LoaderCallbacks<List<Movie>> moviesLoaderListener = new LoaderManager.LoaderCallbacks<List<Movie>>() {
        @Override
        public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
            showLoading();
            return new MyAsyncTaskLoader(getBaseContext());
        }

        @Override
        public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> moviesList) {
            if (moviesList != null) {
                showMoviesDataView();
                // Set the movies List to Adapter
                mMovieAdapter.setMoviesList(moviesList);
            } else {
                showErrorMessage(getBaseContext().getResources().getString(R.string.http_api_unauthorized_error_message));
            }

        }

        @Override
        public void onLoaderReset(Loader<List<Movie>> loader) {
            mMovieAdapter.setMoviesList(null);
        }
    };

    private final LoaderManager.LoaderCallbacks<Cursor> favoritesLoaderListener
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            showLoading();
            return new MyFavoritesAsyncTaskLoader(getBaseContext());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            Log.d(TAG, "onLoadFinished Favorites: " + data.getCount());

            showMoviesDataView();
            // Update the data that the adapter uses to create ViewHolders
            mMovieAdapter.swapCursor(data);

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mMovieAdapter.swapCursor(null);
        }
    };

    // In onStart, if preferences have been changed, refresh the data and set the flag to false
    @Override
    protected void onStart() {
        super.onStart();

        if (PREFERENCES_HAVE_BEEN_UPDATED) {
            Log.d(TAG, "onStart: preferences were updated");

            if (NetworkUtils.isConnected(getBaseContext())) {
                if (mMoviesType.equals(FAVORITES_KEY)) {
                    getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, favoritesLoaderListener);
                } else {
                    getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
                }
            } else {
                showErrorMessage(getBaseContext().getResources().getString(R.string.network_error_message));
            }
            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks. */
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*
         * Set this flag to true so that when control returns to MainActivity, it can refresh the
         * data.
         */
        PREFERENCES_HAVE_BEEN_UPDATED = true;

        // Set preference values just changed
        if (key.equals(getString(R.string.pref_language_key))) {
            String language = sharedPreferences.getString(key, getResources().getString(R.string.pref_language_english_key));
            mLanguage = language.equals(getBaseContext().getResources().getString(R.string.pref_language_device_key)) ? Locale.getDefault().getLanguage() : language;
        }


    }

    private static class MyAsyncTaskLoader extends AsyncTaskLoader<List<Movie>> {

        List<Movie> mMoviesList = null;

        public MyAsyncTaskLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            if (mMoviesList != null) {
                deliverResult(mMoviesList);
            } else {
                forceLoad();
            }

        }

        @Override
        public List<Movie> loadInBackground() {

            URL moviesRequestUrl = NetworkUtils.buildUrlWithSortingType(mMoviesType, mLanguage);

            try {
                String jsonMoviesResponse = NetworkUtils
                        .getResponseFromHttpUrl(moviesRequestUrl);

                return TheMovieDBJsonUtils.parseMoviesJson(jsonMoviesResponse);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Sends the result of the load to the registered listener.
         *
         * @param moviesList The result of the load
         */
        public void deliverResult(List<Movie> moviesList) {
            mMoviesList = moviesList;
            super.deliverResult(moviesList);
        }

    }


    private static class MyFavoritesAsyncTaskLoader extends AsyncTaskLoader<Cursor> {
        public MyFavoritesAsyncTaskLoader(Context baseContext) {
            super(baseContext);
        }

        // Initialize a Cursor, this will hold all the favorites data
        Cursor mMovieData = null;

        // onStartLoading() is called when a loader first starts loading data
        @Override
        protected void onStartLoading() {
            Log.d(TAG, "onStartLoading");

            if (mMovieData != null) {
                // Delivers any previously loaded data immediately
                deliverResult(mMovieData);
            } else {
                // Force a new load
                forceLoad();
            }
        }

        // loadInBackground() performs asynchronous loading of data
        @Override
        public Cursor loadInBackground() {
            // Will implement to load data

            Log.d(TAG, "loadInBackground");

            // Query and load all movie data in the background;

            try {
                return getContext().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);

            } catch (Exception e) {
                Log.e(TAG, "Failed to asynchronously load data.");
                e.printStackTrace();
                return null;
            }
        }

        // deliverResult sends the result of the load, a Cursor, to the registered listener
        public void deliverResult(Cursor data) {
            Log.d(TAG, "deliveryResult: " + data.getCount());

            mMovieData = data;
            super.deliverResult(data);
        }


    }

    /**
     * This method is used when we are resetting data, so that at one point in time during a
     * refresh of our data, you can see that there is no data showing.
     */

    private void invalidateData() {
        mMovieAdapter.setMoviesList(null);
        mMovieAdapter.swapCursor(null);
    }

    private void setActivityTitle() {

        //Set Activity Title
        String title;
        switch (mMoviesType) {
            case MainActivity.MOST_POPULAR_KEY:
                title = getResources().getString(R.string.most_popular_activity_title);
                break;
            case MainActivity.TOP_RATED_KEY:
                title = getResources().getString(R.string.highest_rated_activity_title);
                break;
            case MainActivity.FAVORITES_KEY:
                title = getResources().getString(R.string.my_favorites_activity_title);
                break;
            default:
                title = getString(R.string.app_name);
        }
        this.setTitle(title);

    }

    /**
     * This method dynamically calculate the number of columns and the layout would adapt to the screen size and orientation
     */
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
//        int widthDivider = 400;
        int widthDivider = 300;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Put the order type in the outState bundle
        outState.putString(MOVIES_TYPE_TEXT_KEY, mMoviesType);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.main, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (mMoviesType) {
            case MOST_POPULAR_KEY:
                menu.findItem(R.id.action_sort_order_popular).setChecked(true);
                break;
            case TOP_RATED_KEY:
                menu.findItem(R.id.action_sort_order_rated).setChecked(true);
                break;
            case FAVORITES_KEY:
                menu.findItem(R.id.action_favorites).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_order_popular:
                mMoviesType = MOST_POPULAR_KEY;

                // set mMovieAdapter equal to a new MovieAdapter
                mMovieAdapter = new MovieAdapter(this, MainActivity.this, mMoviesType);

                /* attaches adapter to the RecyclerView in layout. */
                mRecyclerView.setAdapter(mMovieAdapter);

                invalidateData();
                if (NetworkUtils.isConnected(getBaseContext())) {
                    getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
                } else {
                    showErrorMessage(getBaseContext().getResources().getString(R.string.network_error_message));
                }
                break;
            case R.id.action_sort_order_rated:
                mMoviesType = TOP_RATED_KEY;

                // set mMovieAdapter equal to a new MovieAdapter
                mMovieAdapter = new MovieAdapter(this, MainActivity.this, mMoviesType);

                /* attaches adapter to the RecyclerView in layout. */
                mRecyclerView.setAdapter(mMovieAdapter);

                invalidateData();
                if (NetworkUtils.isConnected(getBaseContext())) {
                    getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
                } else {
                    showErrorMessage(getBaseContext().getResources().getString(R.string.network_error_message));
                }
                break;
            case R.id.action_favorites:
                mMoviesType = FAVORITES_KEY;

                // set mMovieAdapter equal to a new MovieAdapter
                mMovieAdapter = new MovieAdapter(this, MainActivity.this, mMoviesType);

                /* attaches adapter to the RecyclerView in layout. */
                mRecyclerView.setAdapter(mMovieAdapter);

                Log.d(TAG, "OnOptionsItemSelected: " + mMoviesType);

                invalidateData();
                getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, favoritesLoaderListener);
                break;
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                break;
        }

        setActivityTitle();

        return super.onOptionsItemSelected(item);

    }

    private void showAlertDialog(final String errorMessage) {

        AlertDialog alertDialog = new AlertDialog.Builder(
                MainActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.alert_dialog_title));

        // Make a choice mandatory
        alertDialog.setCancelable(false);

        // Setting Dialog Message
        alertDialog.setMessage(errorMessage);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_offline);

        // Setting OK Button
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.open_favorite_button_title),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // load favorites movies
                        mMoviesType = FAVORITES_KEY;

                        // set mMovieAdapter equal to a new MovieAdapter
                        mMovieAdapter = new MovieAdapter(MainActivity.this, MainActivity.this, mMoviesType);

                        /* attaches adapter to the RecyclerView in layout. */
                        mRecyclerView.setAdapter(mMovieAdapter);

                        invalidateData();
                        getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, favoritesLoaderListener);

                        setActivityTitle();

                        // close alert dialog
                        dialog.dismiss();// use dismiss to cancel alert dialog
                    }
                });
        // Setting Retry Button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.retry_button_title),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // retry reloading
                        if (NetworkUtils.isConnected(getBaseContext())) {
                            getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesLoaderListener);
                        } else {
                            showErrorMessage(errorMessage);
                        }
                        dialog.dismiss();// use dismiss to cancel alert dialog
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

}
