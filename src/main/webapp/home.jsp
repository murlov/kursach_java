<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
%>
<%@page import="Helpers.User"%>
<%@page import="java.util.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="style.css">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Home</title>
    <style>
        .tag-form, .tag-list {
            margin-top: 10px;
        }
        .tag-item {
            display: inline-block;
            background-color: #f0f0f0;
            padding: 5px 10px;
            margin: 5px;
            border-radius: 5px;
        }
    </style>
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
            <div class="" style="float: right;">
                <a href="<%=request.getContextPath()%>/Upload" class="pure-material-button-contained" style="float: right; text-decoration: none; margin: 10px;">
                    Upload File
                </a>
                <a href="<%=request.getContextPath()%>/Logout" class="pure-material-button-contained" style="float: right; text-decoration: none; margin: 10px;">
                    Logout
                </a>
            </div>
        </li>
    </ul>

    <% if (session.getAttribute("user") != null) {
        User u = (User) session.getAttribute("user");
        long space = u.getSpace();
        space = 10 - space;
    %>
    <div class="disk">
        > Approximately <strong><%=space%> GB</strong> Available
    </div>
</div>

<div class="content">
    <!-- Управление тегами -->
    <div class="tag-form">
        <h3>Manage Tags</h3>
        <%
            String tagStatus = (String) session.getAttribute("tag-status");
            if (tagStatus != null) {
                if ("duplicate".equals(tagStatus)) {
        %>
        <div class="er">Tag with this name already exists.</div>
        <%
        } else if ("created".equals(tagStatus)) {
        %>
        <div class="success">Tag successfully created.</div>
        <%
        } else if ("error".equals(tagStatus)) {
        %>
        <div class="er">An error occurred while creating the tag.</div>
        <%
                }
                session.removeAttribute("tag-status");
            }
        %>
        <form action="<%=request.getContextPath()%>/CreateTag" method="POST">
            <input type="text" name="tagName" placeholder="Enter tag name" required>
            <input type="submit" class="but" value="Create Tag">
        </form>
    </div>

    <div class="tag-list">
        <h4>Your Tags:</h4>
        <%
            List<String[]> tags = u.readTags();
            if (tags != null && !tags.isEmpty()) {
                for (String[] tag : tags) {
                    String tagId = tag[0];
                    String tagName = tag[1];
        %>
        <div class="tag-item">
            <%=tagName%>
            <form action="<%=request.getContextPath()%>/DeleteTag" method="POST" style="display:inline;">
                <input type="hidden" name="tagId" value="<%=tagId%>">
                <input type="submit" class="but" value="Delete">
            </form>
        </div>
        <%
            }
        } else {
        %>
        <p>No tags available.</p>
        <%
            }
        %>
    </div>

    <!-- Список файлов -->
    <%
        int i = 1;
        if (u.getContent("") != null) {
            String[] FileList = u.getContent("");
            for (String item : FileList) {
                int fileId = u.getFileId(item);
    %>
    <div class="item">
        <div class="name"><strong><%=item%></strong></div>
        <span style="float: right"><%=u.getSize(item)%></span>

        <div class="options">
            <form action="<%=request.getContextPath()%>/DownloadFile" method="POST">
                <input type="hidden" name="file" value="<%=item%>">
                <input type="submit" class="but" style="text-decoration: none; margin: 10px;" value="Download">
            </form>

            <form action="<%=request.getContextPath()%>/RenameFile" method="POST">
                <input type="hidden" name="file" value="<%=item%>">
                <input type="text" name="nname" placeholder="New name" required>
                <input type="submit" class="but" style="text-decoration: none; margin: 10px;" value="Rename">
            </form>

            <form action="<%=request.getContextPath()%>/Delete" method="POST">
                <input type="hidden" name="file" value="<%=item%>">
                <input type="submit" class="but" style="text-decoration: none; margin: 10px;" value="Delete">
            </form>

            <!-- Привязка тегов к файлу -->
            <form action="<%=request.getContextPath()%>/AddFileTag" method="POST" style="display:inline;">
                <input type="hidden" name="fileId" value="<%=fileId%>">
                <select name="tagId">
                    <% for (String[] tag : tags) { %>
                    <option value="<%=tag[0]%>"><%=tag[1]%></option>
                    <% } %>
                </select>
                <input type="submit" class="but" value="Add Tag">
            </form>

            <!-- Отображение тегов файла -->
            <div class="file-tags">
                <%
                    List<Integer> fileTags = u.readTagsForFile(fileId);
                    for (Integer tagId : fileTags) {
                        String tagName = u.getTagName(tagId);
                %>
                <span class="tag-item">
                    <%=tagName%>
                    <form action="<%=request.getContextPath()%>/RemoveFileTag" method="POST" style="display:inline;">
                        <input type="hidden" name="fileId" value="<%=fileId%>">
                        <input type="hidden" name="tagId" value="<%=tagId%>">
                        <input type="submit" class="but" value="Remove">
                    </form>
                </span>
                <%
                    }
                %>
            </div>
        </div>
    </div>
    <%
            i++;
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
        } else {
            response.sendRedirect(request.getContextPath() + "/Register");
        }
    %>
</div>
</body>
</html>