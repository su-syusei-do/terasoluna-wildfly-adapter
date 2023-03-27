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
    <a href="http://localhost:8080/realms/sample1/protocol/openid-connect/logout?post_logout_redirect_uri=http://localhost:8180/SampleWebApp/&amp;client_id=sample-web-app">Logout</a>
</body>
</html>
