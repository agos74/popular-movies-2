package com.udacity.popularmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elmargomez.typer.Font;
import com.elmargomez.typer.Typer;
import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.data.MovieContract;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.utils.DateUtils;
import com.udacity.popularmovies.utils.TheMovieDBJsonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private Movie movie;
    private String mFirstTrailerKey;
    private boolean mFromFavorites;
    private static boolean FAVORITE_HAS_BEEN_REMOVED = false;

    //ButterKnife Binding
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.backdrop_iv)
    ImageView mBackdropIv;
    @BindView(R.id.poster_iv)
    ImageView mPosterIv;
    //    @BindView(R.id.title_tv)
//    TextView mTitleTv;
    @BindView(R.id.original_title_tv)
    TextView mOriginalTitleTv;
    @BindView(R.id.release_date_tv)
    TextView mReleaseDateTv;
    @BindView(R.id.rating_tv)
    TextView mRatingTv;
    @BindView(R.id.rating_bar)
    RatingBar mRatingBar;
    @BindView(R.id.plot_synopsis_tv)
    TextView mPlotSynopsisTv;
    @BindView(R.id.reviews_title_tv)
    TextView mReviewsTitleTv;
    @BindView(R.id.trailers_title_tv)
    TextView mTrailersTitleTv;
    @BindView(R.id.favorite_fab)
    FloatingActionButton mFavoriteFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        movie = intent != null ? (Movie) intent.getParcelableExtra("Movie") : null;

        mFromFavorites = intent.getBooleanExtra("favorites", false);

        if (movie == null) {
            // Movie data not found in intent
            closeOnError();
            return;
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        populateUI();

        //load trailers fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragmentTrailers = fm.findFragmentById(R.id.fragmentContainer_trailers);
        if (fragmentTrailers == null) {
            fragmentTrailers = new TrailerListFragment();

            Bundle args = new Bundle();
            args.putString("movieId", movie.getId());
            fragmentTrailers.setArguments(args);

            fm.beginTransaction()
                    .add(R.id.fragmentContainer_trailers, fragmentTrailers)
                    .commit();
        }

        //load reviews fragment
        Fragment fragmentReviews = fm.findFragmentById(R.id.fragmentContainer_reviews);
        if (fragmentReviews == null) {
            fragmentReviews = new ReviewListFragment();

            Bundle args = new Bundle();
            args.putString("movieId", movie.getId());
            fragmentReviews.setArguments(args);

            fm.beginTransaction()
                    .add(R.id.fragmentContainer_reviews, fragmentReviews)
                    .commit();
        }

    }

    public void setReviewsTitleText(String numReviews) {
        mReviewsTitleTv.setText(getString(R.string.reviews_label) + " (" + numReviews + ")");
    }

    public void setTrailersTitleText(String numTrailers) {
        mTrailersTitleTv.setText(getString(R.string.trailers_label) + " (" + numTrailers + ")");
    }

    private void populateUI() {

        mCollapsingToolbar.setTitle(movie.getTitle());

        Typeface font = Typer.set(this).getFont(Font.ROBOTO_MEDIUM);
        mCollapsingToolbar.setExpandedTitleTypeface(font);

        //display Movie detail data
        Uri backdropUri = Uri.parse(TheMovieDBJsonUtils.TMDB_IMAGE_PATH).buildUpon()
                .appendEncodedPath(TheMovieDBJsonUtils.TMDB_IMAGE_WIDTH_LARGE)
                .appendEncodedPath(movie.getBackdrop())
                .build();
        Picasso.with(mBackdropIv.getContext())
                .load(backdropUri)
                .error(R.mipmap.ic_launcher)
                .placeholder(R.drawable.progress_animation)
                .into(mBackdropIv);

        Uri posterUri = Uri.parse(TheMovieDBJsonUtils.TMDB_IMAGE_PATH).buildUpon()
                .appendEncodedPath(TheMovieDBJsonUtils.TMDB_IMAGE_WIDTH_MEDIUM)
                .appendEncodedPath(movie.getPoster())
                .build();
        Picasso.with(mPosterIv.getContext())
                .load(posterUri)
                .error(R.mipmap.ic_launcher)
                .placeholder(R.drawable.progress_animation)
                .into(mPosterIv);

//        mTitleTv.setText(movie.getTitle());
        mOriginalTitleTv.setText(movie.getOriginalTitle());
        mReleaseDateTv.setText(DateUtils.getDateFormatted(movie.getReleaseDate()));
        String rating = movie.getRating();
        mRatingTv.setText(rating);
        mRatingBar.setRating(getRatingFloat(rating));
        mPlotSynopsisTv.setText(movie.getPlotSynopsis());

        if (isFavorite(movie.getId())) {
            // Set star full icon
            mFavoriteFab.setImageResource(R.drawable.ic_star_24px);
        }

    }

    private float getRatingFloat(String rating) {
        double ratingDouble = Float.valueOf(rating);
        float ratingFloat = (float) ((ratingDouble * 5) / 10);
        Log.d(TAG, "rating: " + String.valueOf(ratingFloat));
        return ratingFloat;
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    /**
     * onClickFavorite is called when the "Star" button is clicked.
     * It add or remove favorite movie into the underlying database.
     */
    @OnClick(R.id.favorite_fab)
    public void onClickFavorite(View view) {
        // Insert or remove Favorite Movie via a ContentResolver

        // Check if movie is in the favorites list
        boolean isFavorite = isFavorite(movie.getId());

        if (isFavorite) { //Delete favorite

            // Build appropriate uri with String row id appended
            String stringId = movie.getId();
            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(stringId).build();

            // Delete a single row of data using a ContentResolver
            int movieDeleted = getContentResolver().delete(uri, null, null);

            if (movieDeleted > 0) {
                Toast.makeText(getBaseContext(), R.string.favorite_delete_message, Toast.LENGTH_SHORT).show();
            }

            // Set star outline icon
            mFavoriteFab.setImageResource(R.drawable.ic_star_outline_24px);

            FAVORITE_HAS_BEEN_REMOVED = true;

        } else { // Add favorite

            // Create new empty ContentValues object
            ContentValues contentValues = new ContentValues();
            // Put the movie details into the ContentValues
            contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
            contentValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP, movie.getBackdrop());
            contentValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
            contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER, movie.getPoster());
            contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
            contentValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, movie.getPlotSynopsis());
            contentValues.put(MovieContract.MovieEntry.COLUMN_RATING, movie.getRating());
            contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());

            // Insert the content values via a ContentResolver
            Uri uriAdded = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

            if (uriAdded != null) {
                Toast.makeText(getBaseContext(), R.string.favorite_add_message, Toast.LENGTH_SHORT).show();
            }

            // Set star full icon
            mFavoriteFab.setImageResource(R.drawable.ic_star_24px);

            FAVORITE_HAS_BEEN_REMOVED = false;

        }

    }

    // Check if movie with movieId is in the favorites list
    private boolean isFavorite(String movieId) {

        boolean isFavorite = false;

        // Build appropriate uri with String row id appended
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(movieId).build();

        Cursor retCursor = null;
        try {
            String[] projection = {MovieContract.MovieEntry.COLUMN_MOVIE_ID};
            retCursor = getContentResolver().query(uri, projection, null, null, null);
        } finally {

            if (retCursor != null) {
                isFavorite = retCursor.getCount() > 0;

                // Close the cursor
                retCursor.close();
            }
        }

        return isFavorite;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.d(TAG, "onCreateOptionsMenu: " + mFirstTrailerKey);

        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.detail, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // When the home button is pressed, take the user back to the Main Activity
            case android.R.id.home:
                boolean flagRefresh = FAVORITE_HAS_BEEN_REMOVED && mFromFavorites;
                Intent returnIntent = new Intent();
                returnIntent.putExtra("flag_refresh", flagRefresh);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_share:
                if (mFirstTrailerKey != null) {
                    Intent shareIntent = createShareMovieIntent(mFirstTrailerKey);
                    shareIntent = Intent.createChooser(shareIntent, getString(R.string.share_trailer_using_message));
                    startActivity(shareIntent);
                    return true;
                }
                Toast.makeText(this, getString(R.string.share_no_trailer_message), Toast.LENGTH_LONG).show();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set mFirstTrailerKey for use in action share.
     *
     * @param firstTrailerKey key for first movie trailer
     */
    public void setFirstTrailerKey(String firstTrailerKey) {
        Log.d(TAG, "setFirstTrailerKey: " + firstTrailerKey);
        mFirstTrailerKey = firstTrailerKey;
    }

    /**
     * Uses the ShareCompat Intent builder to create our Trailer intent for sharing.  All we need
     * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     *
     * @param firstTrailerKey key for first movie trailer
     * @return the Intent to use to share our movie trailer
     */
    private Intent createShareMovieIntent(String firstTrailerKey) {

        String shareLink = "http://www.youtube.com/watch?v=" + firstTrailerKey;

        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                .putExtra(Intent.EXTRA_TEXT, shareLink);
        return shareIntent;
    }


}
