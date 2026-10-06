<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.QueueDAO, com.hospital.model.QueueItem" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Patient Dashboard - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    Integer patientId = (Integer) session.getAttribute("userId");
    String patientName = (String) session.getAttribute("name");

    QueueDAO queueDAO = new QueueDAO();
    QueueItem activeQueue = (patientId != null) ? queueDAO.getTodayPatientQueue(patientId) : null;
    boolean hasToken = (activeQueue != null && !"CANCELLED".equals(activeQueue.getStatus()));
%>

<main class="main-content">
    <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; gap: 1rem;">
        <div>
            <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">
                Welcome, <%= patientName != null ? patientName : "Patient" %>
            </h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Live queue monitoring & online consultation status
            </p>
        </div>
        <div style="display: flex; gap: 0.75rem; align-items: center;">
            <div style="font-size: 0.85rem; color: var(--text-muted);">
                Last Synced: <strong id="lastUpdated" style="color: var(--text-main);">Syncing...</strong>
            </div>
            <a href="${pageContext.request.contextPath}/patient/book-token.jsp" class="btn btn-primary">
                + Book New Token
            </a>
        </div>
    </div>

    <!-- Active Token Container -->
    <div id="activeTokenCard" style="<%= hasToken ? "" : "display: none;" %>">

        <!-- Notification Banner -->
        <div id="notificationBanner" class="notification-banner waiting">
            <div style="display: flex; align-items: center; gap: 0.75rem;">
                <span style="font-size: 1.4rem;">&#128227;</span>
                <span id="notificationText">
                    <%= hasToken ? activeQueue.getNotificationMessage() : "Please wait for queue status..." %>
                </span>
            </div>
            <span id="queueStatusBadge" class="badge <%= hasToken ? "badge-" + activeQueue.getStatus().toLowerCase() : "badge-waiting" %>">
                <%= hasToken ? activeQueue.getStatus() : "WAITING" %>
            </span>
        </div>

        <!-- Appointment Info Strip -->
        <div class="card" style="margin-bottom: 1.5rem; background: #ffffff;">
            <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 1rem;">
                <div>
                    <span class="card-title">Doctor & Department</span>
                    <h3 id="doctorName" style="color: var(--primary); font-size: 1.25rem; font-weight: 700;">
                        <%= hasToken ? activeQueue.getDoctorName() : "" %>
                    </h3>
                    <div style="font-size: 0.9rem; color: var(--text-muted);">
                        Department: <strong id="departmentName"><%= hasToken ? activeQueue.getDepartmentName() : "" %></strong> |
                        Consultation Room: <strong id="roomNumber" style="color: var(--text-main);"><%= hasToken ? activeQueue.getRoomNumber() : "" %></strong>
                    </div>
                </div>

                <div class="btn-group">
                    <button id="btnCheckIn" class="btn btn-success"
                            style="<%= (hasToken && "BOOKED".equals(activeQueue.getStatus())) ? "" : "display:none;" %>"
                            onclick="handleCheckIn()">
                        Check In at Hospital
                    </button>
                    <button id="btnCancelToken" class="btn btn-outline"
                            style="<%= (hasToken && ("BOOKED".equals(activeQueue.getStatus()) || "WAITING".equals(activeQueue.getStatus()))) ? "" : "display:none;" %>"
                            onclick="handleCancelToken()">
                        Cancel Token
                    </button>
                    <a href="${pageContext.request.contextPath}/patient/live-queue.jsp" class="btn btn-outline">
                        View Live Queue
                    </a>
                </div>
            </div>
        </div>

        <!-- KPI 4 Cards Grid Priority: Current Token, Your Token, People Ahead, Estimated Wait -->
        <div class="dashboard-grid">
            <div class="card" style="border-top: 4px solid var(--secondary);">
                <div class="card-title">Current Serving Token</div>
                <div class="card-value" id="currentToken" style="color: var(--secondary);">
                    <%= hasToken ? activeQueue.getCurrentToken() : 0 %>
                </div>
                <div class="card-subtext">Now with doctor</div>
            </div>

            <div class="card" style="border-top: 4px solid var(--primary); background: #f0fdfa;">
                <div class="card-title" style="color: var(--primary-dark);">Your Token Number</div>
                <div class="card-value" id="patientToken" style="color: var(--primary-dark);">
                    <%= hasToken ? activeQueue.getTokenNumber() : 0 %>
                </div>
                <div class="card-subtext">Generated via Online Booking</div>
            </div>

            <div class="card" style="border-top: 4px solid var(--warning);">
                <div class="card-title">People Ahead of You</div>
                <div class="card-value" id="peopleAhead" style="color: var(--warning);">
                    <%= hasToken ? activeQueue.getPeopleAhead() : 0 %>
                </div>
                <div class="card-subtext">Waiting patients ahead</div>
            </div>

            <div class="card" style="border-top: 4px solid var(--info);">
                <div class="card-title">Estimated Waiting Time</div>
                <div class="card-value" id="estimatedWait" style="color: var(--info);">
                    <%= hasToken ? activeQueue.getEstimatedWaitMinutes() : 0 %> mins
                </div>
                <div class="card-subtext">Calculated automatically</div>
            </div>
        </div>

    </div>

    <!-- No Active Token Card -->
    <div id="noActiveTokenCard" class="card" style="text-align: center; padding: 3rem 1.5rem; <%= hasToken ? "display: none;" : "" %>">
        <div style="font-size: 3rem; margin-bottom: 1rem;">&#127973;</div>
        <h3 style="font-size: 1.35rem; font-weight: 700; margin-bottom: 0.5rem;">You Have No Active Token Today</h3>
        <p style="color: var(--text-muted); max-width: 480px; margin: 0 auto 1.5rem;">
            You can book an online appointment token right now to reserve your spot in line without waiting physically at the hospital.
        </p>
        <a href="${pageContext.request.contextPath}/patient/book-token.jsp" class="btn btn-primary" style="padding: 0.75rem 1.5rem;">
            Book an Online Token
        </a>
    </div>

    <!-- Quick Navigation Links -->
    <div style="display: flex; gap: 1rem; margin-top: 2rem;">
        <a href="${pageContext.request.contextPath}/patient/appointments.jsp" class="btn btn-outline">
            &#128197; My Appointment History
        </a>
        <a href="${pageContext.request.contextPath}/patient/profile.jsp" class="btn btn-outline">
            &#9881; My Preferences
        </a>
    </div>
</main>

<script>
let currentQueueId = <%= (hasToken ? activeQueue.getQueueId() : 0) %>;

document.addEventListener("DOMContentLoaded", () => {
    // Start AJAX real-time polling every 3 seconds
    QueueManager.startPatientPolling(currentQueueId > 0 ? currentQueueId : null);
});

async function handleCheckIn() {
    if (!currentQueueId) return;
    if (!confirm("Are you at the hospital and ready to check in to the active waiting queue?")) return;

    try {
        const resp = await HospitalAJAX.checkIn(currentQueueId);
        if (resp && resp.success) {
            alert(resp.message);
            QueueManager.updatePatientDashboard(currentQueueId);
        } else {
            alert(resp ? resp.message : "Check-in failed.");
        }
    } catch (e) {
        alert("Check-in error: " + e.message);
    }
}

async function handleCancelToken() {
    if (!currentQueueId) return;
    if (!confirm("Are you sure you want to cancel your token? This will release your token number for other patients.")) return;

    try {
        const resp = await HospitalAJAX.cancelToken(currentQueueId);
        if (resp && resp.success) {
            alert("Token cancelled successfully.");
            window.location.reload();
        } else {
            alert(resp ? resp.message : "Cancellation failed.");
        }
    } catch (e) {
        alert("Cancellation error: " + e.message);
    }
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
