package com.udacity.popularmovies;

import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.model.Video;
import com.udacity.popularmovies.utilities.NetworkUtils;
import com.udacity.popularmovies.utilities.TheMovieDBJsonUtils;
import com.udacity.popularmovies.utilities.VideoUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Created by Agostino on 11/03/2018.
 */

public class TrailerListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Video>> {

    private static final String TAG = Video.class.getSimpleName();

    private static final int VIDEOS_LOADER_ID = 1;

    public static final String VIDEOS_REQUEST_KEY = "v";
    public static final String REVIEWS_REQUEST_KEY = "r";

    //ButterKnife Binding
    @BindView(R.id.recyclerview_trailers)
    RecyclerView mTrailerRecyclerView;

    private static String movieId;

    private VideoAdapter mVideoAdapter;

    private List<Video> mVideoList;

    private List<Video> getVideoList() {
        int amount = 2;
        List<Video> list = new ArrayList<>(amount);
        while (list.size() < amount) {
            Video video = new Video("58f7e846c3a3684529000ea3", "3jBFwltrxJw", "Official Trailer (Extended version)", "YouTube", "Trailer");
            list.add(video);
        }
        return list;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        movieId = args.getString("movieId", null);

        // Initialize the AsyncTaskLoader
        getLoaderManager().initLoader(VIDEOS_LOADER_ID, null, TrailerListFragment.this);

        Log.d(TAG, "OnCreate: movieId=".concat(movieId));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trailer_list, container, false);
        ButterKnife.bind(this, view);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mTrailerRecyclerView.setLayoutManager(horizontalLayoutManager);

        Log.d(TAG, "OnCreateView");

        updateUI();
        return view;
    }

    private void updateUI() {
        mVideoAdapter = new VideoAdapter(null);
        mTrailerRecyclerView.setAdapter(mVideoAdapter);
    }

    @Override
    public Loader<List<Video>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "OnCreateLoader");
        return new MyAsyncTaskLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Video>> loader, List<Video> videoList) {
        if (videoList != null) {
            // Instead of iterating through every video, use mVideoAdapter.setVideoList and pass in the video List
            mVideoAdapter.setVideoList(videoList);
        }
        Log.d(TAG, "OnLoadFinished: videos=" + videoList.size());

    }

    @Override
    public void onLoaderReset(Loader<List<Video>> loader) {

    }

    private static class MyAsyncTaskLoader extends AsyncTaskLoader<List<Video>> {

        List<Video> mVideoList = new ArrayList<>();

        public MyAsyncTaskLoader(Context context) {
            super(context);
            onForceLoad();
        }

        @Override
        protected void onStartLoading() {
            Log.d(TAG, "MyAsyncTask: OnStartLoading:videos=" + mVideoList.size());
            if (mVideoList != null) {
                deliverResult(mVideoList);
            } else {
                forceLoad();
            }

        }

        @Override
        public List<Video> loadInBackground() {
            Log.d(TAG, "MyAsyncTask: loadInBackground");

            URL videosRequestUrl = NetworkUtils.buildUrlWithMovieId(movieId, VIDEOS_REQUEST_KEY);

            try {
                String jsonVideosResponse = NetworkUtils
                        .getResponseFromHttpUrl(videosRequestUrl);

                return TheMovieDBJsonUtils.parseVideosJson(jsonVideosResponse);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Sends the result of the load to the registered listener.
         *
         * @param videoList The result of the load
         */
        public void deliverResult(List<Video> videoList) {
            Log.d(TAG, "MyAsyncTask: deliverResult:videos=" + videoList.size());

            mVideoList = videoList;
            super.deliverResult(videoList);
        }

    }

    static class VideoHolder extends RecyclerView.ViewHolder {

        //ButterKnife Binding
        @BindView(R.id.video_iv)
        ImageView mVideoIv;
        @BindView(R.id.name_tv)
        TextView mNameTv;

        private View view;

        public VideoHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            view = itemView;
        }

        public void bindData(Video video) {
            //video thumbnail
            Uri thumbnailUri = VideoUtils.getThumbnail(video);
            Picasso.with(mVideoIv.getContext()).load(thumbnailUri).error(R.mipmap.ic_launcher).into(mVideoIv);

            mNameTv.setText(video.getName());
        }
    }

    private class VideoAdapter extends RecyclerView.Adapter<VideoHolder> {

        private List<Video> mVideoList;

        public VideoAdapter(List<Video> videoList) {
            mVideoList = videoList;
        }

        @Override
        public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.trailer_item, parent, false);
            return new VideoHolder(view);
        }

        @Override
        public void onBindViewHolder(final VideoHolder holder, int position) {
            final Video video = mVideoList.get(position);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
//                    Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + video.getKey()));
//                    context.startActivity(appIntent);

                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + video.getKey()));
                    context.startActivity(webIntent);
                }
            });

            holder.bindData(video);
        }

        @Override
        public int getItemCount() {
            return mVideoList.size();
        }

        public void setVideoList(List<Video> videoList) {
            mVideoList = videoList;
            notifyDataSetChanged();
        }
    }
}
