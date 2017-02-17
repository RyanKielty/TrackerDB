package test;

import com.theironyard.Album;
import com.theironyard.Main;
import com.theironyard.User;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by ryankielty on 2/17/17.
 */
public class MainTest {
    public Connection startConnection() throws SQLException {
        Connection connect = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(connect);
        return connect;
    }

    @Test
    public void testUser() throws SQLException {
        Connection connect = startConnection();
        Main.insertUser(connect, "Ryan", "ryan");
        User user = Main.selectUser(connect, "Ryan");
        connect.close();
        assertTrue(user != null);
    }

    @Test
    public void testAlbum() throws SQLException {
        Connection connect = startConnection();
        Main.insertUser(connect, "Ryan", "ryan");
        User user = Main.selectUser(connect, "Ryan");
        Main.insertAlbum(connect, user.getId(), "D.W.T.W.", "Ab Soul", 2016);
        Album selectAlbum = Main.selectAlbum(connect, 1);
    }

}
