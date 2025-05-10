package Helpers;

import Servlets.Register;
import Servlets.UploadFile;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

public class User extends HttpServlet {

    private File file;
    public String directory = null;
    private String mEmail;
    private String mName;
    private String filePath = "D:\\Programming\\java\\StorageSpace";

    public boolean registerUser(String name, String email, String password) throws SQLException {
        Statement stmt = Register.getConnection().createStatement();
        String sql = "INSERT INTO users(name, email, password) VALUES ('" + name + "','" + email + "','" + password + "')";
        stmt.executeUpdate(sql);
        // Инициализируем запись в user_storage
        String initStorage = "INSERT INTO user_storage(user_email, used_space, quota) VALUES (?, 0, 10737418240)";
        PreparedStatement storageStmt = Register.getConnection().prepareStatement(initStorage);
        storageStmt.setString(1, email);
        storageStmt.executeUpdate();
        storageStmt.close();
        stmt.close();
        return true;
    }

    public boolean makeDir(String email) {
        try {
            File file = new File(filePath + email);
            if (file.mkdir()) {
                directory = filePath + email;
                System.out.println(directory);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public String getSize(String file) {
        String sql = "SELECT size FROM files WHERE user_email = ? AND file_name = ? AND status = 'active'";
        try {
            PreparedStatement ps = Register.getConnection().prepareStatement(sql);
            ps.setString(1, mEmail);
            ps.setString(2, file);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long sizeInBytes = rs.getLong("size");
                rs.close();
                ps.close();
                return FileUtils.byteCountToDisplaySize(sizeInBytes);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
        return "0 B";
    }

    public long getSpace() {
        String sql = "SELECT used_space FROM user_storage WHERE user_email = ?";
        try {
            PreparedStatement ps = Register.getConnection().prepareStatement(sql);
            ps.setString(1, mEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long bytes = rs.getLong("used_space");
                long mb = bytes / (1024 * 1024);
                long gb = mb / 1024;
                rs.close();
                ps.close();
                return gb;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
        return 0;
    }

    public String handleFile(HttpServletRequest request) {

        long currentSpace = getSpace();
        if (currentSpace < 10) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(UploadFile.maxMemSize);
            factory.setRepository(new File("D:\\Programming\\java\\StorageSpace\\Temp"));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(UploadFile.maxFileSize);

            try {
                List fileItems = upload.parseRequest(request);
                Iterator i = fileItems.iterator();

                while (i.hasNext()) {
                    FileItem fi = (FileItem) i.next();
                    if (!fi.isFormField()) {
                        if (fi.getSize() == 0 || fi.getName() == null || fi.getName().isEmpty()) {
                            return "Please select a file.";
                        }

                        String fileName = fi.getName();
                        String contentType = fi.getContentType();
                        boolean isInMemory = fi.isInMemory();
                        long sizeInBytes = fi.getSize();

                        if (fileName.lastIndexOf("\\") >= 0) {
                            file = new File(directory + "/" + fileName.substring(fileName.lastIndexOf("\\")));
                        } else {
                            file = new File(directory + "/" + fileName.substring(fileName.lastIndexOf("\\") + 1));
                        }
                        fi.write(file);
                        System.out.println("Uploaded Filename: " + fileName);

                        // Сохраняем информацию о файле в таблицу files
                        String sql = "INSERT INTO files (user_email, file_name, file_path, size) VALUES (?, ?, ?, ?)";
                        PreparedStatement ps = Register.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, mEmail);
                        ps.setString(2, fileName);
                        ps.setString(3, file.getAbsolutePath());
                        ps.setLong(4, sizeInBytes);
                        ps.executeUpdate();

                        ResultSet rs = ps.getGeneratedKeys();
                        int fileId = 0;
                        if (rs.next()) {
                            fileId = rs.getInt(1);
                        }
                        rs.close();
                        ps.close();

                        // Обновляем used_space в user_storage
                        String updateSpace = "UPDATE user_storage SET used_space = used_space + ?, last_updated = NOW() WHERE user_email = ?";
                        PreparedStatement spaceStmt = Register.getConnection().prepareStatement(updateSpace);
                        spaceStmt.setLong(1, sizeInBytes);
                        spaceStmt.setString(2, mEmail);
                        spaceStmt.executeUpdate();
                        spaceStmt.close();
                    }
                }
                return "success";
            } catch (FileExistsException e) {
                return "That File Already Exists!";
            } catch (Exception ex) {
                System.out.println(ex);
                return "An Unexpected Error Occurred! - " + ex;
            }
        } else {
            return "Your 10 GB Quota has been exceeded!";
        }
    }

    public String[] getContent(String dir) {
        String sql = "SELECT file_name FROM files WHERE user_email = ? AND status = 'active'";
        try {
            PreparedStatement ps = Register.getConnection().prepareStatement(sql);
            ps.setString(1, mEmail);
            ResultSet rs = ps.executeQuery();
            List<String> fileList = new ArrayList<>();
            while (rs.next()) {
                fileList.add(rs.getString("file_name"));
            }
            rs.close();
            ps.close();
            return fileList.toArray(new String[0]);
        } catch (SQLException e) {
            System.out.println(e);
            return new String[0];
        }
    }

    public boolean validateEmail(String email) throws SQLException {
        PreparedStatement preparedStatement = Register.getConnection().prepareStatement("SELECT * FROM users WHERE email = ?");
        preparedStatement.setString(1, email);
        System.out.println(preparedStatement);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            return false;
        }
        preparedStatement.close();
        return true;
    }

    public boolean validate(String email, String password) throws SQLException {
        PreparedStatement preparedStatement = Servlets.Login.getConnection().prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?");
        preparedStatement.setString(1, email);
        preparedStatement.setString(2, password);
        System.out.println(preparedStatement);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            mName = rs.getString("name");
            mEmail = rs.getString("email");
            String rsPassword = rs.getString("password");
            directory = filePath + email;
            return true;
        }
        preparedStatement.close();
        return false;
    }

    public String getFileLocation(String name) {
        String sql = "SELECT file_path FROM files WHERE user_email = ? AND file_name = ? AND status = 'active'";
        try {
            PreparedStatement ps = Register.getConnection().prepareStatement(sql);
            ps.setString(1, mEmail);
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String path = rs.getString("file_path");
                rs.close();
                ps.close();
                return path;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
        return "";
    }

    public boolean delete(String name) {
        String sql = "SELECT id, size FROM files WHERE user_email = ? AND file_name = ? AND status = 'active'";
        try {
            PreparedStatement ps = Register.getConnection().prepareStatement(sql);
            ps.setString(1, mEmail);
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int fileId = rs.getInt("id");
                long size = rs.getLong("size");

                // Удаляем связи с тегами
                String deleteTags = "DELETE FROM file_tags WHERE file_id = ?";
                PreparedStatement tagStmt = Register.getConnection().prepareStatement(deleteTags);
                tagStmt.setInt(1, fileId);
                tagStmt.executeUpdate();
                tagStmt.close();

                // Обновляем статус файла
                String updateSql = "UPDATE files SET status = 'deleted' WHERE id = ?";
                PreparedStatement updateStmt = Register.getConnection().prepareStatement(updateSql);
                updateStmt.setInt(1, fileId);
                updateStmt.executeUpdate();
                updateStmt.close();

                // Обновляем used_space
                String updateSpace = "UPDATE user_storage SET used_space = used_space - ?, last_updated = NOW() WHERE user_email = ?";
                PreparedStatement spaceStmt = Register.getConnection().prepareStatement(updateSpace);
                spaceStmt.setLong(1, size);
                spaceStmt.setString(2, mEmail);
                spaceStmt.executeUpdate();
                spaceStmt.close();

                rs.close();
                ps.close();
                return true;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    public boolean rename(String file, String nname) throws IOException {
        String sql = "SELECT id FROM files WHERE user_email = ? AND file_name = ? AND status = 'active'";
        try {
            PreparedStatement ps = Register.getConnection().prepareStatement(sql);
            ps.setString(1, mEmail);
            ps.setString(2, file);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int fileId = rs.getInt("id");

                // Переименовываем файл на диске
                String oldPath = directory + "/" + file;
                String newPath = directory + "/" + nname;
                File oldFile = new File(oldPath);
                File newFile = new File(newPath);
                if (oldFile.renameTo(newFile)) {
                    // Обновляем запись в базе
                    String updateSql = "UPDATE files SET file_name = ?, file_path = ? WHERE id = ?";
                    PreparedStatement updateStmt = Register.getConnection().prepareStatement(updateSql);
                    updateStmt.setString(1, nname);
                    updateStmt.setString(2, newPath);
                    updateStmt.setInt(3, fileId);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                    rs.close();
                    ps.close();
                    return true;
                }
                rs.close();
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    // CRUD для files
    public int createFile(String fileName, String filePath, long size) throws SQLException {
        String sql = "INSERT INTO files (user_email, file_name, file_path, size) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, mEmail);
        ps.setString(2, fileName);
        ps.setString(3, filePath);
        ps.setLong(4, size);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        int fileId = 0;
        if (rs.next()) {
            fileId = rs.getInt(1);
        }
        rs.close();
        ps.close();
        return fileId;
    }

    public List<String[]> readFiles() throws SQLException {
        String sql = "SELECT id, file_name, file_path, size FROM files WHERE user_email = ? AND status = 'active'";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setString(1, mEmail);
        ResultSet rs = ps.executeQuery();
        List<String[]> files = new ArrayList<>();
        while (rs.next()) {
            files.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("file_name"),
                    rs.getString("file_path"),
                    String.valueOf(rs.getLong("size"))
            });
        }
        rs.close();
        ps.close();
        return files;
    }

    public boolean updateFile(int fileId, String newFileName, String newFilePath) throws SQLException {
        String sql = "UPDATE files SET file_name = ?, file_path = ? WHERE id = ? AND user_email = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setString(1, newFileName);
        ps.setString(2, newFilePath);
        ps.setInt(3, fileId);
        ps.setString(4, mEmail);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public boolean deleteFile(int fileId) throws SQLException {
        String sql = "SELECT size FROM files WHERE id = ? AND user_email = ? AND status = 'active'";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setInt(1, fileId);
        ps.setString(2, mEmail);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            long size = rs.getLong("size");
            String updateSql = "UPDATE files SET status = 'deleted' WHERE id = ?";
            PreparedStatement updateStmt = Register.getConnection().prepareStatement(updateSql);
            updateStmt.setInt(1, fileId);
            updateStmt.executeUpdate();
            updateStmt.close();

            String updateSpace = "UPDATE user_storage SET used_space = used_space - ?, last_updated = NOW() WHERE user_email = ?";
            PreparedStatement spaceStmt = Register.getConnection().prepareStatement(updateSpace);
            spaceStmt.setLong(1, size);
            spaceStmt.setString(2, mEmail);
            spaceStmt.executeUpdate();
            spaceStmt.close();

            rs.close();
            ps.close();
            return true;
        }
        rs.close();
        ps.close();
        return false;
    }

    // CRUD для user_storage
    public boolean createUserStorage(String userEmail) throws SQLException {
        String sql = "INSERT INTO user_storage (user_email, used_space, quota) VALUES (?, 0, 10737418240)";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setString(1, userEmail);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public long[] readUserStorage(String userEmail) throws SQLException {
        String sql = "SELECT used_space, quota FROM user_storage WHERE user_email = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setString(1, userEmail);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            long[] result = new long[]{rs.getLong("used_space"), rs.getLong("quota")};
            rs.close();
            ps.close();
            return result;
        }
        rs.close();
        ps.close();
        return new long[]{0, 0};
    }

    public boolean updateUserStorage(String userEmail, long usedSpace) throws SQLException {
        String sql = "UPDATE user_storage SET used_space = ?, last_updated = NOW() WHERE user_email = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setLong(1, usedSpace);
        ps.setString(2, userEmail);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public boolean deleteUserStorage(String userEmail) throws SQLException {
        String sql = "DELETE FROM user_storage WHERE user_email = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setString(1, userEmail);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    // CRUD для tags
    public int createTag(String tagName) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM tags WHERE tag_name = ? AND user_email = ?";
        PreparedStatement checkPs = Register.getConnection().prepareStatement(checkSql);
        checkPs.setString(1, tagName);
        checkPs.setString(2, mEmail);
        ResultSet rs = checkPs.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            rs.close();
            checkPs.close();
            return -1; // tag already exist
        }
        rs.close();
        checkPs.close();

        String sql = "INSERT INTO tags (tag_name, user_email) VALUES (?, ?)";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, tagName);
        ps.setString(2, mEmail);
        ps.executeUpdate();
        rs = ps.getGeneratedKeys();
        int tagId = 0;
        if (rs.next()) {
            tagId = rs.getInt(1);
        }
        rs.close();
        ps.close();
        return tagId;
    }

    public List<String[]> readTags() throws SQLException {
        String sql = "SELECT id, tag_name FROM tags WHERE user_email = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setString(1, mEmail);
        ResultSet rs = ps.executeQuery();
        List<String[]> tags = new ArrayList<>();
        while (rs.next()) {
            tags.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("tag_name")});
        }
        rs.close();
        ps.close();
        return tags;
    }

    public boolean updateTag(int tagId, String newTagName) throws SQLException {
        String sql = "UPDATE tags SET tag_name = ? WHERE id = ? AND user_email = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setString(1, newTagName);
        ps.setInt(2, tagId);
        ps.setString(3, mEmail);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public boolean deleteTag(int tagId) throws SQLException {
        String deleteFileTags = "DELETE FROM file_tags WHERE tag_id = ?";
        PreparedStatement fileTagStmt = Register.getConnection().prepareStatement(deleteFileTags);
        fileTagStmt.setInt(1, tagId);
        fileTagStmt.executeUpdate();
        fileTagStmt.close();

        String sql = "DELETE FROM tags WHERE id = ? AND user_email = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setInt(1, tagId);
        ps.setString(2, mEmail);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    // CRUD для file_tags
    public boolean createFileTag(int fileId, int tagId) throws SQLException {
        String sql = "INSERT INTO file_tags (file_id, tag_id) VALUES (?, ?)";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setInt(1, fileId);
        ps.setInt(2, tagId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public List<Integer> readTagsForFile(int fileId) throws SQLException {
        String sql = "SELECT tag_id FROM file_tags WHERE file_id = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setInt(1, fileId);
        ResultSet rs = ps.executeQuery();
        List<Integer> tagIds = new ArrayList<>();
        while (rs.next()) {
            tagIds.add(rs.getInt("tag_id"));
        }
        rs.close();
        ps.close();
        return tagIds;
    }

    public List<Integer> readFilesForTag(int tagId) throws SQLException {
        String sql = "SELECT file_id FROM file_tags WHERE tag_id = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setInt(1, tagId);
        ResultSet rs = ps.executeQuery();
        List<Integer> fileIds = new ArrayList<>();
        while (rs.next()) {
            fileIds.add(rs.getInt("file_id"));
        }
        rs.close();
        ps.close();
        return fileIds;
    }

    public boolean deleteFileTag(int fileId, int tagId) throws SQLException {
        String sql = "DELETE FROM file_tags WHERE file_id = ? AND tag_id = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setInt(1, fileId);
        ps.setInt(2, tagId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    // Вспомогательные методы
    public String getTagName(int tagId) throws SQLException {
        String sql = "SELECT tag_name FROM tags WHERE id = ? AND user_email = ?";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setInt(1, tagId);
        ps.setString(2, mEmail);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String tagName = rs.getString("tag_name");
            rs.close();
            ps.close();
            return tagName;
        }
        rs.close();
        ps.close();
        return "";
    }

    public int getFileId(String fileName) throws SQLException {
        String sql = "SELECT id FROM files WHERE user_email = ? AND file_name = ? AND status = 'active'";
        PreparedStatement ps = Register.getConnection().prepareStatement(sql);
        ps.setString(1, mEmail);
        ps.setString(2, fileName);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int fileId = rs.getInt("id");
            rs.close();
            ps.close();
            return fileId;
        }
        rs.close();
        ps.close();
        return -1;
    }
}