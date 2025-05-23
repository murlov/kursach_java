package Servlets;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import Servlets.Login;
import Helpers.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Register extends HttpServlet {

    static Connection con = null;

    public static Connection getConnection() {
        if (con == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cloud_db?zeroDateTimeBehavior=convertToNull", "root", "YD2y8g6jxNwyZvhb");
            } catch (SQLException ex) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return con;
    }

    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cloud_db?zeroDateTimeBehavior=convertToNull", "root", "YD2y8g6jxNwyZvhb");
            if (con == null) {
                throw new ServletException("Failed to initialize database connection");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Register.class.getName()).log(Level.SEVERE, "Failed to initialize database", ex);
            throw new ServletException("Database connection error", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        HttpSession session = request.getSession(false);

        User u = new User();
        try {
            if (u.validateEmail(email)) {
                if (u.makeDir(email)) {
                    if (u.registerUser(name, email, password)) {
                        session.setAttribute("user", u);
                        response.sendRedirect(request.getContextPath() + "/Home");
                    }
                } else {
                    session.setAttribute("rstatus", "An Unexpected Error has occurred!");
                    response.sendRedirect(request.getContextPath() + "/Register");
                }
            } else {
                session.setAttribute("rstatus", "That E-Mail already exists!");
                response.sendRedirect(request.getContextPath() + "/Register");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Register.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
