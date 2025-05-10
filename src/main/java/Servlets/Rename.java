package Servlets;

import Helpers.User;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Rename extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            String oldName = request.getParameter("file");
            String newName = request.getParameter("nname");

            // Отладочный вывод
            System.out.println("Received oldName: '" + oldName + "'");
            System.out.println("Received newName: '" + newName + "'");

            if (oldName == null || oldName.trim().isEmpty()) {
                session.setAttribute("rename-status", "Error: Original file name is missing");
                response.sendRedirect(request.getContextPath() + "/Home");
                return;
            }

            if (newName != null && !newName.trim().isEmpty()) {
                try {
                    boolean success = user.rename(oldName.trim(), newName.trim());
                    if (success) {
                        response.sendRedirect(request.getContextPath() + "/Home");
                    } else {
                        session.setAttribute("rename-status", "Error renaming file: Operation failed");
                        response.sendRedirect(request.getContextPath() + "/RenameFile?file=" + URLEncoder.encode(oldName.trim(), "UTF-8"));
                    }
                } catch (Exception e) {
                    session.setAttribute("rename-status", "Error renaming file: " + e.getMessage());
                    response.sendRedirect(request.getContextPath() + "/RenameFile?file=" + URLEncoder.encode(oldName.trim(), "UTF-8"));
                }
            } else {
                session.setAttribute("rename-status", "Error: New name cannot be empty");
                response.sendRedirect(request.getContextPath() + "/RenameFile?file=" + URLEncoder.encode(oldName.trim(), "UTF-8"));
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/Register");
        }
    }
}