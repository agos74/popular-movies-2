package com.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.udacity.popularmovies.model.Review;
import com.udacity.popularmovies.utilities.NetworkUtils;
import com.udacity.popularmovies.utilities.TheMovieDBJsonUtils;

import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/*
 * Created by Agostino on 18/03/2018.
 */

public class ReviewListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Review>> {

    private static final String TAG = ReviewListFragment.class.getSimpleName();

    private static final int REVIEWS_LOADER_ID = 10;

    public static final String REVIEWS_REQUEST_KEY = "r";

    private static final int MAX_TEXT_LINES = 3;

    //ButterKnife Binding
    @BindView(R.id.recyclerview_reviews)
    RecyclerView mReviewRecyclerView;

    private static String movieId;

    private ReviewAdapter mReviewAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        movieId = args.getString("movieId", null);

        // Initialize the AsyncTaskLoader
        getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, ReviewListFragment.this);

        Log.d(TAG, "OnCreate: movieId=".concat(movieId));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_list, container, false);
        ButterKnife.bind(this, view);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mReviewRecyclerView.setLayoutManager(horizontalLayoutManager);

        Log.d(TAG, "OnCreateView");

        updateUI();
        return view;
    }

    private void updateUI() {
        mReviewAdapter = new ReviewAdapter(null);
        mReviewRecyclerView.setAdapter(mReviewAdapter);
    }

    @Override
    public Loader<List<Review>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "OnCreateLoader");
        return new MyAsyncTaskLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Review>> loader, List<Review> reviewList) {
        if (reviewList != null) {
            // Instead of iterating through every review, use mReviewAdapter.setReviewList and pass in the review List
            mReviewAdapter.setReviewList(reviewList);
        }
        Log.d(TAG, "OnLoadFinished: reviews=" + (reviewList == null ? 0 : reviewList.size()));

        //update Reviews title with number
        Integer numReviews = reviewList == null ? 0 : reviewList.size();
        ((DetailActivity) getActivity()).setReviewsTitleText(numReviews.toString());

    }

    @Override
    public void onLoaderReset(Loader<List<Review>> loader) {

    }

    private static class MyAsyncTaskLoader extends AsyncTaskLoader<List<Review>> {

        List<Review> mReviewList;

        public MyAsyncTaskLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            Log.d(TAG, "MyAsyncTask: OnStartLoading:reviews=" + (mReviewList == null ? 0 : mReviewList.size()));
            if (mReviewList != null) {
                deliverResult(mReviewList);
            } else {
                forceLoad();
            }

        }

        @Override
        public List<Review> loadInBackground() {
            Log.d(TAG, "MyAsyncTask: loadInBackground");

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
            String language = sharedPreferences.getString(this.getContext().getResources().getString(R.string.pref_language_key), this.getContext().getResources().getString(R.string.pref_language_english_key));

            URL reviewsRequestUrl = NetworkUtils.buildUrlWithMovieId(movieId, REVIEWS_REQUEST_KEY, language);

            try {
                String jsonReviewsResponse = NetworkUtils
                        .getResponseFromHttpUrl(reviewsRequestUrl);

                return TheMovieDBJsonUtils.parseReviewsJson(jsonReviewsResponse);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Sends the result of the load to the registered listener.
         *
         * @param reviewList The result of the load
         */
        public void deliverResult(List<Review> reviewList) {
            Log.d(TAG, "MyAsyncTask: deliverResult:reviews=" + (reviewList == null ? 0 : reviewList.size()));

            mReviewList = reviewList;
            super.deliverResult(reviewList);
        }

    }

    static class ReviewHolder extends RecyclerView.ViewHolder {

        //ButterKnife Binding
        @BindView(R.id.content_tv)
        TextView mContentTv;
        @BindView(R.id.author_tv)
        TextView mAuthorTv;
        @BindView(R.id.toggle_btn)
        Button mButtonToggle;
        @BindView(R.id.url_tv)
        TextView mUrlTv;

        private Review mReview;

        public ReviewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(Review review) {
            mAuthorTv.setText(review.getAuthor());
            mContentTv.setText(review.getContent());

            mContentTv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    Log.d(TAG, "mContentTv line count: " + mContentTv.getLineCount());
                    if (mContentTv.getLineCount() > MAX_TEXT_LINES) {
                        mButtonToggle.setVisibility(View.VISIBLE);
                        mContentTv.setMaxLines(MAX_TEXT_LINES);
                    } else {
                        mButtonToggle.setVisibility(View.INVISIBLE);
                    }

                    itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                }

            });


            //set review url
            String tempString = itemView.getContext().getString(R.string.activity_movie_review_url_text);
            SpannableString content = new SpannableString(tempString);
            content.setSpan(new UnderlineSpan(), 0, tempString.length(), 0);
            mUrlTv.setText(content);
            mUrlTv.setTextColor(itemView.getContext().getResources().getColor(R.color.colorAccent));

            mReview = review;
        }


        @OnClick(R.id.toggle_btn)
        public void toggleClick() {

            if (mContentTv.getMaxLines() == MAX_TEXT_LINES) { //expand
                mContentTv.setMaxLines(Integer.MAX_VALUE);
                mButtonToggle.setRotation(0);
            } else {
                mContentTv.setMaxLines(MAX_TEXT_LINES); //collapse
                mButtonToggle.setRotation(180);
            }
        }

        @OnClick(R.id.url_tv)
        public void onClick(View view) {
//            String url = (String) mUrlTv.getText();
            String url = mReview.getUrl();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(browserIntent);
        }

    }

    private class ReviewAdapter extends RecyclerView.Adapter<ReviewHolder> {

        private List<Review> mReviewList;


        public ReviewAdapter(List<Review> reviewList) {
            mReviewList = reviewList;
        }

        @Override
        public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.review_item, parent, false);
            return new ReviewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ReviewHolder holder, int position) {
            final Review review = mReviewList.get(position);
            holder.bindData(review);
        }

        @Override
        public int getItemCount() {
            return (mReviewList == null) ? 0 : mReviewList.size();
        }

        public void setReviewList(List<Review> reviewList) {
            mReviewList = reviewList;
            notifyDataSetChanged();
        }
    }
}
