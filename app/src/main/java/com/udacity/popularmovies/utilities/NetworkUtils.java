package com.udacity.popularmovies.utilities;

/**
 * Created by Agostino on 22/02/2018.
 */

import android.net.Uri;

import com.udacity.popularmovies.BuildConfig;
import com.udacity.popularmovies.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;


/**
 * These utilities will be used to communicate with the network.
 */
public class NetworkUtils {


    private static final String THEMOVIEDB_BASE_URL = "http://api.themoviedb.org/3/movie";

    /* The sort type we want our API to return */
    private static final String POPULAR_ENDPOINT = "popular";
    private static final String TOP_RATED_ENDPOINT = "top_rated";

    private static final String PARAM_API_KEY = "api_key";

    private static final String THEMOVIEDB_API_KEY = BuildConfig.API_KEY;

    /**
     * Builds the URL used to query TheMovieDB.
     *
     * @param sortType The keyword that will be queried for.
     * @return The URL to use to query TheMovieDB server.
     */
    public static URL buildUrl(String sortType) {
        String endPoint;
        switch (sortType) {
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
