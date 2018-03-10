package com.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.utilities.NetworkUtils;
import com.udacity.popularmovies.utilities.TheMovieDBJsonUtils;

import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler, LoaderManager.LoaderCallbacks<List<Movie>> {

    public static final String MOST_POPULAR_ORDER_KEY = "p";
    public static final String TOP_RATED_ORDER_KEY = "t";

    private static final String MOST_POPULAR_TITLE = "Most Popular Movies";
    private static final String TOP_RATED_TITLE = "Highest Rated Movies";

    //ButterKnife Binding
    @BindView(R.id.recyclerview_main)
    RecyclerView mRecyclerView;
    /* This Layout with TextView and Button is used to display errors and will be hidden if there are no errors */
    @BindView(R.id.layout_error)
    LinearLayout mErrorLayout;
    /*
     * The ProgressBar that will indicate to the user that we are loading data. It will be
     * hidden when no data is loading.
     */
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.retry_button)
    Button mRetryButton;


    private MovieAdapter mMovieAdapter;

    private static final String SORTING_TYPE_TEXT_KEY = "sorting_type";

    private static String mSortingType = MOST_POPULAR_ORDER_KEY;

    private static final int MOVIES_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);


        /*
         * GridLayoutManager to show the movie posters
         */
        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns());

        /*
         * GridLayoutManager to show the movie posters in grid of 2 or 4 columns
         */
/*
        if (super.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            layoutManager = new GridLayoutManager(this, 4);
        }
*/


        /* Set the layoutManager on mRecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

         /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        // set mMovieAdapter equal to a new MovieAdapter
        mMovieAdapter = new MovieAdapter(this);

        /* attaches adapter to the RecyclerView in layout. */
        mRecyclerView.setAdapter(mMovieAdapter);


        //If savedInstanceState is not null and contains SORTING_TYPE_TEXT_KEY, set mSortingType with the value
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SORTING_TYPE_TEXT_KEY)) {
                mSortingType = savedInstanceState
                        .getString(SORTING_TYPE_TEXT_KEY);
            }
        }

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, MainActivity.this);
            }
        });


        // Initialize the AsyncTaskLoader
        getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, MainActivity.this);

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
        startActivity(intentToStartDetailActivity);
    }


    /**
     * This method will make the View for the movies data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMoviesDataView() {
        /* First, make sure the error is invisible */
        mErrorLayout.setVisibility(View.INVISIBLE);
        // Show mRecyclerView, not mMovieImageView
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movies
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        // Hide mRecyclerView, not mMovieImageView
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {

        mLoadingIndicator.setVisibility(View.VISIBLE);

        return new MyAsyncTaskLoader(this);
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

            String orderType = mSortingType;
            URL moviesRequestUrl = NetworkUtils.buildUrlWithSortingType(orderType);

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


    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> moviesList) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (moviesList != null) {
            showMoviesDataView();
            // Instead of iterating through every movie, use mMovieAdapter.setMoviesList and pass in the movies List
            mMovieAdapter.setMoviesList(moviesList);
        } else {
            showErrorMessage();
        }

        setActivityTitle();

    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {

    }

    /**
     * This method is used when we are resetting data, so that at one point in time during a
     * refresh of our data, you can see that there is no data showing.
     */

    private void invalidateData() {
        mMovieAdapter.setMoviesList(null);
    }

    private void setActivityTitle() {

        //Set Activity Title
        String title;
        switch (mSortingType) {
            case MainActivity.MOST_POPULAR_ORDER_KEY:
                title = MOST_POPULAR_TITLE;
                break;
            case MainActivity.TOP_RATED_ORDER_KEY:
                title = TOP_RATED_TITLE;
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
        outState.putString(SORTING_TYPE_TEXT_KEY, mSortingType);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_order_popular:
                mSortingType = MOST_POPULAR_ORDER_KEY;
                break;
            case R.id.action_sort_order_rated:
                mSortingType = TOP_RATED_ORDER_KEY;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        invalidateData();
        getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        return true;

    }


}
