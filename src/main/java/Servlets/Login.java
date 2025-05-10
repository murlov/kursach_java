package Servlets;

import Helpers.User;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Login extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = new User();
        boolean status;
        try {
            status = user.validate(request.getParameter("email"), request.getParameter("password"));
        } catch (SQLException ex) {
            status = false;
        }
        HttpSession session = request.getSession();
        if (status) {
            session.setAttribute("user", user);
            response.sendRedirect(request.getContextPath() + "/Home");
        } else {
            session.setAttribute("status", "false");
            response.sendRedirect(request.getContextPath() + "/Login");
        }
    }
}