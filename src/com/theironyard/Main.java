package com.theironyard;


import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

//    static HashMap<String, User> usersMap = new HashMap<>();

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection connect = DriverManager.getConnection("jdbc:h2:./main");
        createTables(connect);

        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("userName");
                    String password = session.attribute("password");

                    User user = selectUser(connect, userName);

                    HashMap data = new HashMap();

                    if(userName == null ) {
                        return new ModelAndView(data, "login.html");
                    } else {
                        data.put("userName", userName);
                        data.put("password", password);
                        data.put("albums", user.albumsList);
                        return new ModelAndView(data, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String userName = request.queryParams("userName");
                    String password = request.queryParams("password");
                    if (userName == null || userName.isEmpty()) {
                        throw new Exception("Login name not found.");
                    }
                    if (password.isEmpty()) {
                        throw new Exception("Password required");
                    }
                    User user = selectUser(connect, userName);
                    if (user == null) {
                        insertUser(connect, userName, password);
                    }
                    Session session = request.session();
                    session.attribute("userName", userName);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/add-album",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("userName");


                    if (userName == null) {
                        throw new Exception("Shame on you, login!");
                    }

                    String title = request.queryParams("title");
                    if (title == null) {
                        throw new Exception("Title required for tracking");
                    }
                    String artist = request.queryParams("artist");
                    if (artist == null) {
                        throw new Exception("Artist required for tracking");
                    }
                    int releaseYear = Integer.parseInt(request.queryParams("releaseYear"));
                    if (releaseYear < 1930 && releaseYear > 2017) {
                        throw new Exception("Invalid release year");
                    }

                    User user = selectUser(connect, userName);
                    insertAlbum(connect, user.id, title, artist, releaseYear);

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/remove-album",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("userName");
                    User user = selectUser(connect, userName);

                    String removeAlbum = request.queryParams("removeAlbum");

                    deleteAlbum(connect, Integer.parseInt(removeAlbum));

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/edit-album",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("userName");
                    User user = selectUser(connect, userName);

                    String title = request.queryParams("title");
                    if (title == null) {
                        throw new Exception("Title required for tracking");
                    }
                    String artist = request.queryParams("artist");
                    if (artist == null) {
                        throw new Exception("Artist required for tracking");
                    }
                    int releaseYear = Integer.parseInt(request.queryParams("releaseYear"));
                    if (releaseYear < 1930 && releaseYear > 2017) {
                        throw new Exception("Invalid release year");
                    }

                    String editAlbum = request.queryParams("editAlbum");

                    updateAlbum(connect, Integer.parseInt(editAlbum), title, artist, releaseYear);

                    response.redirect("/");
                    return "";
                })
        );

    }



    public static void createTables(Connection connect) throws SQLException {
        Statement statement = connect.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
        statement.execute("CREATE TABLE IF NOT EXISTS albums (id IDENTITY, user_id INT, album_title VARCHAR, album_artist VARCHAR, release_year INT)");
    }

    public static void insertUser(Connection connect, String userName, String password) throws SQLException {
        PreparedStatement statement = connect.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        statement.setString(1, userName);
        statement.setString(2, password);
        statement.execute();
    }

    public static User selectUser(Connection connect, String userName) throws SQLException {
        PreparedStatement stmt = connect.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, userName);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            String password = results.getString("password");
            ArrayList<Album> albums = albums(connect, id);

            return new User(id, userName, password, albums);
        }
        return null;
    }

    public static void insertAlbum(Connection connect, int id, String title, String artist, int releaseYear) throws SQLException {
        PreparedStatement statement = connect.prepareStatement("INSERT INTO albums VALUES (Null, ?, ?, ?, ?)");
        statement.setInt(1, id);
        statement.setString(2, title);
        statement.setString(3, artist);
        statement.setInt(4, releaseYear);
        statement.execute();
    }

    public static Album selectAlbum(Connection connect, int id) throws SQLException {
        PreparedStatement stmt = connect.prepareStatement("SELECT * FROM albums INNER JOIN users ON albums.user_id = users.id WHERE albums.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int albumsId = results.getInt("albums.id");
            String title = results.getString("albums.title");
            String artist = results.getString("albums.artist");
            int releaseYear = results.getInt("albums.releaseYear");
            return new Album(albumsId, title, artist, releaseYear);
        }
        return null;
    }

//
    public static ArrayList<Album> albums(Connection connect, int id) throws SQLException {
        ArrayList<Album> albums = new ArrayList<>();
        PreparedStatement statement = connect.prepareStatement("SELECT * FROM albums INNER JOIN users ON albums.user_id = users.id WHERE users.id = ?");
        statement.setInt(1, id);
        ResultSet results = statement.executeQuery();
        while (results.next()) {
            int albumsId = results.getInt("albums.id");
            int userId = results.getInt("albums.user_id");
            String title = results.getString("albums.album_title");
            String artist = results.getString("albums.album_artist");
            int releaseYear = results.getInt("albums.release_year");
            Album album = new Album(albumsId, userId, title, artist, releaseYear);
            albums.add(album);
        }
        return albums;
    }

    public static void updateAlbum(Connection connect, int id, String title, String artist, int releaseYear) throws SQLException {
        PreparedStatement statement = connect.prepareStatement("UPDATE ALBUMS SET album_artist = ?, album_title = ?, release_year = ? WHERE id = ?");
        statement.setString(1, artist);
        statement.setString(2, title);
        statement.setInt(3, releaseYear);
        statement.setInt(4, id);

        statement.execute();

    }

    public static void deleteAlbum(Connection connect, int id) throws SQLException {
        PreparedStatement statement = connect.prepareStatement("DELETE FROM albums WHERE id = ?");
        statement.setInt(1, id);
        statement.execute();
    }

}
