<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Admin Dashboard - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<main class="main-content">
    <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; margin-bottom: 2rem; gap: 1rem;">
        <div>
            <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">Hospital Administration</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">Overall operations, daily metrics & active clinic queues</p>
        </div>
        <div class="btn-group">
            <a href="${pageContext.request.contextPath}/admin/queue-monitor.jsp" class="btn btn-primary">&#128269; Monitor All Queues</a>
            <a href="${pageContext.request.contextPath}/admin/xml-config.jsp" class="btn btn-outline">&#128196; XML Configuration</a>
        </div>
    </div>

    <!-- Big KPI Grid (Section 19 requirements) -->
    <div class="dashboard-grid">
        <div class="card" style="border-top: 4px solid var(--primary);">
            <div class="card-title">Total Patients Today</div>
            <div class="card-value" id="statTotalPatients">0</div>
            <div class="card-subtext">All hospital registrations</div>
        </div>

        <div class="card" style="border-top: 4px solid #8b5cf6;">
            <div class="card-title">Online Bookings</div>
            <div class="card-value" id="statOnlineBookings" style="color: #8b5cf6;">0</div>
            <div class="card-subtext">Booked via portal</div>
        </div>

        <div class="card" style="border-top: 4px solid #ea580c;">
            <div class="card-title">Walk-in Tokens</div>
            <div class="card-value" id="statWalkinTokens" style="color: #ea580c;">0</div>
            <div class="card-subtext">Front-desk generated</div>
        </div>

        <div class="card" style="border-top: 4px solid var(--warning);">
            <div class="card-title">Waiting in Queues</div>
            <div class="card-value" id="statWaiting" style="color: var(--warning);">0</div>
            <div class="card-subtext">Ready for consultation</div>
        </div>

        <div class="card" style="border-top: 4px solid var(--secondary);">
            <div class="card-title">In Consultation</div>
            <div class="card-value" id="statInConsultation" style="color: var(--secondary);">0</div>
            <div class="card-subtext">With doctors right now</div>
        </div>

        <div class="card" style="border-top: 4px solid var(--success);">
            <div class="card-title">Completed Today</div>
            <div class="card-value" id="statCompleted" style="color: var(--success);">0</div>
            <div class="card-subtext">Consultations concluded</div>
        </div>

        <div class="card" style="border-top: 4px solid var(--danger);">
            <div class="card-title">Cancelled Tokens</div>
            <div class="card-value" id="statCancelled" style="color: var(--danger);">0</div>
            <div class="card-subtext">Revoked by patient</div>
        </div>

        <div class="card" style="border-top: 4px solid #64748b;">
            <div class="card-title">Absent Patients</div>
            <div class="card-value" id="statAbsent" style="color: #64748b;">0</div>
            <div class="card-subtext">Did not show up</div>
        </div>
    </div>

    <!-- Department-wise Queue Breakdown Table -->
    <div class="card" style="margin-bottom: 2rem;">
        <h3 style="font-size: 1.2rem; font-weight: 700; margin-bottom: 1rem;">Department-wise Queue Load</h3>
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Department</th>
                        <th>Waiting Patients</th>
                        <th>Total Registered Today</th>
                    </tr>
                </thead>
                <tbody id="adminDeptStatsBody">
                    <tr><td colspan="3" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">Loading department loads...</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Quick Navigation Hub -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem;">
        <a href="${pageContext.request.contextPath}/admin/doctors.jsp" class="card" style="text-decoration: none; color: inherit; text-align: center;">
            <div style="font-size: 2rem; margin-bottom: 0.5rem;">&#128104;&#8205;&#9877;&#65039;</div>
            <strong>Manage Doctors</strong>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">Profiles, schedules & clinic assignment</div>
        </a>

        <a href="${pageContext.request.contextPath}/admin/departments.jsp" class="card" style="text-decoration: none; color: inherit; text-align: center;">
            <div style="font-size: 2rem; margin-bottom: 0.5rem;">&#127973;</div>
            <strong>Manage Departments</strong>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">Add/edit medical departments</div>
        </a>

        <a href="${pageContext.request.contextPath}/admin/rooms.jsp" class="card" style="text-decoration: none; color: inherit; text-align: center;">
            <div style="font-size: 2rem; margin-bottom: 0.5rem;">&#128716;</div>
            <strong>Manage Rooms</strong>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">Consultation rooms & availability</div>
        </a>

        <a href="${pageContext.request.contextPath}/admin/patients.jsp" class="card" style="text-decoration: none; color: inherit; text-align: center;">
            <div style="font-size: 2rem; margin-bottom: 0.5rem;">&#128101;</div>
            <strong>View Patients</strong>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">Registered patient list & audit</div>
        </a>
    </div>
</main>

<script>
document.addEventListener("DOMContentLoaded", () => {
    // Start AJAX admin polling
    QueueManager.startAdminPolling();
});
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
