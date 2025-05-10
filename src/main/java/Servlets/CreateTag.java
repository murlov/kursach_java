package Servlets;

import Helpers.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CreateTag extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User u = (User) session.getAttribute("user");
            String tagName = request.getParameter("tagName");
            try {
                int result = u.createTag(tagName);
                if (result == -1) {
                    session.setAttribute("tag-status", "duplicate");
                } else {
                    session.setAttribute("tag-status", "created");
                }
                response.sendRedirect(request.getContextPath() + "/Home");
            } catch (Exception e) {
                session.setAttribute("tag-status", "error");
                response.sendRedirect(request.getContextPath() + "/Home");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/Register");
        }
    }
}