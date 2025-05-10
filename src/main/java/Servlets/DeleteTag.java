package Servlets;

import Helpers.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DeleteTag extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User u = (User) session.getAttribute("user");
            int tagId = Integer.parseInt(request.getParameter("tagId"));
            try {
                u.deleteTag(tagId);
                response.sendRedirect(request.getContextPath() + "/Home");
            } catch (Exception e) {
                response.sendRedirect(request.getContextPath() + "/Home");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/Register");
        }
    }
}