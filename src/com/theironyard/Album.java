package com.theironyard;

/**
 * Created by ryankielty on 1/21/17.
 */
public class Album {
    int id;
    int userId;
    String title;
    String artist;
    int releaseYear;

    public Album(int id, int userId, String title, String artist, int releaseYear) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.artist = artist;
        this.releaseYear = releaseYear;
    }

    public Album(int userId, String title, String artist, int releaseYear) {
        this.userId = userId;
        this.title = title;
        this.artist = artist;
        this.releaseYear = releaseYear;
    }

    public Album(String title, String artist, int releaseYear) {
        this.title = title;
        this.artist = artist;
        this.releaseYear = releaseYear;
    }

    public Album() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String albumTitle) {
        this.title = albumTitle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

}
