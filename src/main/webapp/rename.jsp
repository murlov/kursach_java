<%@page import="Helpers.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="style.css">
    <title>Rename</title>
</head>
<body>
<div class="container">
    <div class="box">
        <h2>Select New Name:</h2>
        <%
            String file = request.getParameter("file");
            if (file == null || file.trim().isEmpty() || session.getAttribute("user") == null) {
                response.sendRedirect(request.getContextPath() + "/Register");
                return;
            }
        %>
        <form action="R" method="post">
            <div class="input-box">
                <input type="text" name="nname" required />
                <label>New Name</label>
            </div>
            <input type="hidden" name="file" value="<%=file.trim()%>">
            <p style="color: white">For file named: <strong><%=file%></strong></p>
            <br><br>
            <input type="submit" class="pure-material-button-contained" value="Rename" />
        </form>
        <%
            if (session.getAttribute("rename-status") != null) {
        %>
        <div class="er">
            <%= session.getAttribute("rename-status") %>
        </div>
        <%
                session.removeAttribute("rename-status");
            }
        %>
    </div>
</div>
</body>
</html>