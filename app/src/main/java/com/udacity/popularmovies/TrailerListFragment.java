package com.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.model.Video;
import com.udacity.popularmovies.utilities.VideoUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Created by Agostino on 11/03/2018.
 */

public class TrailerListFragment extends Fragment {

    //ButterKnife Binding
    @BindView(R.id.recyclerview_trailers)
    RecyclerView mTrailerRecyclerView;


    private ArrayList<Video> getVideoList() {
        int amount = 2;
        ArrayList<Video> list = new ArrayList<>(amount);
        while (list.size() < amount) {
            Video video = new Video("58f7e846c3a3684529000ea3", "3jBFwltrxJw", "Official Trailer (Extended version)", "YouTube", "Trailer");
            list.add(video);
        }
        return list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trailer_list, container, false);
        ButterKnife.bind(this, view);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mTrailerRecyclerView.setLayoutManager(horizontalLayoutManager);
        updateUI();
        return view;
    }

    private void updateUI() {
        VideoAdapter mAdapter = new VideoAdapter(getVideoList());
        mTrailerRecyclerView.setAdapter(mAdapter);
    }

    static class VideoHolder extends RecyclerView.ViewHolder {

        //ButterKnife Binding
        @BindView(R.id.video_iv)
        ImageView mVideoIv;
        @BindView(R.id.name_tv)
        TextView mNameTv;

        private View view;

        private Video mVideo;

        public VideoHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            view = itemView;
        }

        public void bindData(Video video) {
            mVideo = video;
            //video thumbnail
            Uri thumbnailUri = VideoUtils.getThumbnail(video);
            Picasso.with(mVideoIv.getContext()).load(thumbnailUri).error(R.mipmap.ic_launcher).into(mVideoIv);

            mNameTv.setText(video.getName());
        }
    }


    private class VideoAdapter extends RecyclerView.Adapter<VideoHolder> {

        private final ArrayList<Video> mVideoList;

        public VideoAdapter(ArrayList<Video> videoList) {
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
    }
}
