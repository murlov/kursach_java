package Helpers;

import Servlets.Register;
import Servlets.UploadFile;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User extends HttpServlet {

    private File file;
    public String directory = null;
    private String mEmail;
    private String mName;
    private String filePath = "D:\\Programming\\java\\StorageSpace";
    private int userId; // Добавляем поле для хранения ID пользователя

    public boolean registerUser(String name, String email, String password) throws SQLException {
        // Добавляем пользователя в таблицу users
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        PreparedStatement stmt = Register.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, name);
        stmt.setString(2, email);
        stmt.setString(3, password);
        int affectedRows = stmt.executeUpdate();

        if (affectedRows == 0) {
            return false;
        }

        // Получаем сгенерированный ID пользователя
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            userId = generatedKeys.getInt(1);
            // Назначаем пользователю роль USER (предположим, id роли USER = 2)
            String userRoleSql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, 2)";
            PreparedStatement userRoleStmt = Register.getConnection().prepareStatement(userRoleSql);
            userRoleStmt.setInt(1, userId);
            userRoleStmt.executeUpdate();
        }

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
        File f = new File(directory + "/" + file);
        String size = FileUtils.byteCountToDisplaySize(f.length());
        System.out.println(size);
        return size;
    }

    public long getSpace() {
        long size = FileUtils.sizeOfDirectory(new File(directory));
        long mb = size / (1024 * 1024);
        long gb = mb / 1024;
        return gb;
    }

    public String handleFile(HttpServletRequest request) {
        if (getSpace() < 10) {
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
                        String fieldName = fi.getFieldName();
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
                        System.out.println("Uploaded Filename: " + fileName + "<br>");

                        long fileSize = FileUtils.sizeOf(file);

                        // Добавляем информацию о файле в таблицу files
                        String sql = "INSERT INTO files (user_id, file_name, file_path, size, upload_date) VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement stmt = Register.getConnection().prepareStatement(sql);
                        stmt.setInt(1, userId);
                        stmt.setString(2, fileName);
                        stmt.setString(3, file.getAbsolutePath());
                        stmt.setLong(4, fileSize);
                        stmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        stmt.executeUpdate();
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

    public String[] getContent(String dir) throws SQLException {
        // Получаем список файлов из таблицы files вместо файловой системы
        String sql = "SELECT file_name FROM files WHERE user_id = ? AND file_path LIKE ?";
        PreparedStatement stmt = Register.getConnection().prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.setString(2, directory + dir + "%");
        ResultSet rs = stmt.executeQuery();

        // Преобразуем результат в массив
        rs.last();
        String[] fileList = new String[rs.getRow()];
        rs.beforeFirst();
        int index = 0;
        while (rs.next()) {
            fileList[index++] = rs.getString("file_name");
        }
        return fileList;
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
        PreparedStatement preparedStatement = Servlets.Login.getConnection().prepareStatement(
                "SELECT u.*, s.status_name FROM users u " +
                        "JOIN statuses s ON u.status_id = s.id " +
                        "WHERE u.email = ? AND u.password = ?"
        );
        preparedStatement.setString(1, email);
        preparedStatement.setString(2, password);
        System.out.println(preparedStatement);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            mName = rs.getString("name");
            mEmail = rs.getString("email");
            userId = rs.getInt("id");
            String status = rs.getString("status_name");
            directory = filePath + email;
            // Проверяем, активен ли пользователь
            return "ACTIVE".equals(status);
        }
        preparedStatement.close();
        return false;
    }

    public String getFileLocation(String name) throws SQLException {
        String sql = "SELECT file_path FROM files WHERE user_id = ? AND file_name = ?";
        PreparedStatement stmt = Register.getConnection().prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.setString(2, name);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString("file_path");
        }
        return null;
    }

    public boolean delete(String name) throws SQLException {
        // Удаляем запись из таблицы files
        String sql = "DELETE FROM files WHERE user_id = ? AND file_name = ?";
        PreparedStatement stmt = Register.getConnection().prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.setString(2, name);
        int rows = stmt.executeUpdate();

        // Удаляем файл с диска
        if (rows > 0) {
            File file = new File(directory + "/" + name);
            if (file.delete()) {
                System.out.println("File deleted successfully");
                return true;
            }
        }
        System.out.println("Failed to delete the file");
        return false;
    }

    public boolean rename(String file, String nname) throws IOException, SQLException {
        String String1 = directory + "/" + file;
        String String2 = directory + "/" + nname;

        String1 = String1.replaceAll("\\s", "");
        String2 = String2.replaceAll("\\s", "");

        File oldName = new File(String1);
        File newName = new File(String2);

        if (oldName.renameTo(newName)) {
            // Обновляем запись в таблице files
            String sql = "UPDATE files SET file_name = ?, file_path = ? WHERE user_id = ? AND file_name = ?";
            PreparedStatement stmt = Register.getConnection().prepareStatement(sql);
            stmt.setString(1, nname);
            stmt.setString(2, String2);
            stmt.setInt(3, userId);
            stmt.setString(4, file);
            stmt.executeUpdate();
            System.out.println("Renamed successfully");
            return true;
        } else {
            System.out.println("Error");
            return false;
        }
    }

    // Геттер для userId
    public int getUserId() {
        return userId;
    }
}