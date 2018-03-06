package com.udacity.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.utilities.TheMovieDBJsonUtils;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Agostino on 21/02/2018.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {


    private List<Movie> mMoviesList = new ArrayList<>();

    /**
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final MovieAdapterOnClickHandler mClickHandler;

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
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a movie grid item.
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mMovieImageView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mMovieImageView = view.findViewById(R.id.iv_movie);
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
            Movie movie = mMoviesList.get(adapterPosition);
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
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_grid_item;
        LayoutInflater inflater = LayoutInflater.from(context);

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
        Movie movieForThisPosition = mMoviesList.get(position);

        Uri imgUri = Uri.parse(TheMovieDBJsonUtils.TMDB_POSTER_PATH).buildUpon()
                .appendEncodedPath(TheMovieDBJsonUtils.TMDB_POSTER_WIDTH_MEDIUM)
                .appendEncodedPath(movieForThisPosition.getPoster())
                .build();

        Picasso
                .with(holder.mMovieImageView.getContext())
                .load(imgUri)
                .error(R.mipmap.ic_launcher)
//                .placeholder(R.drawable.progress_animation)
                .into(holder.mMovieImageView);

        holder.mMovieImageView.setContentDescription(movieForThisPosition.getTitle());
    }

    @Override
    public int getItemCount() {
        return (mMoviesList == null) ? 0 : mMoviesList.size();
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
