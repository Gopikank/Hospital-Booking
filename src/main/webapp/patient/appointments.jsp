<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.AppointmentDAO, com.hospital.model.Appointment, java.util.List" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="My Appointments & History - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    Integer patientId = (Integer) session.getAttribute("userId");
    AppointmentDAO apptDAO = new AppointmentDAO();
    List<Appointment> list = (patientId != null) ? apptDAO.getAppointmentsByPatient(patientId) : java.util.Collections.emptyList();
%>

<main class="main-content">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
        <div>
            <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">My Appointment History</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">Complete record of your booked consultation tokens</p>
        </div>
        <a href="${pageContext.request.contextPath}/patient/book-token.jsp" class="btn btn-primary">+ Book Token</a>
    </div>

    <div class="card">
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
                    <% if (list.isEmpty()) { %>
                        <tr>
                            <td colspan="8" style="text-align: center; color: var(--text-muted); padding: 2rem;">
                                No appointments found. Book your first token online today!
                            </td>
                        </tr>
                    <% } else {
                        for (Appointment a : list) { %>
                            <tr>
                                <td><strong><%= a.getAppointmentDate() %></strong></td>
                                <td><span style="font-size: 1.1rem; font-weight: 700; color: var(--primary);"><%= a.getTokenNumber() != null ? a.getTokenNumber() : "--" %></span></td>
                                <td><%= a.getDoctorName() %></td>
                                <td><%= a.getDepartmentName() %></td>
                                <td><%= a.getRoomNumber() != null ? a.getRoomNumber() : "101" %></td>
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
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
