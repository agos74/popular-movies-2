# popular-movies-2
Popular Movies, Stage 2

App for Udacity Android Developer Nanodegree Course.

IMPORTANT!
This app need API_KEY from website themoviedb.org, used in NetworkUtils.java, row N.36:
private static final String THEMOVIEDB_API_KEY = BuildConfig.API_KEY;  

Used this guide to hide the API_KEY in gradle.properties:
https://richardroseblog.wordpress.com/2016/05/29/hiding-secret-api-keys-from-git/

Used this library for API 16 min SDK compatibility of ImageView: 
https://github.com/nuuneoi/AdjustableImageView

Settings
- Data Source Language: language key passed to get data using themoviedb.org API, English (default) or Current Device Language.
- Video Type: video type to get trailers using themoviedb.org API, Only Trailer or All (Trailer, Teaser, Clip, etc.).

For personal purpose i have translated strings only for italian language, default is english.
