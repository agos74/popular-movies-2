package com.udacity.popularmovies;


import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Add general preferences, defined in the XML file in res->xml->pref_general
        addPreferencesFromResource(R.xml.pref_general);

    }
}
