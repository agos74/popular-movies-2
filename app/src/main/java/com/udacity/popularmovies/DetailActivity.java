package com.udacity.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.utilities.TheMovieDBJsonUtils;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        Movie movie = intent != null ? (Movie) intent.getParcelableExtra("Movie") : null;

        if (movie == null) {
            // Movie data not found in intent
            closeOnError();
            return;
        }

        populateUI(movie);

        ActionBar actionBar = this.getSupportActionBar();

        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    private void populateUI(Movie movie) {

        ImageView mPosterIv = findViewById(R.id.poster_iv);
        TextView mTitleTv = findViewById(R.id.title_tv);
        TextView mOriginalTitleTv = findViewById(R.id.original_title_tv);
        TextView mReleaseDateTv = findViewById(R.id.release_date_tv);
        TextView mRatingTv = findViewById(R.id.rating_tv);
        TextView mPlotSynopsisTv = findViewById(R.id.plot_synopsis_tv);

        //display Movie detail data
        Uri imgUri = Uri.parse(TheMovieDBJsonUtils.TMDB_POSTER_PATH).buildUpon()
                .appendEncodedPath(TheMovieDBJsonUtils.TMDB_POSTER_WIDTH_MEDIUM)
                .appendEncodedPath(movie.getPoster())
                .build();

        Picasso.with(mPosterIv.getContext()).load(imgUri).into(mPosterIv);
        mPosterIv.setContentDescription(movie.getTitle());

        mTitleTv.setText(movie.getTitle());
        mOriginalTitleTv.setText(movie.getOriginalTitle());
        mReleaseDateTv.setText(movie.getReleaseDate());
        String rating = movie.getRating().concat("/10");
        mRatingTv.setText(rating);
        mPlotSynopsisTv.setText(movie.getPlotSynopsis());

    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the Main Activity
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
