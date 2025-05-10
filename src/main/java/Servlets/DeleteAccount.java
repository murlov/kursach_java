package Servlets;

import Helpers.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DeleteAccount extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            request.getRequestDispatcher("/deleteAccount.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/Register");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            try {
                user.deleteAccount();
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/Login");
            } catch (Exception e) {
                request.setAttribute("error", "Error deleting account: " + e.getMessage());
                request.getRequestDispatcher("/deleteAccount.jsp").forward(request, response);
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/Register");
        }
    }
}