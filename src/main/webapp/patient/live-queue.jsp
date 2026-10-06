<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.DoctorDAO, com.hospital.model.Doctor, java.util.List" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Live Hospital Queue - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    DoctorDAO doctorDAO = new DoctorDAO();
    List<Doctor> doctors = doctorDAO.getAllDoctors();
    Integer sessionUserId = (Integer) session.getAttribute("userId");
%>

<main class="main-content" style="max-width: 960px;">
    <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; gap: 1rem;">
        <div>
            <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">Live Consultation Queue</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">Real-time hospital display screen updating every 3 seconds</p>
        </div>
        <div>
            <span style="font-size: 0.85rem; color: var(--text-muted);">Last Updated: </span>
            <strong id="liveQueueTimestamp" style="color: var(--primary);">Connecting...</strong>
        </div>
    </div>

    <!-- Doctor Filter -->
    <div class="card" style="margin-bottom: 2rem;">
        <div style="display: flex; flex-wrap: wrap; align-items: center; gap: 1rem;">
            <label style="font-weight: 600; min-width: 120px;">Select Clinic:</label>
            <select id="doctorFilter" class="form-control" style="max-width: 400px;" onchange="refreshLiveQueue()">
                <% if (doctors != null) {
                    for (Doctor doc : doctors) { %>
                    <option value="<%= doc.getDoctorId() %>">
                        <%= doc.getDoctorName() %> &mdash; <%= doc.getDepartmentName() %> (Room <%= doc.getRoomNumber() != null ? doc.getRoomNumber() : "101" %>)
                    </option>
                <%  }
                   } %>
            </select>
        </div>
    </div>

    <!-- Clinic Header & Big Serving Board -->
    <div class="card" style="text-align: center; border-top: 6px solid var(--primary); margin-bottom: 2rem; padding: 2.5rem 1.5rem;">
        <div id="liveDeptAndDoc" style="font-size: 1.25rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">
            GENERAL MEDICINE
        </div>
        <div id="liveDoctorName" style="font-size: 1.5rem; font-weight: 800; color: var(--primary); margin-bottom: 1.5rem;">
            Dr. Kumar | Room 101
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem; max-width: 600px; margin: 0 auto 1.5rem;">
            <div style="background: #f0fdf4; border: 2px solid #86efac; border-radius: var(--radius-md); padding: 1.25rem;">
                <div class="card-title" style="color: #15803d;">NOW SERVING</div>
                <div class="token-number-hero" id="nowServingToken" style="color: #15803d; font-size: 3.5rem;">0</div>
            </div>

            <div style="background: #eff6ff; border: 2px solid #93c5fd; border-radius: var(--radius-md); padding: 1.25rem;">
                <div class="card-title" style="color: #1d4ed8;">NEXT IN LINE</div>
                <div class="token-number-hero" id="nextInLineToken" style="color: #1d4ed8; font-size: 3.5rem;">--</div>
            </div>
        </div>

        <!-- If logged-in patient has a token in this queue -->
        <div id="patientStatusCallout" style="display: none; max-width: 600px; margin: 0 auto; background: var(--warning-light); border: 1px solid #fde68a; padding: 1rem; border-radius: var(--radius-sm); font-weight: 600;">
            Your Token: <span id="calloutYourToken" style="color: var(--primary); font-size: 1.2rem;">0</span> |
            People Ahead: <span id="calloutPeopleAhead" style="color: var(--warning); font-size: 1.2rem;">0</span>
        </div>
    </div>

    <!-- Active Queue Table -->
    <div class="card">
        <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 1rem;">Upcoming Queue Order</h3>
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Token #</th>
                        <th>Patient Name</th>
                        <th>Booking Source</th>
                        <th>Status</th>
                        <th>Queue Position</th>
                    </tr>
                </thead>
                <tbody id="liveQueueTableBody">
                    <tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2rem;">Loading live queue data...</td></tr>
                </tbody>
            </table>
        </div>
    </div>
</main>

<script>
let liveQueueInterval = null;
const currentUserId = <%= sessionUserId != null ? sessionUserId : 0 %>;

document.addEventListener("DOMContentLoaded", () => {
    refreshLiveQueue();
    liveQueueInterval = setInterval(refreshLiveQueue, 3000);
});

async function refreshLiveQueue() {
    const docId = document.getElementById("doctorFilter").value;
    if (!docId) return;

    try {
        const resp = await HospitalAJAX.getQueueStatus(null, docId);
        if (resp && resp.success && resp.data) {
            const d = resp.data;

            // DOM Updates
            document.getElementById("liveQueueTimestamp").innerText = d.lastUpdated;
            document.getElementById("liveDeptAndDoc").innerText = (d.departmentName || "").toUpperCase();
            document.getElementById("liveDoctorName").innerText = d.doctorName + " | Room " + (d.roomNumber || "101");
            document.getElementById("nowServingToken").innerText = d.currentToken || 0;

            const items = d.dailyQueue || [];
            let nextToken = "--";
            let queueHtml = "";
            let waitingCount = 0;
            let patientHasTokenInQueue = false;

            items.forEach(item => {
                if (item.status === 'WAITING') {
                    waitingCount++;
                    if (nextToken === "--") {
                        nextToken = item.tokenNumber;
                    }
                }

                if (currentUserId > 0 && item.patientId === currentUserId && (item.status === 'WAITING' || item.status === 'CALLED' || item.status === 'BOOKED')) {
                    patientHasTokenInQueue = true;
                    document.getElementById("calloutYourToken").innerText = item.tokenNumber;
                    document.getElementById("calloutPeopleAhead").innerText = item.peopleAhead;
                }

                // Show only active/upcoming
                if (item.status === 'WAITING' || item.status === 'CALLED' || item.status === 'IN_CONSULTATION') {
                    const isSelf = (currentUserId > 0 && item.patientId === currentUserId);
                    const rowStyle = isSelf ? 'background: #f0fdfa; font-weight: 700;' : '';
                    const srcClass = (item.bookingSource || '').toLowerCase();
                    const statusClass = (item.status || '').toLowerCase();
                    const posDisplay = item.status === 'CALLED' ? '<span style="color: var(--success); font-weight: bold;">WITH DOCTOR</span>' : (waitingCount + ' in line');

                    queueHtml += '<tr style="' + rowStyle + '">' +
                        '<td><strong style="font-size: 1.1rem; color: var(--primary);">' + item.tokenNumber + '</strong></td>' +
                        '<td>' + item.patientName + '</td>' +
                        '<td><span class="badge badge-' + srcClass + '">' + item.bookingSource + '</span></td>' +
                        '<td><span class="badge badge-' + statusClass + '">' + item.status + '</span></td>' +
                        '<td>' + posDisplay + '</td>' +
                        '</tr>';
                }
            });

            document.getElementById("nextInLineToken").innerText = nextToken;
            document.getElementById("patientStatusCallout").style.display = patientHasTokenInQueue ? "block" : "none";

            const tbody = document.getElementById("liveQueueTableBody");
            if (queueHtml === "") {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2rem;">No patients currently in line for this clinic.</td></tr>`;
            } else {
                tbody.innerHTML = queueHtml;
            }
        }
    } catch (e) {
        console.error("Live queue fetch error:", e);
    }
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
