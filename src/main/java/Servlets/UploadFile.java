package Servlets;

import Helpers.User;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class UploadFile extends HttpServlet {
    static public int maxFileSize = 1024 * 1024 * 1024; // 1 GB
    static public int maxMemSize = 3024 * 1024 * 1024; // 3 GB

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean isMultipart = org.apache.commons.fileupload.servlet.ServletFileUpload.isMultipartContent(request);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (!isMultipart) {
            out.println("<html><head><title>Servlet upload</title></head><body><p>No file uploaded</p></body></html>");
            return;
        }

        HttpSession session = request.getSession();
        if (session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
        } else {
            User user = (User) session.getAttribute("user");
            String result;
            try {
                result = user.handleFile(request);
            } catch (Exception e) {
                result = "Error: " + e.getMessage();
            }
            session.setAttribute("upload-status", result);
            response.sendRedirect(request.getContextPath() + "/Upload");
        }
    }
}