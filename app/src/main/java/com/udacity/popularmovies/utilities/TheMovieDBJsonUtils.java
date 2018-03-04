package com.udacity.popularmovies.utilities;


import com.udacity.popularmovies.model.Movie;

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

    public static final String TMDB_POSTER_PATH = "http://image.tmdb.org/t/p/";
    public static final String TMDB_POSTER_WIDTH_MEDIUM = "w185/";
    //public static final String TMDB_POSTER_WIDTH_LARGE = "w342/";

    public static List<Movie> parseMoviesJson(String moviesJsonStr) throws JSONException {


        /* Movies information. Each movie is an element of the "results" array */
        final String TMDB_RESULTS = "results";

        final String TMDB_ID = "id";
        final String TMDB_TITLE = "title";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_POSTER = "poster_path";
        final String TMDB_PLOT_SYNOPSIS = "overview";
        final String TMDB_RATING = "vote_average";
        final String TMDB_RELEASE_DATE = "release_date";

        //error message code
        final String TMDB_MESSAGE_CODE = "cod";

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
                movie.setTitle(movieJson.optString(TMDB_ID));
            }

            if (movieJson.has(TMDB_TITLE)) {
                movie.setTitle(movieJson.optString(TMDB_TITLE));
            }

            if (movieJson.has(TMDB_ORIGINAL_TITLE)) {
                movie.setOriginalTitle(movieJson.optString(TMDB_ORIGINAL_TITLE));
            }

            if (movieJson.has(TMDB_POSTER)) {
                movie.setPoster(movieJson.optString(TMDB_POSTER));
            }

            if (movieJson.has(TMDB_PLOT_SYNOPSIS)) {
                movie.setPlotSynopsis(movieJson.optString(TMDB_PLOT_SYNOPSIS));
            }

            if (movieJson.has(TMDB_RATING)) {
                movie.setRating(movieJson.getString(TMDB_RATING));
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

}
