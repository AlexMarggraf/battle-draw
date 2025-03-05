import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class themeGetter {
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
    public static void clearList(List<String> list){
        list.clear();
        System.out.println("List cleared");
    }

    public static void main(String[] args) {
        List<String> selectedTopics = getRandomWords(30);
        System.out.println("Selected Topics: " + selectedTopics);
        clearList(selectedTopics);
        System.out.println("Selected Topics cleared: " + selectedTopics);

    }
}
