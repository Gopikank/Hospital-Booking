<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.DoctorDAO, com.hospital.dao.QueueDAO, com.hospital.model.Doctor, com.hospital.model.QueueItem, java.util.List, java.sql.Date, java.time.LocalDate" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Daily Clinic Queue - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    Integer userId = (Integer) session.getAttribute("userId");
    DoctorDAO doctorDAO = new DoctorDAO();
    QueueDAO queueDAO = new QueueDAO();
    Doctor doc = (userId != null) ? doctorDAO.getDoctorByUserId(userId) : null;
    Date today = Date.valueOf(LocalDate.now());
    List<QueueItem> queueList = (doc != null) ? queueDAO.getDailyQueueForDoctor(doc.getDoctorId(), today) : java.util.Collections.emptyList();
%>

<main class="main-content">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
        <div>
            <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">Today's Complete Queue Schedule</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                <%= doc != null ? doc.getDoctorName() : "Doctor" %> &bull; <%= today %>
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/doctor/dashboard.jsp" class="btn btn-outline">&larr; Back to Doctor Console</a>
    </div>

    <div class="card">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Token #</th>
                        <th>Patient Name</th>
                        <th>Phone</th>
                        <th>Source</th>
                        <th>Status</th>
                        <th>Booked At</th>
                        <th>Checked-In At</th>
                        <th>Called At</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (queueList.isEmpty()) { %>
                        <tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 2rem;">No appointments scheduled for today.</td></tr>
                    <% } else {
                        for (QueueItem q : queueList) { %>
                            <tr>
                                <td><strong style="color: var(--primary); font-size: 1.1rem;"><%= q.getTokenNumber() %></strong></td>
                                <td><strong><%= q.getPatientName() %></strong></td>
                                <td><%= q.getPatientPhone() != null ? q.getPatientPhone() : "--" %></td>
                                <td><span class="badge badge-<%= q.getBookingSource().toLowerCase() %>"><%= q.getBookingSource() %></span></td>
                                <td><span class="badge badge-<%= q.getStatus().toLowerCase() %>"><%= q.getStatus() %></span></td>
                                <td style="font-size: 0.85rem; color: var(--text-muted);"><%= q.getCreatedAt() %></td>
                                <td style="font-size: 0.85rem; color: var(--text-muted);"><%= q.getCheckedInAt() != null ? q.getCheckedInAt() : "--" %></td>
                                <td style="font-size: 0.85rem; color: var(--text-muted);"><%= q.getCalledAt() != null ? q.getCalledAt() : "--" %></td>
                            </tr>
                        <% }
                    } %>
                </tbody>
            </table>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
