package com.udacity.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.data.MovieContract;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.utilities.TheMovieDBJsonUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Created by Agostino on 21/02/2018.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();


    private List<Movie> mMoviesList;

    // Class variables for the Cursor that holds movies data and the Context
    private Cursor mCursor;
    private final Context mContext;

    private final String mMoviesType;

    /**
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final MovieAdapterOnClickHandler mClickHandler;

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public void swapCursor(Cursor c) {
        if (c != null) {
            Log.d(TAG, "swapCursor: " + c.getCount());
        } else {
            Log.d(TAG, "swapCursor: null");
        }

        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return;
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            notifyDataSetChanged();
        }
    }


    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler, Context mContext, String mMoviesType) {
        mClickHandler = clickHandler;
        this.mContext = mContext;
        this.mMoviesType = mMoviesType;

        Log.d(TAG, "MovieAdapter constructor: " + mMoviesType);

    }

    /**
     * Cache of the children views for a movie grid item.
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_movie)
        ImageView mMovieImageView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movie movie = new Movie();
            if (mMoviesType.equals(MainActivity.FAVORITES_KEY)) {

                // Indices for movie fields
                int movieIdIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                int titleIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
                int originalTitleIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE);
                int posterIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER);
                int releaseDateIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
                int ratingIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING);
                int plotSynopsisIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS);
                int backdropIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP);

                // Move cursor to right position
                mCursor.moveToPosition(adapterPosition);

                // Determine the values of the wanted data
                movie.setId(mCursor.getString(movieIdIndex));
                movie.setTitle(mCursor.getString(titleIndex));
                movie.setOriginalTitle(mCursor.getString(originalTitleIndex));
                movie.setPoster(mCursor.getString(posterIndex));
                movie.setReleaseDate(mCursor.getString(releaseDateIndex));
                movie.setRating(mCursor.getString(ratingIndex));
                movie.setPlotSynopsis(mCursor.getString(plotSynopsisIndex));
                movie.setBackdrop(mCursor.getString(backdropIndex));

            } else {
                movie = mMoviesList.get(adapterPosition);
            }

            mClickHandler.onClick(movie);
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @return A new MovieAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Log.d(TAG, "onCreateViewHolder mMoviesType: " + mMoviesType);

        int layoutIdForListItem = R.layout.movie_grid_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the poster
     * for this particular position, using the "position" argument that is conveniently
     * passed into us.
     */
    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position: " + String.valueOf(position));

        String poster;
        String title;

        if (mMoviesType.equals(MainActivity.FAVORITES_KEY)) {

            // Indices for the poster and title column
            int posterIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER);
            int titleIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);

            mCursor.moveToPosition(position); // get to the right location in the cursor

            // Determine the values of the wanted data
            poster = mCursor.getString(posterIndex);
            title = mCursor.getString(titleIndex);
        } else {

            Movie movieForThisPosition = mMoviesList.get(position);
            poster = movieForThisPosition.getPoster();
            title = movieForThisPosition.getTitle();
        }

        Uri imgUri = Uri.parse(TheMovieDBJsonUtils.TMDB_IMAGE_PATH).buildUpon()
                .appendEncodedPath(TheMovieDBJsonUtils.TMDB_IMAGE_WIDTH_MEDIUM)
                .appendEncodedPath(poster)
                .build();

        Picasso
                .with(holder.mMovieImageView.getContext())
                .load(imgUri)
                .error(R.mipmap.ic_launcher)
                .placeholder(R.drawable.progress_animation)
                .into(holder.mMovieImageView);

        holder.mMovieImageView.setContentDescription(title);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + mMoviesType);

        if (mMoviesType.equals(MainActivity.FAVORITES_KEY)) {
            return (mCursor == null) ? 0 : mCursor.getCount();
        } else {
            return (mMoviesList == null) ? 0 : mMoviesList.size();
        }
    }

    /**
     * This method is used to set the movie on a MovieAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param moviesList The new movies list to be displayed.
     */
    public void setMoviesList(List<Movie> moviesList) {
        mMoviesList = moviesList;
        notifyDataSetChanged();
    }

}
