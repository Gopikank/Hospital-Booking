<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.DoctorDAO, com.hospital.dao.QueueDAO, com.hospital.model.Doctor, com.hospital.model.QueueItem, java.util.List, java.sql.Date, java.time.LocalDate" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Queue Monitor - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    DoctorDAO doctorDAO = new DoctorDAO();
    QueueDAO queueDAO = new QueueDAO();
    List<Doctor> doctors = doctorDAO.getAllDoctors();
    Date today = Date.valueOf(LocalDate.now());
%>

<main class="main-content">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">Live Hospital Queue Monitor</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">Cross-departmental real-time consultation tracker</p>
        </div>
        <button type="button" class="btn btn-outline" onclick="window.location.reload()">&#8635; Refresh All</button>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 1.5rem;">
        <% for (Doctor doc : doctors) {
            List<QueueItem> docQueue = queueDAO.getDailyQueueForDoctor(doc.getDoctorId(), today);
            QueueItem currentServing = null;
            int waiting = 0;
            for (QueueItem q : docQueue) {
                if ("CALLED".equals(q.getStatus()) || "IN_CONSULTATION".equals(q.getStatus())) {
                    currentServing = q;
                } else if ("WAITING".equals(q.getStatus())) {
                    waiting++;
                }
            }
        %>
            <div class="card" style="border-top: 4px solid var(--primary);">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                    <div>
                        <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--primary);"><%= doc.getDoctorName() %></h3>
                        <div style="font-size: 0.85rem; color: var(--text-muted);"><%= doc.getDepartmentName() %> &bull; Room <%= doc.getRoomNumber() != null ? doc.getRoomNumber() : "101" %></div>
                    </div>
                    <span class="badge <%= "AVAILABLE".equals(doc.getStatus()) ? "badge-completed" : "badge-absent" %>">
                        <%= doc.getStatus() %>
                    </span>
                </div>

                <div style="display: flex; gap: 1rem; background: #f8fafc; padding: 1rem; border-radius: var(--radius-sm); margin-bottom: 1rem;">
                    <div style="flex: 1; text-align: center; border-right: 1px solid var(--border-color);">
                        <div class="card-title">Now Serving</div>
                        <div style="font-size: 1.75rem; font-weight: 800; color: var(--secondary);">
                            <%= currentServing != null ? "Token " + currentServing.getTokenNumber() : "--" %>
                        </div>
                    </div>
                    <div style="flex: 1; text-align: center;">
                        <div class="card-title">Waiting</div>
                        <div style="font-size: 1.75rem; font-weight: 800; color: var(--warning);">
                            <%= waiting %>
                        </div>
                    </div>
                </div>

                <!-- Recent Queue Snippet -->
                <div style="font-size: 0.85rem; font-weight: 600; color: var(--text-muted); margin-bottom: 0.35rem;">Today's Tokens:</div>
                <div style="display: flex; flex-wrap: wrap; gap: 0.35rem;">
                    <% if (docQueue.isEmpty()) { %>
                        <span style="color: var(--text-muted); font-size: 0.8rem;">No tokens booked today</span>
                    <% } else {
                        for (QueueItem q : docQueue) { %>
                            <span class="badge badge-<%= q.getStatus().toLowerCase() %>" title="<%= q.getPatientName() %> (<%= q.getBookingSource() %>)">
                                #<%= q.getTokenNumber() %>
                            </span>
                        <% }
                    } %>
                </div>
            </div>
        <% } %>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
