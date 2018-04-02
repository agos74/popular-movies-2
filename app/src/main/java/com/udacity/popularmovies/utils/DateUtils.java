package com.udacity.popularmovies.utils;

/*
 * Created by Agostino on 02/04/2018.
 */

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static final String TAG = DateUtils.class.getSimpleName();

    public static String getDateFormatted(String dateString) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            // return dateString original
            return dateString;
        }
        String dateFormatted = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);

        Log.d(TAG, "date medium: " + dateFormatted);

        return dateFormatted;

    }

}
