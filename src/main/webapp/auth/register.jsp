<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Patient Registration - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    String error = request.getParameter("error");
%>

<main class="main-content" style="max-width: 520px;">
    <div class="card" style="padding: 2.25rem 2rem;">
        <div style="text-align: center; margin-bottom: 1.75rem;">
            <div class="brand-icon" style="margin: 0 auto 0.75rem; width: 48px; height: 48px; font-size: 1.75rem;">+</div>
            <h2 style="font-size: 1.6rem; font-weight: 800;">Create Patient Account</h2>
            <p style="color: var(--text-muted); font-size: 0.9rem;">Book online hospital tokens and track queues live</p>
        </div>

        <% if ("missing_fields".equals(error)) { %>
            <div class="alert alert-warning">Please fill in all required fields.</div>
        <% } else if ("password_mismatch".equals(error)) { %>
            <div class="alert alert-danger">Passwords do not match. Please verify.</div>
        <% } else if ("short_password".equals(error)) { %>
            <div class="alert alert-warning">Password must be at least 6 characters long.</div>
        <% } else if ("email_exists".equals(error)) { %>
            <div class="alert alert-danger">An account with this email address already exists.</div>
        <% } else if ("failed".equals(error) || "server_error".equals(error)) { %>
            <div class="alert alert-danger">Registration could not be completed. Please try again.</div>
        <% } %>

        <form action="${pageContext.request.contextPath}/auth/register" method="POST" onsubmit="return validateForm()">
            <div class="form-group">
                <label class="form-label" for="fullName">Full Name</label>
                <input type="text" id="fullName" name="fullName" class="form-control"
                       placeholder="e.g. Arun Kumar" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="email">Email Address</label>
                <input type="email" id="email" name="email" class="form-control"
                       placeholder="arun@example.com" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="phone">Phone Number</label>
                <input type="tel" id="phone" name="phone" class="form-control"
                       placeholder="10-digit mobile number" pattern="[0-9]{10}" required>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="password">Password</label>
                    <input type="password" id="password" name="password" class="form-control"
                           placeholder="••••••••" minlength="6" required>
                </div>
                <div class="form-group">
                    <label class="form-label" for="confirmPassword">Confirm Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" class="form-control"
                           placeholder="••••••••" minlength="6" required>
                </div>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; padding: 0.75rem; margin-top: 0.5rem;">
                Register Account
            </button>
        </form>

        <div style="text-align: center; margin-top: 1.5rem; font-size: 0.9rem; color: var(--text-muted);">
            Already have an account?
            <a href="${pageContext.request.contextPath}/auth/login.jsp" style="color: var(--primary); font-weight: 600;">Sign In here</a>
        </div>
    </div>
</main>

<script>
function validateForm() {
    const p1 = document.getElementById("password").value;
    const p2 = document.getElementById("confirmPassword").value;
    if (p1 !== p2) {
        alert("Passwords do not match!");
        return false;
    }
    return true;
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
