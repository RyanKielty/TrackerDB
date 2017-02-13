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

    static HashMap<String, User> usersMap = new HashMap<>();

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

                    User user = usersMap.get(userName);

                    HashMap data = new HashMap();

                    if(userName == null) {
                        return new ModelAndView(data, "login.html");
                    } else {
                        data.put("userName", userName);
                        data.put("albums", user.albumsList);
                        return new ModelAndView(data, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String userName = request.queryParams("loginName");
                    if (userName == null || userName.isEmpty()) {
                        throw new Exception("Login name not found.");
                    }
                    User user = usersMap.get(userName);
                    if (user == null) {
                        user = new User(userName);
                        usersMap.put(userName, user);
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
                    User user = usersMap.get(userName);

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

                    Album addAlbum = new Album(title, artist, releaseYear);
                    user.albumsList.add(addAlbum);

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/remove-album",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("userName");
                    User user = usersMap.get(userName);

                    String removeAlbum = request.queryParams("removeAlbum");

                    user.albumsList.remove(Integer.parseInt(removeAlbum)-1);

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/edit-album",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("userName");
                    User user = usersMap.get(userName);

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

                    user.albumsList.remove(Integer.parseInt(editAlbum)-1);
                    user.albumsList.add(Integer.parseInt(editAlbum)-1, new Album(title, artist, releaseYear));

                    response.redirect("/");
                    return "";
                })
        );

    }

    private static void createTables(Connection connect) throws SQLException {
        Statement statement = connect.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
        statement.execute("CREATE TABLE IF NOT EXISTS messages (id IDENTITY, album_title VARCHAR, album_artist VARCHAR, release_year INT)");
    }

    public static void insertUser(Connection connect, String name, String password) throws SQLException {
        PreparedStatement statement = connect.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        statement.setString(1, name);
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
            return new User(id, userName, password);
        }
        return null;
    }

    public static void insertAlbum(Connection connect, int id, String title, String artist, int releaseYear) throws SQLException {
        PreparedStatement statement = connect.prepareStatement("INSERT INTO albums VALUES (Null, ?, ?, ?, ?");
        statement.setInt(1, id);
        statement.setString(2, title);
        statement.setString(3, artist);
        statement.setInt(4, releaseYear);
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


    public static ArrayList<Album> selectAlbums(Connection connect, int id) throws SQLException {
        ArrayList<Album> albums = new ArrayList<>();
        PreparedStatement statement = connect.prepareStatement("SELECT * FROM albums INNER JOIN users ON album.user_id = users.id WHERE albums.id = ?");
        statement.setInt(1, id);
        ResultSet results = statement.executeQuery();
        while (results.next()) {
            int albumsId = results.getInt("albums.id");
            String title = results.getString("albums.title");
            String artist = results.getString("albums.artist");
            int releaseYear = results.getInt("albums.releaseYear");
            Album album = new Album(albumsId, title, artist, releaseYear);
            albums.add(album);
        }
        return albums;
    }

    public static void updateAlbum() throws SQLException {

    }

    public static void deleteAlbum(Connection connect, int id) throws SQLException {
        PreparedStatement statement = connect.prepareStatement("DELETE FROM albums WHERE id = ?");
        statement.setInt(1, id);
        statement.execute();
    }

}
