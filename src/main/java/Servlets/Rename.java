package Servlets;

import Helpers.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Rename extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String newName = request.getParameter("nname");
        String fileName = request.getParameter("file");

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            try {
                user.rename(fileName, newName);
            } catch (Exception e) {
                // Логирование ошибки
            }
            response.sendRedirect(request.getContextPath() + "/Home");
        } else {
            response.sendRedirect(request.getContextPath() + "/Register");
        }
    }
}