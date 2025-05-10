<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <link rel="stylesheet" href="style.css">
  <title>Delete Account</title>
</head>
<body>
<div class="container">
  <div class="box">
    <h2>Delete Account</h2>
    <p>Are you sure you want to delete your account? This action cannot be undone.</p>
    <%
      if (request.getAttribute("error") != null) {
    %>
    <div class="er">
      <%= request.getAttribute("error") %>
    </div>
    <%
      }
    %>
    <form action="DeleteAccount" method="post">
      <input type="submit" class="pure-material-button-contained" style="background-color: #ff4444" value="Confirm Delete">
      <a href="<%=request.getContextPath()%>/Home" class="pure-material-button-contained" style="text-decoration: none;">Cancel</a>
    </form>
  </div>
</div>
</body>
</html>