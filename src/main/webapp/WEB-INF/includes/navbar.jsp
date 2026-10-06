<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String userRole = (String) session.getAttribute("role");
    String userName = (String) session.getAttribute("name");
    boolean isLoggedIn = (userRole != null && session.getAttribute("userId") != null);
%>
<header class="navbar">
    <div class="nav-container">
        <a href="${pageContext.request.contextPath}/index.jsp" class="brand">
            <span class="brand-icon">+</span>
            <span>City Care Hospital</span>
        </a>

        <ul class="nav-links">
            <li><a href="${pageContext.request.contextPath}/index.jsp">Home</a></li>

            <% if ("PATIENT".equals(userRole)) { %>
                <li><a href="${pageContext.request.contextPath}/patient/dashboard.jsp">Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/patient/book-token.jsp">Book Token</a></li>
                <li><a href="${pageContext.request.contextPath}/patient/live-queue.jsp">Live Queue</a></li>
                <li><a href="${pageContext.request.contextPath}/patient/appointments.jsp">My Appointments</a></li>
                <li><a href="${pageContext.request.contextPath}/patient/profile.jsp">Preferences</a></li>
            <% } else if ("DOCTOR".equals(userRole)) { %>
                <li><a href="${pageContext.request.contextPath}/doctor/dashboard.jsp">Doctor Console</a></li>
                <li><a href="${pageContext.request.contextPath}/doctor/queue.jsp">Today's Queue</a></li>
            <% } else if ("ADMIN".equals(userRole)) { %>
                <li><a href="${pageContext.request.contextPath}/admin/dashboard.jsp">Admin Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/queue-monitor.jsp">Queue Monitor</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/departments.jsp">Departments</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/doctors.jsp">Doctors</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/rooms.jsp">Rooms</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/patients.jsp">Patients</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/xml-config.jsp">XML Config</a></li>
            <% } %>

            <% if (!isLoggedIn) { %>
                <li><a href="${pageContext.request.contextPath}/auth/login.jsp" class="btn btn-outline" style="padding: 0.35rem 0.8rem;">Login</a></li>
                <li><a href="${pageContext.request.contextPath}/auth/register.jsp" class="btn btn-primary" style="padding: 0.35rem 0.8rem;">Register</a></li>
            <% } else { %>
                <li class="nav-user">
                    <span class="user-badge"><%= userRole %></span>
                    <span style="font-weight: 600; font-size: 0.9rem;"><%= userName %></span>
                    <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-outline" style="padding: 0.25rem 0.65rem; font-size: 0.85rem;">Logout</a>
                </li>
            <% } %>
        </ul>
    </div>
</header>
