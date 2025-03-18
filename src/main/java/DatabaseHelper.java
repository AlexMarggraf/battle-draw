import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String PATH = "jdbc:sqlite:resources/Thema.db";

    public static List<String> getRandomWords(int amount) {
        String query = "SELECT thema FROM Thema ORDER BY RANDOM() LIMIT " + amount;
        List<String> themaList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(PATH);
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
    public static int getPlayerId(String username) {
        String query = "SELECT id FROM players WHERE username = ?";
        int id = -1; // Default to -1 if user is not found

        try (Connection conn = DriverManager.getConnection(PATH);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id"); // Retrieve the player ID
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return id; // Returns -1 if the player is not found
    }
    public static String getPlayerName(int id) {
        String query = "SELECT username FROM players WHERE id = ?";
        String username = null;

        try (Connection conn = DriverManager.getConnection(PATH);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username"); // Retrieve the player ID
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return username;
    }

    public static void setStatus (int id, int status) {
        String update = "Update Players set online = " + status + " where id = " + id;

        try (Connection conn = DriverManager.getConnection(PATH)) {
            PreparedStatement pstmt = conn.prepareStatement(update);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public static Boolean getStatus (int id) {
        String query = "Select online from Players where id = " + id;

        try (Connection con = DriverManager.getConnection(PATH)) {
            PreparedStatement pstmt = con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt("online") == 1;
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return false;
    }

    public static Boolean checkCredentials(String username, String password) {
        String query = "SELECT id FROM Players WHERE username = ? AND password = ?";
        int userId = -1;

        try (Connection conn = DriverManager.getConnection(PATH);
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

    public static void insertPlayer(String username, String password) {
        String query = "INSERT INTO Players (username, password) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(PATH);
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

    public static Boolean checkUsername(String username) {
        String query = "SELECT EXISTS(SELECT 1 FROM Players WHERE username = ?)";

        try (Connection conn = DriverManager.getConnection(PATH);
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

    public static void changeUsername(String username, int id) {
        String update = "UPDATE Players SET username = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(PATH);
            PreparedStatement pstmt = conn.prepareStatement(update)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public static void changePassword(String password, int id) {
        String update = "UPDATE Players SET password = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(PATH);
            PreparedStatement pstmt = conn.prepareStatement(update)) {

            pstmt.setString(1, password);
            pstmt.setInt(2, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public static void setOffline() {
        String update = "UPDATE Players SET online = 0";

        try(Connection conn = DriverManager.getConnection(PATH);
        Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(update);
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public static void clearList(List<String> list){
        list.clear();
        System.out.println("List cleared");
    }

    public static void main(String[] args) {
        List<String> selectedTopics = getRandomWords(3);
        System.out.println("Selected Topics: " + selectedTopics);
        //clearList(selectedTopics);
        //System.out.println("Selected Topics:" selectedTopics);
        int id = getPlayerId("dave");
        System.out.println("" + id);
        String username = getPlayerName(4);
        System.out.println(username);
        boolean userid = checkCredentials("dave", "letmein");
        System.out.println(userid);
        insertPlayer("globi", "sfhufhs");
    }
}
