package Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserStorageRepository {
    public void initializeStorage(int userId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO user_storage (user_id, used_space, max_space) VALUES (?, 0, 10737418240)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public long getAvailableSpace(int userId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT (max_space - used_space) / (1024 * 1024 * 1024) AS available_gb FROM user_storage WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("available_gb");
            }
        }
        return 0;
    }

    public void updateUsedSpace(int userId, long sizeChange) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE user_storage SET used_space = used_space + ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sizeChange);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
}