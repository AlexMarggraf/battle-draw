package unit_test.server_test;

import org.junit.jupiter.api.*;
import server.DatabaseHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseHelperTest {

    @BeforeEach
    void ensureTestUserExists() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database/ThemaTest.db");
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT OR IGNORE INTO Players (username, password, online) VALUES ('testuser', 'pass123', 0)");
        }
    }

    @BeforeAll
    void setupTestDatabase() throws Exception {
        DatabaseHelper.setDatabasePath("jdbc:sqlite:database/ThemaTest.db");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database/ThemaTest.db");
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS Players");
            stmt.execute("DROP TABLE IF EXISTS Thema");

            stmt.execute("""
                CREATE TABLE Players (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE,
                    password TEXT,
                    online INTEGER
                )
            """);

            stmt.execute("""
                CREATE TABLE Thema (
                    thema TEXT
                )
            """);

            stmt.execute("INSERT INTO Players (username, password, online) VALUES ('testuser', 'pass123', 0)");
            stmt.execute("INSERT INTO Thema (thema) VALUES ('Tree'), ('Mountain'), ('Sky')");

            conn.setAutoCommit(true); // just to be safe

            ResultSet rs = conn.createStatement().executeQuery("SELECT id, username FROM Players");
            while (rs.next()) {
                System.out.println("Player: " + rs.getInt("id") + " - " + rs.getString("username"));
            }
        }
    }

    @Test
    void testGetRandomWords() {
        List<String> words = DatabaseHelper.getRandomWords(2);
        assertNotNull(words);
        assertEquals(2, words.size());
    }

    @Test
    void testGetPlayerIdAndName() {
        int id = DatabaseHelper.getPlayerId("testuser");
        if (id <= 0) {
            System.out.println("No user found for 'testuser'.");
        }
        assertTrue(id > 0);


        String name = DatabaseHelper.getPlayerName(id);
        assertEquals("testuser", name);
    }

    @Test
    void testSetAndGetStatus() {
        int id = DatabaseHelper.getPlayerId("testuser");
        DatabaseHelper.setStatus(id, 1);
        assertTrue(DatabaseHelper.getStatus(id));

        DatabaseHelper.setStatus(id, 0);
        assertFalse(DatabaseHelper.getStatus(id));
    }

    @Test
    void testCheckCredentials() {
        assertTrue(DatabaseHelper.checkCredentials("testuser", "pass123"));
        assertFalse(DatabaseHelper.checkCredentials("testuser", "wrongpass"));
    }

    @Test
    void testInsertPlayerAndCheckUsername() {
        String username = "newUserTest";
        assertTrue(DatabaseHelper.checkUsername(username)); // Available

        DatabaseHelper.insertPlayer(username, "secret");
        assertFalse(DatabaseHelper.checkUsername(username)); // Now taken

        int id = DatabaseHelper.getPlayerId(username);
        assertTrue(id > 0);
    }

    @Test
    void testChangeUsername() {
        int id = DatabaseHelper.getPlayerId("testuser");
        String newUsername = "updatedUser";

        DatabaseHelper.changeUsername(newUsername, id);
        String updatedName = DatabaseHelper.getPlayerName(id);
        assertEquals(newUsername, updatedName);
    }

    @Test
    void testChangePassword() {
        int id = DatabaseHelper.getPlayerId("updatedUser");
        DatabaseHelper.changePassword("newPass123", id);

        assertTrue(DatabaseHelper.checkCredentials("updatedUser", "newPass123"));
    }

    @Test
    void testSetOffline() {
        DatabaseHelper.setOffline();
        int id = DatabaseHelper.getPlayerId("updatedUser");
        assertFalse(DatabaseHelper.getStatus(id));
    }

    @Test
    void testClearList() {
        List<String> list = new java.util.ArrayList<>();
        list.add("X");
        list.add("Y");

        DatabaseHelper.clearList(list);
        assertTrue(list.isEmpty());
    }
}
