<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Secured</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/app/css/styles.css">
</head>
<body>
    <div id="wrapper">
        <h1>Secured...</h1>
        <textarea cols="100" rows="30">${AccessToken}</textarea>
        <textarea cols="100" rows="30">${IDToken}</textarea>
    </div>
    <a href="${pageContext.request.contextPath}/logout">Logout</a>
</body>
</html>
