<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Server Error - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<main class="main-content" style="text-align: center; padding: 4rem 1rem;">
    <div style="max-width: 520px; margin: 0 auto;">
        <div style="font-size: 4rem; color: var(--danger); font-weight: 900; line-height: 1;">500</div>
        <h2 style="font-size: 1.75rem; font-weight: 800; margin: 1rem 0 0.5rem;">Internal Processing Error</h2>
        <p style="color: var(--text-muted); margin-bottom: 2rem;">
            An unexpected error occurred while processing your queue request. Our technical staff has logged the incident.
        </p>
        <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-primary">Return to Hospital Home</a>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
