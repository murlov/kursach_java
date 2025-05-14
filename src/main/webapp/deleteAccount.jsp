<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <link rel="stylesheet" href="style.css">
  <title>Delete Account</title>
  <style>
    .container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
      background: linear-gradient(135deg, #1e3c72, #2a5298);
      margin: 0;
    }
    .box {
      background: rgba(30, 60, 114, 0.9);
      padding: 30px;
      border-radius: 15px;
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
      text-align: center;
      width: 400px;
      color: white;
    }
    .box h2 {
      margin-bottom: 20px;
      font-size: 24px;
      color: #ffffff;
    }
    .box p {
      font-size: 18px;
      color: #ffffff;
      margin-bottom: 30px;
      line-height: 1.5;
    }
    .box form {
      display: inline-block;
      margin: 0;
    }
    .pure-material-button-contained {
      padding: 12px 25px;
      margin: 0 10px;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-size: 16px;
      transition: background-color 0.3s;
      height: 60px;
      box-sizing: border-box;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      text-decoration: none;
      color: white;
    }
    .pure-material-button-contained[type="submit"] {
      background-color: #ff4444;
    }
    .pure-material-button-contained[type="submit"]:hover {
      background-color: #cc0000;
    }
    .pure-material-button-contained[a] {
      background-color: #007bff;
    }
    .pure-material-button-contained[a]:hover {
      background-color: #0056b3;
    }
    .er {
      color: #ff4444;
      margin-bottom: 20px;
      font-size: 16px;
    }
  </style>
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
      <input type="submit" class="pure-material-button-contained" value="Confirm Delete">
    </form>
    <a href="<%=request.getContextPath()%>/Home" class="pure-material-button-contained">Cancel</a>
  </div>
</div>
</body>
</html>