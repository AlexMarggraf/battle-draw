package server;

import game.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles database operations for player management and theme word retrieval.
 * Uses SQLite database located at resources/Thema.db.
 */
public class DatabaseHelper {
    private static String dbPath = "jdbc:sqlite:database/Thema.db";
    private static final Logger LOGGER = LogManager.getLogger(DatabaseHelper.class);

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbPath);
    }

    public static void setDatabasePath(String path) {
        dbPath = path;
    }

    /**
     * Fetches random theme words from the database.
     *
     * @param amount Number of random words to retrieve
     * @return List of theme words (might be empty if something goes wrong)
     */
    public static List<String> getRandomWords(int amount) {
        String query = "SELECT thema FROM Thema ORDER BY RANDOM() LIMIT " + amount;
        List<String> themaList = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                themaList.add(rs.getString("thema"));
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return themaList;
    }

    /**
     * Looks up a player's database ID by their username.
     *
     * @param username Name of the player to find
     * @return Player ID if found, -1 if not found or error occurs
     */
    public static int getPlayerId(String username) {
        String query = "SELECT id FROM players WHERE username = ?";
        int id = -1;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id");
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return id;
    }

    /**
     * Gets the username associated with a playerID.
     *
     * @param id Player ID to look up
     * @return Username if found, null if not found
     */
    public static String getPlayerName(int id) {
        String query = "SELECT username FROM players WHERE id = ?";
        String username = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username");
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return username;
    }

    /**
     * Updates a player's online status.
     *
     * @param id     Player ID to update
     * @param status New status (0 for offline, 1 for online)
     */
    public static void setStatus(int id, int status) {
        String update = "UPDATE Players SET online = " + status + " WHERE id = " + id;

        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(update);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    /**
     * Checks if a player is currently marked as online.
     *
     * @param id Player ID to check
     * @return True if player is online, false if offline or error occurs
     */
    public static Boolean getStatus(int id) {
        String query = "SELECT online FROM Players WHERE id = " + id;

        try (Connection con = getConnection()) {
            PreparedStatement pstmt = con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt("online") == 1;
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return false;
    }

    /**
     * Validates player login credentials.
     *
     * @param username Username to check
     * @param password Password to verify
     * @return True if credentials match, false otherwise
     */
    public static Boolean checkCredentials(String username, String password) {
        String query = "SELECT id FROM Players WHERE username = ? AND password = ?";
        int userId = -1;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("id");
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        return userId != -1;
    }

    /**
     * Creates a new player account in the database.
     *
     * @param username New player's username
     * @param password New player's password
     */
    public static void insertPlayer(String username, String password) {
        String query = "INSERT INTO Players (username, password) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        System.out.println("Inserted user with ID: " + userId);
                    }
                }
            } else {
                System.out.println("No rows affected. Insert failed.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    /**
     * Checks if a username is available for registration.
     *
     * @param username Username to check
     * @return True if username is available, false if already taken
     */
    public static Boolean checkUsername(String username) {
        String query = "SELECT EXISTS(SELECT 1 FROM Players WHERE username = ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return !rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return false;
    }

    /**
     * Changes a player's username.
     *
     * @param username New username to set
     * @param id       Player ID to update
     */
    public static void changeUsername(String username, int id) {
        String update = "UPDATE Players SET username = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(update)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    /**
     * Updates a player's password.
     *
     * @param password New password to set
     * @param id       Player ID to update
     */
    public static void changePassword(String password, int id) {
        String update = "UPDATE Players SET password = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(update)) {

            pstmt.setString(1, password);
            pstmt.setInt(2, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    /**
     * Marks all players as offline in the database.
     * Useful for resetting statuses on server startup/shutdown.
     */
    public static void setOffline() {
        String update = "UPDATE Players SET online = 0";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(update);
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    /**
     * Clears a list and logs the action.
     *
     * @param list List to clear
     */
    public static void clearList(List<String> list) {
        list.clear();
        System.out.println("List cleared");
    }

    public static void setHighscore(int id, double score) {
        String update = "UPDATE Players SET score = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(update)) {

            pstmt.setDouble(1, score);
            pstmt.setInt(2, id);


            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public static Double getHighscore(int id) {
        String query = "SELECT score FROM Players WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("score");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return null;
    }

    /**
     * This method checks whether the database file exist and will create a new one if not.
     */
    public static void createDB() {
        File dbFile = new File("database/Thema.db");

        if (!dbFile.exists()) {
            String url = "jdbc:sqlite:database/Thema.db";

            File directory = new File("database");
            boolean createdDir = directory.mkdir();

            if (!createdDir) {
                LOGGER.error("Couldn't create directory");
            }

            try (Connection conn = DriverManager.getConnection(url)) {
                if (conn != null) {
                    LOGGER.info("Database was created");

                    String createTablePlayers = "CREATE TABLE IF NOT EXISTS Players ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "username TEXT NOT NULL,"
                            + "password TEXT NOT NULL,"
                            + "online INTEGER NOT NULL DEFAULT 0,"
                            + "score INTEGER DEFAULT 0);";

                    String createTableThema = "CREATE TABLE IF NOT EXISTS Thema ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "thema TEXT NOT NULL);";

                    String insertThemes = "INSERT INTO THEMA (thema) VALUES (?)";


                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(createTablePlayers);
                        stmt.execute(createTableThema);
                        LOGGER.info("Tables created");

                        try {
                            InputStream is = DatabaseHelper.class.getClassLoader().getResourceAsStream("themes.txt");

                            String themes = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                                    .lines()
                                    .collect(Collectors.joining("\n"));

                            String[] themesArray = Arrays.stream(themes.split(",")).map(String::trim).toArray(String[]::new);

                            PreparedStatement pstmt = conn.prepareStatement(insertThemes);

                            for (String theme : themesArray) {
                                pstmt.setString(1, theme);
                                pstmt.addBatch();
                            }

                            pstmt.executeBatch();
                        } catch (SQLException e) {
                            LOGGER.error("Couldn't read themes.txt");
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("Database creation failed: {}", e.getMessage());
            }
        } else {
            LOGGER.info("Database already exists");
        }
    }
}