<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.util.CookieUtil" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Login - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    String rememberedEmail = CookieUtil.getCookieValue(request, "rememberEmail");
    if (rememberedEmail == null) rememberedEmail = "";
    String error = request.getParameter("error");
    String logout = request.getParameter("logout");
%>

<main class="main-content" style="max-width: 480px;">
    <div class="card" style="padding: 2.25rem 2rem;">
        <div style="text-align: center; margin-bottom: 1.75rem;">
            <div class="brand-icon" style="margin: 0 auto 0.75rem; width: 48px; height: 48px; font-size: 1.75rem;">+</div>
            <h2 style="font-size: 1.6rem; font-weight: 800;">Welcome Back</h2>
            <p style="color: var(--text-muted); font-size: 0.9rem;">Sign in to your hospital portal</p>
        </div>

        <% if ("invalid_credentials".equals(error)) { %>
            <div class="alert alert-danger">Invalid email or password. Please try again.</div>
        <% } else if ("session_expired".equals(error)) { %>
            <div class="alert alert-warning">Your session has expired. Please login again.</div>
        <% } else if ("unauthorized".equals(error)) { %>
            <div class="alert alert-danger">You must log in to access that page.</div>
        <% } else if ("missing_fields".equals(error)) { %>
            <div class="alert alert-warning">Please enter both email and password.</div>
        <% } else if ("true".equals(logout)) { %>
            <div class="alert alert-success">You have been logged out successfully.</div>
        <% } %>

        <form action="${pageContext.request.contextPath}/auth/login" method="POST" autocomplete="off">
            <div class="form-group">
                <label class="form-label" for="email">Email Address</label>
                <input type="email" id="email" name="email" class="form-control"
                       placeholder="name@example.com" value="" autocomplete="off" required autofocus>
            </div>

            <div class="form-group">
                <label class="form-label" for="password">Password</label>
                <input type="password" id="password" name="password" class="form-control"
                       placeholder="••••••••" autocomplete="new-password" required>
            </div>

            <div class="form-group" style="display: flex; align-items: center; justify-content: space-between;">
                <label style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.85rem; cursor: pointer;">
                    <input type="checkbox" name="remember" value="true">
                    Remember my email
                </label>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; padding: 0.75rem;">Sign In</button>
        </form>

        <div style="text-align: center; margin-top: 1.5rem; font-size: 0.9rem; color: var(--text-muted);">
            Don't have an account?
            <a href="${pageContext.request.contextPath}/auth/register.jsp" style="color: var(--primary); font-weight: 600;">Register as Patient</a>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
