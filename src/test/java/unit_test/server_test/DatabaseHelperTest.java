package unit_test.server_test;

import org.junit.jupiter.api.*;
import server.DatabaseHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link DatabaseHelper} class.
 * These tests validate database operations such as:
 * - Player account creation and authentication
 * - Username/password updates
 * - Online/offline status tracking
 * - Thema (topic) retrieval
 * - Utility methods like list clearing
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseHelperTest {

    /**
     * Ensures that the test user exists before each test.
     *
     * @throws Exception if database interaction fails
     */
    @BeforeEach
    void ensureTestUserExists() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database/ThemaTest.db");
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT OR IGNORE INTO Players (username, password, online) VALUES ('testuser', 'pass123', 0)");
        }
    }

    /**
     * Initializes the test database before all tests by:
     * - Dropping existing tables
     * - Creating schema
     * - Inserting default test records
     *
     * @throws Exception if database interaction fails
     */
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

            conn.setAutoCommit(true);

            ResultSet rs = conn.createStatement().executeQuery("SELECT id, username FROM Players");
            while (rs.next()) {
                System.out.println("Player: " + rs.getInt("id") + " - " + rs.getString("username"));
            }
        }
    }

    /**
     * Tests whether {@link DatabaseHelper#getRandomWords(int)} returns the requested number of words.
     */
    @Test
    void testGetRandomWords() {
        List<String> words = DatabaseHelper.getRandomWords(2);
        assertNotNull(words);
        assertEquals(2, words.size());
    }

    /**
     * Tests retrieving a player’s ID and name from the database using {@code getPlayerId} and {@code getPlayerName}.
     */
    @Test
    void testGetPlayerIdAndName() {
        int id = DatabaseHelper.getPlayerId("testuser");
        assertTrue(id > 0, "ID should be greater than 0");

        String name = DatabaseHelper.getPlayerName(id);
        assertEquals("testuser", name);
    }

    /**
     * Tests setting a user's online status and retrieving it.
     */
    @Test
    void testSetAndGetStatus() {
        int id = DatabaseHelper.getPlayerId("testuser");
        DatabaseHelper.setStatus(id, 1);
        assertTrue(DatabaseHelper.getStatus(id));

        DatabaseHelper.setStatus(id, 0);
        assertFalse(DatabaseHelper.getStatus(id));
    }

    /**
     * Tests credential validation using correct and incorrect passwords.
     */
    @Test
    void testCheckCredentials() {
        assertTrue(DatabaseHelper.checkCredentials("testuser", "pass123"));
        assertFalse(DatabaseHelper.checkCredentials("testuser", "wrongpass"));
    }

    /**
     * Tests inserting a new player and checking if the username becomes unavailable afterward.
     */
    @Test
    void testInsertPlayerAndCheckUsername() {
        String username = "newUserTest";
        assertTrue(DatabaseHelper.checkUsername(username));

        DatabaseHelper.insertPlayer(username, "secret");
        assertFalse(DatabaseHelper.checkUsername(username));

        int id = DatabaseHelper.getPlayerId(username);
        assertTrue(id > 0);
    }

    /**
     * Tests updating a player’s username and verifying the change.
     */
    @Test
    void testChangeUsername() {
        int id = DatabaseHelper.getPlayerId("testuser");
        String newUsername = "updatedUser";

        DatabaseHelper.changeUsername(newUsername, id);
        String updatedName = DatabaseHelper.getPlayerName(id);
        assertEquals(newUsername, updatedName);
    }

    /**
     * Tests updating a player’s password and validating login using the new credentials.
     */
    @Test
    void testChangePassword() {
        int id = DatabaseHelper.getPlayerId("updatedUser");
        DatabaseHelper.changePassword("newPass123", id);

        assertTrue(DatabaseHelper.checkCredentials("updatedUser", "newPass123"));
    }

    /**
     * Tests setting all users offline and ensures the test user is also set offline.
     */
    @Test
    void testSetOffline() {
        DatabaseHelper.setOffline();
        int id = DatabaseHelper.getPlayerId("updatedUser");
        assertFalse(DatabaseHelper.getStatus(id));
    }

    /**
     * Tests that {@link DatabaseHelper#clearList(List)} empties a list.
     */
    @Test
    void testClearList() {
        List<String> list = new java.util.ArrayList<>();
        list.add("X");
        list.add("Y");

        DatabaseHelper.clearList(list);
        assertTrue(list.isEmpty());
    }
}
