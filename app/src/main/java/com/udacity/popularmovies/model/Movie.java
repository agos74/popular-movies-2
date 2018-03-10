package com.udacity.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * Created by Agostino on 22/02/2018.
 */

public class Movie implements Parcelable {

    private String id;
    private String rating;
    private String title;
    private String poster;
    private String originalTitle;
    private String backdrop;
    private String plotSynopsis;
    private String releaseDate;

    private Movie(Parcel in) {
        id = in.readString();
        rating = in.readString();
        title = in.readString();
        poster = in.readString();
        originalTitle = in.readString();
        backdrop = in.readString();
        plotSynopsis = in.readString();
        releaseDate = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public Movie() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getBackdrop() {
        return backdrop;
    }

    public void setBackdrop(String backdrop) {
        this.backdrop = backdrop;
    }

    @Override //for debug purpose
    public String toString() {
        return "Movie{" +
                "id='" + title + '\'' +
                ", rating='" + rating + '\'' +
                ", title='" + title + '\'' +
                ", poster='" + poster + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", backdrop='" + backdrop + '\'' +
                ", plotSynopsis='" + plotSynopsis + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(rating);
        dest.writeString(title);
        dest.writeString(poster);
        dest.writeString(originalTitle);
        dest.writeString(backdrop);
        dest.writeString(plotSynopsis);
        dest.writeString(releaseDate);
    }
}
