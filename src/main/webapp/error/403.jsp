<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Access Forbidden - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<main class="main-content" style="text-align: center; padding: 4rem 1rem;">
    <div style="max-width: 480px; margin: 0 auto;">
        <div style="font-size: 4rem; color: var(--danger); font-weight: 900; line-height: 1;">403</div>
        <h2 style="font-size: 1.75rem; font-weight: 800; margin: 1rem 0 0.5rem;">Access Forbidden</h2>
        <p style="color: var(--text-muted); margin-bottom: 2rem;">
            You do not have permission to access this resource or page with your current user role.
        </p>
        <div class="btn-group" style="justify-content: center;">
            <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-primary">Go to Home</a>
            <a href="${pageContext.request.contextPath}/auth/login.jsp" class="btn btn-outline">Switch Account</a>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
