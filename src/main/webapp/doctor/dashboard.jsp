<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.DoctorDAO, com.hospital.model.Doctor, java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Doctor Dashboard - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    Integer userId = (Integer) session.getAttribute("userId");
    DoctorDAO doctorDAO = new DoctorDAO();
    Doctor doc = (userId != null) ? doctorDAO.getDoctorByUserId(userId) : null;
    String todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
%>

<main class="main-content">
    <!-- Doctor Clinic Header Banner -->
    <div class="card" style="background: linear-gradient(135deg, #075985, #0284c7); color: white; margin-bottom: 2rem; padding: 2rem;">
        <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 1rem;">
            <div>
                <span class="badge" style="background: rgba(255,255,255,0.2); color: white; margin-bottom: 0.5rem;">DOCTOR CONSOLE</span>
                <h1 style="font-size: 2rem; font-weight: 800; margin-bottom: 0.35rem;">
                    <%= doc != null ? doc.getDoctorName() : "Doctor" %>
                </h1>
                <div style="font-size: 1.05rem; opacity: 0.95;">
                    <%= doc != null ? doc.getDepartmentName() : "General Clinic" %> &bull;
                    Consultation Room: <strong style="color: #fef08a;"><%= doc != null ? doc.getRoomNumber() : "101" %></strong>
                </div>
            </div>
            <div style="text-align: right;">
                <div style="font-size: 0.85rem; opacity: 0.85;">Date: <%= todayFormatted %></div>
                <div style="font-size: 0.85rem; margin-top: 0.25rem;">
                    Auto-Syncing: <strong id="docLastUpdated" style="color: #fef08a;">Active</strong>
                </div>
            </div>
        </div>
    </div>

    <!-- Statistics Strip -->
    <div class="dashboard-grid">
        <div class="card" style="border-top: 4px solid var(--warning);">
            <div class="card-title">Waiting in Line</div>
            <div class="card-value" id="docWaitingCount" style="color: var(--warning);">0</div>
            <div class="card-subtext">Active patients ready</div>
        </div>

        <div class="card" style="border-top: 4px solid var(--success);">
            <div class="card-title">Completed Consultations</div>
            <div class="card-value" id="docCompletedCount" style="color: var(--success);">0</div>
            <div class="card-subtext">Successfully treated</div>
        </div>

        <div class="card" style="border-top: 4px solid var(--text-muted);">
            <div class="card-title">Marked Absent</div>
            <div class="card-value" id="docAbsentCount" style="color: var(--text-muted);">0</div>
            <div class="card-subtext">Did not report</div>
        </div>

        <div class="card" style="border-top: 4px solid var(--primary);">
            <div class="card-title">Total Booked Today</div>
            <div class="card-value" id="docTotalPatients" style="color: var(--primary);">0</div>
            <div class="card-subtext">Online + Walk-in</div>
        </div>
    </div>

    <!-- Current Serving & Call Controls -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 1.5rem; margin-bottom: 2rem;">
        
        <!-- Currently Serving Card -->
        <div class="card" style="border: 2px solid #bae6fd; background: #f0fdfa;">
            <div class="card-title" style="color: var(--primary-dark);">Currently Serving</div>
            <div id="currentServingContent" style="padding: 1rem 0;">
                <div class="card-value" style="color: var(--text-muted);">Loading...</div>
                <div class="card-subtext">Checking active consultations...</div>
            </div>

            <div class="btn-group" style="margin-top: 1rem; border-top: 1px solid var(--border-color); padding-top: 1rem;">
                <button id="btnStartConsultation" class="btn btn-secondary" style="display: none;" onclick="handleStartConsultation()">
                    &#9654; Start Consultation
                </button>
                <button id="btnCompleteConsultation" class="btn btn-success" style="display: none;" onclick="handleCompleteConsultation()">
                    &#10003; Complete Consultation
                </button>
                <button id="btnMarkAbsent" class="btn btn-outline" style="display: none; color: var(--danger); border-color: var(--danger);" onclick="handleMarkAbsent()">
                    &#10006; Mark Absent
                </button>
            </div>
        </div>

        <!-- Next Action Card -->
        <div class="card" style="display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center; padding: 2rem;">
            <h3 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem;">Queue Action</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem; max-width: 320px;">
                Call the next eligible waiting or checked-in patient into your room.
            </p>
            <button id="btnCallNext" class="btn btn-primary" style="font-size: 1.2rem; padding: 1rem 2.5rem; box-shadow: var(--shadow-md);" onclick="handleCallNext()">
                &#128227; CALL NEXT PATIENT
            </button>
        </div>
    </div>

    <!-- Waiting Patients List -->
    <div class="card">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
            <h3 style="font-size: 1.2rem; font-weight: 700;">Waiting Patients (Queue Order)</h3>
            <span style="font-size: 0.85rem; color: var(--text-muted);">Real-time live queue</span>
        </div>

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
                <tbody id="waitingListBody">
                    <tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">Loading waiting queue...</td></tr>
                </tbody>
            </table>
        </div>
    </div>
</main>

<script>
let activeCurrentServingQueueId = null;

document.addEventListener("DOMContentLoaded", () => {
    // Start AJAX real-time doctor polling every 3s
    QueueManager.startDoctorPolling();
});

async function handleCallNext() {
    const btn = document.getElementById("btnCallNext");
    btn.disabled = true;
    btn.innerText = "Calling...";

    try {
        const resp = await HospitalAJAX.doctorCallNext();
        if (resp && resp.success) {
            alert(resp.message);
            QueueManager.updateDoctorDashboard();
        } else {
            alert(resp ? resp.message : "No waiting patients found.");
        }
    } catch (e) {
        alert("Error calling next patient: " + e.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = "&#128227; CALL NEXT PATIENT";
    }
}

async function handleStartConsultation() {
    const btn = document.getElementById("btnStartConsultation");
    let queueId = btn ? btn.dataset.queueId : null;

    if (!queueId) {
        try {
            const resp = await HospitalAJAX.getDoctorQueue();
            if (resp && resp.data && resp.data.currentServing) {
                queueId = resp.data.currentServing.queueId;
            }
        } catch (ignored) {}
    }

    if (!queueId) {
        alert("No active patient is currently called to start consultation.");
        return;
    }

    btn.disabled = true;
    btn.innerText = "Starting...";

    try {
        const r = await HospitalAJAX.doctorStartConsultation(queueId);
        if (r && r.success) {
            alert(r.message || "Consultation started!");
            await QueueManager.updateDoctorDashboard();
        } else {
            alert(r ? r.message : "Could not start consultation.");
        }
    } catch (e) {
        alert("Error starting consultation: " + e.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = "&#9654; Start Consultation";
    }
}

async function handleCompleteConsultation() {
    const btn = document.getElementById("btnCompleteConsultation");
    let queueId = btn ? btn.dataset.queueId : null;
    let token = btn ? btn.dataset.token : "";

    if (!queueId) {
        try {
            const resp = await HospitalAJAX.getDoctorQueue();
            if (resp && resp.data && resp.data.currentServing) {
                queueId = resp.data.currentServing.queueId;
                token = resp.data.currentServing.tokenNumber;
            }
        } catch (ignored) {}
    }

    if (!queueId) {
        alert("No active patient is currently in consultation.");
        return;
    }

    if (!confirm("Are you sure you want to complete consultation for Token " + (token || queueId) + "?")) {
        return;
    }

    btn.disabled = true;
    btn.innerText = "Completing...";

    try {
        const r = await HospitalAJAX.doctorCompleteConsultation(queueId);
        if (r && r.success) {
            alert(r.message || "Consultation completed successfully!");
            await QueueManager.updateDoctorDashboard();
        } else {
            alert(r ? r.message : "Could not complete consultation.");
        }
    } catch (e) {
        alert("Error completing consultation: " + e.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = "&#10003; Complete Consultation";
    }
}

async function handleMarkAbsent() {
    const btn = document.getElementById("btnMarkAbsent");
    let queueId = btn ? btn.dataset.queueId : null;
    let token = btn ? btn.dataset.token : "";

    if (!queueId) {
        try {
            const resp = await HospitalAJAX.getDoctorQueue();
            if (resp && resp.data && resp.data.currentServing) {
                queueId = resp.data.currentServing.queueId;
                token = resp.data.currentServing.tokenNumber;
            }
        } catch (ignored) {}
    }

    if (!queueId) {
        alert("No active patient is currently called to mark absent.");
        return;
    }

    if (!confirm("Mark patient for Token " + (token || queueId) + " as ABSENT?")) {
        return;
    }

    btn.disabled = true;
    btn.innerText = "Marking Absent...";

    try {
        const r = await HospitalAJAX.doctorMarkAbsent(queueId);
        if (r && r.success) {
            alert(r.message || "Patient marked as absent.");
            await QueueManager.updateDoctorDashboard();
        } else {
            alert(r ? r.message : "Could not mark absent.");
        }
    } catch (e) {
        alert("Error marking absent: " + e.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = "&#10006; Mark Absent";
    }
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
