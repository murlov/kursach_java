package Servlets;

import Helpers.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AddTag extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fileName = request.getParameter("file");
        String tagName = request.getParameter("tag");

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            try {
                user.addTagToFile(fileName, tagName);
            } catch (Exception e) {
                session.setAttribute("tag-status", "Error adding tag: " + e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/Home");
        } else {
            response.sendRedirect(request.getContextPath() + "/Register");
        }
    }
}