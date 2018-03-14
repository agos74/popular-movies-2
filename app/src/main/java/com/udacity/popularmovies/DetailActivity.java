package com.udacity.popularmovies;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elmargomez.typer.Font;
import com.elmargomez.typer.Typer;
import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.utilities.TheMovieDBJsonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

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
    @BindView(R.id.plot_synopsis_tv)
    TextView mPlotSynopsisTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

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

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        populateUI(movie);

        //load trailers fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = new TrailerListFragment();

            Bundle args = new Bundle();
            args.putString("movieId", movie.getId());
            fragment.setArguments(args);

            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }

    }


    private void populateUI(Movie movie) {

        mCollapsingToolbar.setTitle(movie.getTitle());

        Typeface font = Typer.set(this).getFont(Font.ROBOTO_MEDIUM);
        mCollapsingToolbar.setExpandedTitleTypeface(font);

        //display Movie detail data
        Uri backdropUri = Uri.parse(TheMovieDBJsonUtils.TMDB_IMAGE_PATH).buildUpon()
                .appendEncodedPath(TheMovieDBJsonUtils.TMDB_IMAGE_WIDTH_LARGE)
                .appendEncodedPath(movie.getBackdrop())
                .build();
        Picasso.with(mBackdropIv.getContext()).load(backdropUri).error(R.mipmap.ic_launcher).into(mBackdropIv);

        Uri posterUri = Uri.parse(TheMovieDBJsonUtils.TMDB_IMAGE_PATH).buildUpon()
                .appendEncodedPath(TheMovieDBJsonUtils.TMDB_IMAGE_WIDTH_MEDIUM)
                .appendEncodedPath(movie.getPoster())
                .build();
        Picasso.with(mPosterIv.getContext()).load(posterUri).error(R.mipmap.ic_launcher).into(mPosterIv);

//        mTitleTv.setText(movie.getTitle());
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
