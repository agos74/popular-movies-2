package com.udacity.popularmovies.utilities;


import android.content.Context;
import android.util.Log;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.model.Review;
import com.udacity.popularmovies.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Agostino on 22/02/2018.
 */

public class TheMovieDBJsonUtils {

    public static final String TMDB_IMAGE_PATH = "http://image.tmdb.org/t/p/";
    public static final String TMDB_IMAGE_WIDTH_MEDIUM = "w185/";
    public static final String TMDB_IMAGE_WIDTH_LARGE = "w500/";

    private static final String TMDB_RESULTS = "results";

    //error message code
    private static final String TMDB_MESSAGE_CODE = "cod";

    private static final String TMDB_TYPE_TRAILER_KEY = "Trailer";

    private static final String TMDB_SITE_YOUTUBE_KEY = "YouTube";

    public static List<Movie> parseMoviesJson(String moviesJsonStr) throws JSONException {

        /* Movies information. Each movie is an element of the "results" array */

        final String TMDB_ID = "id";
        final String TMDB_RATING = "vote_average";
        final String TMDB_TITLE = "title";
        final String TMDB_POSTER = "poster_path";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_BACKDROP = "backdrop_path";
        final String TMDB_PLOT_SYNOPSIS = "overview";
        final String TMDB_RELEASE_DATE = "release_date";


        /* Movies List to hold movies */
        List<Movie> moviesList = new ArrayList<>();

        JSONObject moviesJson = new JSONObject(moviesJsonStr);

        /* Is there an error? */
        if (moviesJson.has(TMDB_MESSAGE_CODE)) {
            int errorCode = moviesJson.getInt(TMDB_MESSAGE_CODE);
            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    /* Invalid API key: You must be granted a valid key. */
                    return null;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* The resource you requested could not be found. */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

        for (int i = 0; i < moviesArray.length(); i++) {

            Movie movie = new Movie();

            /* Get the JSON object representing the movie */
            JSONObject movieJson = moviesArray.getJSONObject(i);

            if (movieJson.has(TMDB_ID)) {
                movie.setId(movieJson.optString(TMDB_ID));
            }

            if (movieJson.has(TMDB_RATING)) {
                movie.setRating(movieJson.getString(TMDB_RATING));
            }

            if (movieJson.has(TMDB_TITLE)) {
                movie.setTitle(movieJson.optString(TMDB_TITLE));
            }

            if (movieJson.has(TMDB_POSTER)) {
                movie.setPoster(movieJson.optString(TMDB_POSTER));
            }

            if (movieJson.has(TMDB_ORIGINAL_TITLE)) {
                movie.setOriginalTitle(movieJson.optString(TMDB_ORIGINAL_TITLE));
            }

            if (movieJson.has(TMDB_BACKDROP)) {
                movie.setBackdrop(movieJson.optString(TMDB_BACKDROP));
            }

            if (movieJson.has(TMDB_PLOT_SYNOPSIS)) {
                movie.setPlotSynopsis(movieJson.optString(TMDB_PLOT_SYNOPSIS));
            }

            if (movieJson.has(TMDB_RELEASE_DATE)) {
                movie.setReleaseDate(movieJson.optString(TMDB_RELEASE_DATE));
            }

            moviesList.add(movie);

            //for debug purpose
//            Log.d("Movie", movie.toString());

        }

        return moviesList;
    }

    public static List<Video> parseVideosJson(String videosJsonStr, boolean onlyTrailer, boolean onlyYoutube) throws JSONException {

        /* Videos information. Each video is an element of the "results" array */

        final String TMDB_ID = "id";
        final String TMDB_KEY = "key";
        final String TMDB_NAME = "name";
        final String TMDB_TYPE = "type";
        final String TMDB_SITE = "site";


        /* Video List to hold videos */
        List<Video> videoList = new ArrayList<>();

        JSONObject videosJson = new JSONObject(videosJsonStr);

        /* Is there an error? */
        if (videosJson.has(TMDB_MESSAGE_CODE)) {
            int errorCode = videosJson.getInt(TMDB_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    /* Invalid API key: You must be granted a valid key. */
                    return null;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* The resource you requested could not be found. */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray videosArray = videosJson.getJSONArray(TMDB_RESULTS);

        for (int i = 0; i < videosArray.length(); i++) {

            Video video = new Video();

            /* Get the JSON object representing the video */
            JSONObject videoJson = videosArray.getJSONObject(i);

            if (videoJson.has(TMDB_ID)) {
                video.setId(videoJson.optString(TMDB_ID));
            }

            if (videoJson.has(TMDB_KEY)) {
                video.setKey(videoJson.optString(TMDB_KEY));
            }

            if (videoJson.has(TMDB_NAME)) {
                video.setName(videoJson.optString(TMDB_NAME));
            }

            if (videoJson.has(TMDB_TYPE)) {
                video.setType(videoJson.optString(TMDB_TYPE));
            }

            if (videoJson.has(TMDB_SITE)) {
                video.setSite(videoJson.optString(TMDB_SITE));
            }

            boolean toAdd = true;

            //get only "Trailer" type?
            if (onlyTrailer) {
                if (!video.getType().equals(TMDB_TYPE_TRAILER_KEY)) {
                    toAdd = false;
                }
            }

            //get only "Youtube" site?
            if (toAdd && onlyYoutube) {
                if (!video.getSite().equals(TMDB_SITE_YOUTUBE_KEY)) {
                    toAdd = false;
                }
            }

            if (toAdd) {
                videoList.add(video);
            }

            //for debug purpose
            Log.d("Video", video.toString());

        }

        return videoList;
    }

    public static List<Review> parseReviewsJson(String reviewsJsonStr) throws JSONException {

        /* Reviews information. Each review is an element of the "results" array */

        final String TMDB_ID = "id";
        final String TMDB_AUTHOR = "author";
        final String TMDB_CONTENT = "content";
        final String TMDB_URL = "url";


        /* Review List to hold reviews */
        List<Review> reviewList = new ArrayList<>();

        JSONObject reviewsJson = new JSONObject(reviewsJsonStr);

        /* Is there an error? */
        if (reviewsJson.has(TMDB_MESSAGE_CODE)) {
            int errorCode = reviewsJson.getInt(TMDB_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    /* Invalid API key: You must be granted a valid key. */
                    return null;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* The resource you requested could not be found. */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray reviewsArray = reviewsJson.getJSONArray(TMDB_RESULTS);

        for (int i = 0; i < reviewsArray.length(); i++) {

            Review review = new Review();

            /* Get the JSON object representing the review */
            JSONObject reviewJson = reviewsArray.getJSONObject(i);

            if (reviewJson.has(TMDB_ID)) {
                review.setId(reviewJson.optString(TMDB_ID));
            }

            if (reviewJson.has(TMDB_AUTHOR)) {
                review.setAuthor(reviewJson.optString(TMDB_AUTHOR));
            }

            if (reviewJson.has(TMDB_CONTENT)) {
                review.setContent(reviewJson.optString(TMDB_CONTENT));
            }

            if (reviewJson.has(TMDB_URL)) {
                review.setUrl(reviewJson.optString(TMDB_URL));
            }

            reviewList.add(review);

            //for debug purpose
            Log.d("Review", review.toString());

        }

        return reviewList;
    }
}
