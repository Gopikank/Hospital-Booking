<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.UserDAO, com.hospital.dao.AppointmentDAO, com.hospital.model.User, com.hospital.model.Appointment, java.util.List" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Patient Directory & History - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    UserDAO userDAO = new UserDAO();
    AppointmentDAO apptDAO = new AppointmentDAO();

    String selectedPatientIdStr = request.getParameter("patientId");
    List<User> patients = userDAO.getAllPatients();
    List<Appointment> history = null;
    User selectedPatient = null;

    if (selectedPatientIdStr != null && !selectedPatientIdStr.isEmpty()) {
        int pid = Integer.parseInt(selectedPatientIdStr);
        selectedPatient = userDAO.findById(pid);
        history = apptDAO.getAppointmentsByPatient(pid);
    }
%>

<main class="main-content">
    <div style="margin-bottom: 2rem;">
        <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">Registered Patients & Audit History</h1>
        <p style="color: var(--text-muted); font-size: 0.95rem;">Review patient registrations and consultation histories</p>
    </div>

    <% if (selectedPatient != null && history != null) { %>
        <!-- Specific Patient History Card -->
        <div class="card" style="border-left: 4px solid var(--primary); margin-bottom: 2rem;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                <div>
                    <span class="card-title">Patient Audit File</span>
                    <h3 style="font-size: 1.3rem; font-weight: 700; color: var(--primary);">
                        <%= selectedPatient.getFullName() %> &mdash; <%= selectedPatient.getEmail() %> (<%= selectedPatient.getPhone() %>)
                    </h3>
                </div>
                <a href="${pageContext.request.contextPath}/admin/patients.jsp" class="btn btn-outline">&larr; Back to Patient List</a>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Token #</th>
                            <th>Doctor</th>
                            <th>Department</th>
                            <th>Room</th>
                            <th>Source</th>
                            <th>Status</th>
                            <th>Booked At</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (history.isEmpty()) { %>
                            <tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No appointments found for this patient.</td></tr>
                        <% } else {
                            for (Appointment a : history) { %>
                                <tr>
                                    <td><strong><%= a.getAppointmentDate() %></strong></td>
                                    <td><strong style="color: var(--primary);"><%= a.getTokenNumber() != null ? a.getTokenNumber() : "--" %></strong></td>
                                    <td><%= a.getDoctorName() %></td>
                                    <td><%= a.getDepartmentName() %></td>
                                    <td>Room <%= a.getRoomNumber() != null ? a.getRoomNumber() : "101" %></td>
                                    <td><span class="badge badge-<%= a.getBookingSource().toLowerCase() %>"><%= a.getBookingSource() %></span></td>
                                    <td><span class="badge badge-<%= a.getStatus().toLowerCase() %>"><%= a.getStatus() %></span></td>
                                    <td style="font-size: 0.85rem; color: var(--text-muted);"><%= a.getCreatedAt() %></td>
                                </tr>
                            <% }
                        } %>
                    </tbody>
                </table>
            </div>
        </div>
    <% } %>

    <!-- All Patients Table -->
    <div class="card">
        <h3 style="font-size: 1.2rem; font-weight: 700; margin-bottom: 1rem;">Registered Patients (<%= patients.size() %>)</h3>
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>User ID</th>
                        <th>Full Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Registered Date</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (User u : patients) { %>
                        <tr>
                            <td>#<%= u.getUserId() %></td>
                            <td><strong><%= u.getFullName() %></strong></td>
                            <td><%= u.getEmail() %></td>
                            <td><%= u.getPhone() %></td>
                            <td style="font-size: 0.85rem; color: var(--text-muted);"><%= u.getCreatedAt() %></td>
                            <td>
                                <a href="${pageContext.request.contextPath}/admin/patients.jsp?patientId=<%= u.getUserId() %>" class="btn btn-outline" style="padding: 0.25rem 0.65rem; font-size: 0.8rem;">
                                    View History
                                </a>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
