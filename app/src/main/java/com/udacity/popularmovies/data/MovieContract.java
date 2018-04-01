package com.udacity.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Agostino on 19/03/2018.
 */

public class MovieContract {

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.udacity.popularmovies";

    // The base content URI = "content://" + <authority>
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Path for the "movies" directory
    public static final String PATH_MOVIES = "movies";

    public static final class MovieEntry implements BaseColumns {

        // MovieEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        // Movie table and column names
        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ORIGINAL_TITLE = "originalTitle";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_PLOT_SYNOPSIS = "plotSynopsis";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_BACKDROP = "backdrop";
    }

}
