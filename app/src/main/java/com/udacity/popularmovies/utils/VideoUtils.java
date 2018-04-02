package com.udacity.popularmovies.utils;

import android.net.Uri;
import android.util.Log;

import com.udacity.popularmovies.model.Video;

/*
 * Created by Agostino on 12/03/2018.
 */

public class VideoUtils {

    private static final String TAG = VideoUtils.class.getSimpleName();

    private static final String YOUTUBE_SITE_KEY = "YouTube";
    private static final String YOUTUBE_THUMBNAIL_BASE_URL = "https://img.youtube.com/vi/";

    // Custom thumbnail in 320 x 180 small image resolution
    private static final String YOUTUBE_SMALL_IMAGE_KEY = "mqdefault";
    // Custom thumbnail in 480 x 360 standard image resolution
    private static final String YOUTUBE_STANDARD_IMAGE_KEY = "0";
    // Custom thumbnail  in 720p or 1080p HD image resolution
    private static final String YOUTUBE_HD_IMAGE_KEY = "maxresdefault";


    public static Uri getThumbnail(Video video) {
        Uri thumbnailUri = null;
        switch (video.getSite()) {
            //String url = "https://img.youtube.com/vi/" + {ID} + "/0.jpg";
            case YOUTUBE_SITE_KEY:
                thumbnailUri = Uri.parse(YOUTUBE_THUMBNAIL_BASE_URL).buildUpon()
                        .appendEncodedPath(video.getKey())
                        .appendEncodedPath(YOUTUBE_STANDARD_IMAGE_KEY.concat(".jpg"))
                        .build();
                break;
        }
        Log.d(TAG, "thumbnailUri: " + thumbnailUri);
        return thumbnailUri;

    }


}
