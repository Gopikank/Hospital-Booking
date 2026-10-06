<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${param.title != null ? param.title : "Hospital Queue System"}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <script>
        const contextPath = "${pageContext.request.contextPath}";
    </script>
</head>
<body>
