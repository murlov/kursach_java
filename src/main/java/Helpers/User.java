package Helpers;

import Repositories.*;
import Servlets.UploadFile;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

public class User {
    private int userId;
    private String name;
    private String email;
    private String directory;
    private final String filePath = "D:\\Programming\\java\\StorageSpace\\";

    private final UserRepository userRepository = new UserRepository();
    private final FileRepository fileRepository = new FileRepository();
    private final UserStorageRepository userStorageRepository = new UserStorageRepository();

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; this.directory = filePath + email; }
    public String getDirectory() { return directory; }

    public boolean registerUser(String name, String email, String password) throws SQLException {
        int userId = userRepository.registerUser(name, email, password);
        if (userId > 0) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.directory = filePath + email;
            userStorageRepository.initializeStorage(userId);
            return makeDir(email);
        }
        return false;
    }

    public boolean makeDir(String email) {
        try {
            File file = new File(filePath + email);
            if (file.mkdir()) {
                directory = filePath + email;
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSize(String fileName) {
        File file = new File(directory + "/" + fileName);
        return FileUtils.byteCountToDisplaySize(file.length());
    }

    public long getSpace() throws SQLException {
        return userStorageRepository.getAvailableSpace(userId);
    }

    public String handleFile(HttpServletRequest request) throws SQLException {
        if (getSpace() > 0) {
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
                        String fileName = fi.getName();
                        String contentType = fi.getContentType();
                        long fileSize = fi.getSize();

                        File file;
                        if (fileName.lastIndexOf("\\") >= 0) {
                            file = new File(directory + "/" + fileName.substring(fileName.lastIndexOf("\\")));
                        } else {
                            file = new File(directory + "/" + fileName.substring(fileName.lastIndexOf("\\") + 1));
                        }
                        fi.write(file);

                        fileRepository.saveFile(userId, fileName, file.getAbsolutePath(), fileSize);
                        userStorageRepository.updateUsedSpace(userId, fileSize);

                        return "success";
                    }
                }
            } catch (FileExistsException e) {
                return "That File Already Exists!";
            } catch (Exception e) {
                return "An Unexpected Error Occurred! - " + e;
            }
        }
        return "Your 10 GB Quota has been exceeded!";
    }

    public String[] getContent(String dir) throws SQLException {
        List<String> fileNames = fileRepository.getFileNamesByUserId(userId);
        return fileNames.toArray(new String[0]);
    }

    public boolean validateEmail(String email) throws SQLException {
        return userRepository.validateEmail(email);
    }

    public boolean validate(String email, String password) throws SQLException {
        User user = userRepository.validateUser(email, password);
        if (user != null) {
            this.userId = user.userId;
            this.name = user.name;
            this.email = user.email;
            this.directory = filePath + email;
            return true;
        }
        return false;
    }

    public String getFileLocation(String fileName) throws SQLException {
        return fileRepository.getFilePath(userId, fileName);
    }

    public boolean delete(String fileName) throws SQLException {
        long fileSize = fileRepository.getFileSize(userId, fileName);
        if (fileRepository.deleteFile(userId, fileName)) {
            File file = new File(directory + "/" + fileName);
            if (file.delete()) {
                userStorageRepository.updateUsedSpace(userId, -fileSize);
                return true;
            }
        }
        return false;
    }

    public boolean rename(String oldName, String newName) throws SQLException, IOException {
        // Проверка на пустое или некорректное новое имя
        if (newName == null || newName.trim().isEmpty() || newName.contains("/") || newName.contains("\\")) {
            throw new IOException("Invalid new file name");
        }

        File oldFile = new File(directory + "/" + oldName);
        File newFile = new File(directory + "/" + newName);

        // Проверка существования старого файла
        if (!oldFile.exists()) {
            throw new IOException("File does not exist: " + oldName);
        }

        // Проверка, не существует ли уже файл с новым именем
        if (newFile.exists()) {
            throw new IOException("A file with the name " + newName + " already exists");
        }

        // Переименование файла на диске
        if (oldFile.renameTo(newFile)) {
            // Обновление в базе данных
            return fileRepository.renameFile(userId, oldName, newName, newFile.getAbsolutePath());
        } else {
            throw new IOException("Failed to rename file on disk: " + oldName + " to " + newName);
        }
    }

    public boolean addTagToFile(String fileName, String tagName) throws SQLException {
        return fileRepository.addTagToFile(userId, fileName, tagName);
    }

    public boolean removeTagFromFile(String fileName, String tagName) throws SQLException {
        return fileRepository.removeTagFromFile(userId, fileName, tagName);
    }

    public List<String> getFileTags(String fileName) throws SQLException {
        return fileRepository.getFileTags(userId, fileName);
    }

    public boolean deleteAccount() throws SQLException {
        File userDir = new File(directory);
        if (userDir.exists()) {
            try {
                FileUtils.deleteDirectory(userDir);
            } catch (IOException e) {
                // Логируем ошибку, но продолжаем удаление из базы
            }
        }
        return userRepository.deleteUser(userId);
    }
}