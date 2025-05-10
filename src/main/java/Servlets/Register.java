package Servlets;

import Helpers.User;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Register extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        HttpSession session = request.getSession();
        User user = new User();
        try {
            if (user.validateEmail(email)) {
                if (user.registerUser(name, email, password)) {
                    session.setAttribute("user", user);
                    response.sendRedirect(request.getContextPath() + "/Home");
                } else {
                    session.setAttribute("rstatus", "An Unexpected Error has occurred!");
                    response.sendRedirect(request.getContextPath() + "/Register");
                }
            } else {
                session.setAttribute("rstatus", "That E-Mail already exists!");
                response.sendRedirect(request.getContextPath() + "/Register");
            }
        } catch (SQLException ex) {
            session.setAttribute("rstatus", "Database error occurred!");
            response.sendRedirect(request.getContextPath() + "/Register");
        }
    }
}