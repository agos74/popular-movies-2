package com.udacity.popularmovies.utilities;

/*
  Created by Agostino on 22/02/2018.
 */

import android.net.Uri;

import com.udacity.popularmovies.BuildConfig;
import com.udacity.popularmovies.DetailActivity;
import com.udacity.popularmovies.MainActivity;
import com.udacity.popularmovies.TrailerListFragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


/**
 * These utilities will be used to communicate with the network.
 */
public class NetworkUtils {


    private static final String THEMOVIEDB_BASE_URL = "http://api.themoviedb.org/3/movie";

    private static final String PARAM_API_KEY = "api_key";

    private static final String THEMOVIEDB_API_KEY = BuildConfig.API_KEY;

    /* The sort type we want our API to return */
    private static final String POPULAR_ENDPOINT = "popular";
    private static final String TOP_RATED_ENDPOINT = "top_rated";

    private static final String VIDEOS_ENDPOINT = "videos";
    private static final String REVIEWS_ENDPOINT = "reviews";


    /**
     * Builds the URL used to query TheMovieDB.
     *
     * @param sortingType The keyword that will be queried for.
     * @return The URL to use to query TheMovieDB server.
     */
    public static URL buildUrlWithSortingType(String sortingType) {
        String endPoint;
        switch (sortingType) {
            case MainActivity.MOST_POPULAR_ORDER_KEY:
                endPoint = POPULAR_ENDPOINT;
                break;
            case MainActivity.TOP_RATED_ORDER_KEY:
                endPoint = TOP_RATED_ENDPOINT;
                break;
            default:
                endPoint = POPULAR_ENDPOINT;
        }
        Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                .appendEncodedPath(endPoint)
                .appendQueryParameter(PARAM_API_KEY, THEMOVIEDB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Builds the URL used to query TheMovieDB.
     *
     * @param movieId     The movieId that will be queried for.
     * @param requestType The request type (videos or reviews).
     * @return The URL to use to query TheMovieDB server.
     */
    public static URL buildUrlWithMovieId(String movieId, String requestType) {
        String endPoint = null;
        switch (requestType) {
            case TrailerListFragment.VIDEOS_REQUEST_KEY:
                endPoint = VIDEOS_ENDPOINT;
                break;
            case TrailerListFragment.REVIEWS_REQUEST_KEY:
                endPoint = REVIEWS_ENDPOINT;
                break;
        }
        Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                .appendEncodedPath(movieId)
                .appendEncodedPath(endPoint)
                .appendQueryParameter(PARAM_API_KEY, THEMOVIEDB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }


    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

}
