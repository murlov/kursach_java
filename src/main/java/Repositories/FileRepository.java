package Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FileRepository {
    public void saveFile(int userId, String fileName, String filePath, long fileSize) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO files (user_id, file_name, file_path, file_size) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fileName);
            stmt.setString(3, filePath);
            stmt.setLong(4, fileSize);
            stmt.executeUpdate();
        }
    }

    public List<String> getFileNamesByUserId(int userId) throws SQLException {
        List<String> fileNames = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT file_name FROM files WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                fileNames.add(rs.getString("file_name"));
            }
        }
        return fileNames;
    }

    public String getFilePath(int userId, String fileName) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT file_path FROM files WHERE user_id = ? AND file_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fileName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("file_path");
            }
        }
        return null;
    }

    public long getFileSize(int userId, String fileName) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT file_size FROM files WHERE user_id = ? AND file_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fileName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("file_size");
            }
        }
        return 0;
    }

    public boolean deleteFile(int userId, String fileName) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "DELETE FROM files WHERE user_id = ? AND file_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fileName);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean renameFile(int userId, String oldName, String newName, String newPath) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE files SET file_name = ?, file_path = ? WHERE user_id = ? AND file_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, newPath);
            stmt.setInt(3, userId);
            stmt.setString(4, oldName);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean addTagToFile(int userId, String fileName, String tagName) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        // Найти или создать тег
        int tagId;
        String sqlTag = "INSERT IGNORE INTO tags (tag_name) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlTag, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tagName);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                tagId = rs.getInt(1);
            } else {
                // Тег уже существует, найдем его
                String sqlFindTag = "SELECT tag_id FROM tags WHERE tag_name = ?";
                try (PreparedStatement stmtFind = conn.prepareStatement(sqlFindTag)) {
                    stmtFind.setString(1, tagName);
                    rs = stmtFind.executeQuery();
                    rs.next();
                    tagId = rs.getInt("tag_id");
                }
            }
        }

        // Найти file_id
        int fileId;
        String sqlFile = "SELECT file_id FROM files WHERE user_id = ? AND file_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlFile)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fileName);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return false;
            }
            fileId = rs.getInt("file_id");
        }

        // Добавить связь
        String sqlLink = "INSERT IGNORE INTO file_tags (file_id, tag_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlLink)) {
            stmt.setInt(1, fileId);
            stmt.setInt(2, tagId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<String> getFileTags(int userId, String fileName) throws SQLException {
        List<String> tags = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT t.tag_name FROM tags t " +
                "JOIN file_tags ft ON t.tag_id = ft.tag_id " +
                "JOIN files f ON ft.file_id = f.file_id " +
                "WHERE f.user_id = ? AND f.file_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fileName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tags.add(rs.getString("tag_name"));
            }
        }
        return tags;
    }
}