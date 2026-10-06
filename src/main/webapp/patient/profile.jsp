<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.UserDAO, com.hospital.dao.DepartmentDAO, com.hospital.model.User, com.hospital.model.Department, com.hospital.util.CookieUtil, java.util.List" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Patient Preferences - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    Integer userId = (Integer) session.getAttribute("userId");
    UserDAO userDAO = new UserDAO();
    DepartmentDAO deptDAO = new DepartmentDAO();
    User patient = (userId != null) ? userDAO.findById(userId) : null;
    List<Department> departments = deptDAO.getActiveDepartments();

    // Read existing cookies
    String prefDept = CookieUtil.getCookieValue(request, "preferredDepartment");
    String prefLang = CookieUtil.getCookieValue(request, "preferredLanguage");
    String displayPref = CookieUtil.getCookieValue(request, "queueDisplayPref");
    if (prefLang == null) prefLang = "English";
    if (displayPref == null) displayPref = "Detailed";

    // Handle form submit if POST
    String updatedMsg = null;
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String newDept = request.getParameter("preferredDepartment");
        String newLang = request.getParameter("preferredLanguage");
        String newDisplay = request.getParameter("queueDisplayPref");

        if (newDept != null) CookieUtil.setCookie(response, "preferredDepartment", newDept);
        if (newLang != null) CookieUtil.setCookie(response, "preferredLanguage", newLang);
        if (newDisplay != null) CookieUtil.setCookie(response, "queueDisplayPref", newDisplay);

        prefDept = newDept;
        prefLang = newLang;
        displayPref = newDisplay;
        updatedMsg = "Preferences successfully saved!";
    }
%>

<main class="main-content" style="max-width: 640px;">
    <div style="margin-bottom: 2rem;">
        <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">Patient Profile & Preferences</h1>
        <p style="color: var(--text-muted); font-size: 0.95rem;">
            Manage your personal details and appointment preferences
        </p>
    </div>

    <% if (updatedMsg != null) { %>
        <div class="alert alert-success"><%= updatedMsg %></div>
    <% } %>

    <!-- Account Details -->
    <div class="card" style="margin-bottom: 1.5rem;">
        <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 1rem;">Account Information</h3>
        <table class="table" style="font-size: 0.9rem;">
            <tr>
                <th style="width: 35%;">Full Name:</th>
                <td><%= patient != null ? patient.getFullName() : "" %></td>
            </tr>
            <tr>
                <th>Email Address:</th>
                <td><%= patient != null ? patient.getEmail() : "" %></td>
            </tr>
            <tr>
                <th>Phone Number:</th>
                <td><%= patient != null ? patient.getPhone() : "" %></td>
            </tr>
            <tr>
                <th>Account Type:</th>
                <td><span class="badge badge-waiting">PATIENT</span></td>
            </tr>
        </table>
    </div>

    <!-- Clinic Preferences -->
    <div class="card">
        <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 0.5rem;">Clinic Preferences</h3>
        <p style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 1.25rem;">
            Customize your default clinic and display modes across your visits:
        </p>

        <form method="POST">
            <div class="form-group">
                <label class="form-label" for="preferredDepartment">Preferred Department</label>
                <select id="preferredDepartment" name="preferredDepartment" class="form-control">
                    <option value="">-- None --</option>
                    <% for (Department d : departments) {
                        boolean sel = d.getDepartmentName().equalsIgnoreCase(prefDept);
                    %>
                        <option value="<%= d.getDepartmentName() %>" <%= sel ? "selected" : "" %>>
                            <%= d.getDepartmentName() %>
                        </option>
                    <% } %>
                </select>
                <small style="color: var(--text-muted); font-size: 0.75rem;">Pre-selects this clinic when opening token booking.</small>
            </div>

            <div class="form-group">
                <label class="form-label" for="preferredLanguage">Preferred Language</label>
                <select id="preferredLanguage" name="preferredLanguage" class="form-control">
                    <option value="English" <%= "English".equals(prefLang) ? "selected" : "" %>>English</option>
                    <option value="Hindi" <%= "Hindi".equals(prefLang) ? "selected" : "" %>>Hindi</option>
                    <option value="Tamil" <%= "Tamil".equals(prefLang) ? "selected" : "" %>>Tamil</option>
                    <option value="Malayalam" <%= "Malayalam".equals(prefLang) ? "selected" : "" %>>Malayalam</option>
                </select>
            </div>

            <div class="form-group">
                <label class="form-label" for="queueDisplayPref">Queue Display Mode</label>
                <select id="queueDisplayPref" name="queueDisplayPref" class="form-control">
                    <option value="Detailed" <%= "Detailed".equals(displayPref) ? "selected" : "" %>>Detailed Card View</option>
                    <option value="Compact" <%= "Compact".equals(displayPref) ? "selected" : "" %>>Compact Table View</option>
                </select>
            </div>

            <button type="submit" class="btn btn-primary" style="padding: 0.65rem 1.5rem;">Save Preferences</button>
        </form>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
