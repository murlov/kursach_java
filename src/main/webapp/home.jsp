<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
%>
<%@page import="Helpers.User"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="style.css">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Home</title>
</head>
<body>
<div class="heading">
    <ul>
        <li>
            <a href="<%=request.getContextPath()%>/Home" style="float: left; padding-bottom: 0px;">
                <h1>Home</h1>
            </a>
        </li>
        <li>
            <div style="float: right;">
                <a href="<%=request.getContextPath()%>/Upload" class="pure-material-button-contained" style="float: right; text-decoration: none; margin: 10px;">
                    Upload File
                </a>
                <a href="<%=request.getContextPath()%>/Logout" class="pure-material-button-contained" style="float: right; text-decoration: none; margin: 10px;">
                    Logout
                </a>
                <a href="<%=request.getContextPath()%>/DeleteAccount" class="pure-material-button-contained" style="float: right; text-decoration: none; margin: 10px; background-color: #ff4444;">
                    Delete Account
                </a>
            </div>
        </li>
    </ul>
    <%
        if (session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            long space;
            try {
                space = user.getSpace();
            } catch (Exception e) {
                space = 0;
            }
    %>
    <div class="disk">
        > Approximately <strong><%=space%> GB</strong> Available
    </div>
</div>

<div class="content">
    <%
        try {
            String[] fileList = user.getContent("");
            if (fileList != null && fileList.length > 0) {
                for (String item : fileList) {
                    List<String> tags = user.getFileTags(item);
    %>
    <div class="item">
        <div class="name"><strong><%=item%></strong></div>
        <span style="float: right"><%=user.getSize(item)%></span>
        <div class="tags">
            <strong>Tags:</strong> <%=tags.isEmpty() ? "None" : String.join(", ", tags)%>
            <% if (!tags.isEmpty()) { %>
            <form action="RemoveTag" method="POST">
                <input type="hidden" name="file" value="<%=item%>">
                <select name="tag" style="margin: 10px;">
                    <% for (String tag : tags) { %>
                    <option value="<%=tag%>"><%=tag%></option>
                    <% } %>
                </select>
                <input type="submit" class="but" style="text-decoration: none; margin: 10px;" value="Remove Tag">
            </form>
            <% } %>
        </div>
        <div class="options">
            <form action="DownloadFile" method="POST">
                <input type="hidden" name="file" value="<%=item%>">
                <input type="submit" class="but" style="text-decoration: none; margin: 10px;" value="Download">
            </form>
            <form action="RenameFile" method="POST">
                <input type="hidden" name="file" value="<%=item%>">
                <input type="submit" class="but" style="text-decoration: none; margin: 10px;" value="Rename">
            </form>
            <form action="Delete" method="POST">
                <input type="hidden" name="file" value="<%=item%>">
                <input type="submit" class="but" style="text-decoration: none; margin: 10px;" value="Delete">
            </form>
            <form action="AddTag" method="POST">
                <input type="hidden" name="file" value="<%=item%>">
                <input type="text" name="tag" placeholder="Enter tag" style="margin: 10px; width: 100px;">
                <input type="submit" class="but" style="text-decoration: none; margin: 10px;" value="Add Tag">
            </form>
        </div>
    </div>
    <%
        }
    } else {
    %>
    <div class="Empty">
        <h2>Currently there are no Files Uploaded!</h2>
        <a href="<%=request.getContextPath()%>/Upload" class="fu" style="text-decoration: none; margin: 10px;">
            Start Uploading
        </a>
    </div>
    <%
                }
            } catch (Exception e) {
                response.sendRedirect(request.getContextPath() + "/Register");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/Register");
        }
    %>
</div>
</body>
</html>