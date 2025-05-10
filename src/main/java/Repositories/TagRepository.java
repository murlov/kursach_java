package Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TagRepository {
    public List<String> getAllTags() throws SQLException {
        List<String> tags = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT tag_name FROM tags";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tags.add(rs.getString("tag_name"));
            }
        }
        return tags;
    }
}