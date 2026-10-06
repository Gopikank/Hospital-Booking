<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.DepartmentDAO, com.hospital.dao.DoctorDAO, com.hospital.model.Department, com.hospital.model.Doctor, com.hospital.util.CookieUtil, java.util.List, java.time.LocalDate" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Online Token Booking - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    DepartmentDAO deptDAO = new DepartmentDAO();
    DoctorDAO docDAO = new DoctorDAO();
    List<Department> departments = deptDAO.getActiveDepartments();

    // Read preference cookies
    String prefDept = CookieUtil.getCookieValue(request, "preferredDepartment");
    String prefDocId = CookieUtil.getCookieValue(request, "preferredDoctorId");
    String todayStr = LocalDate.now().toString();
%>

<main class="main-content" style="max-width: 860px;">
    <div style="margin-bottom: 2rem;">
        <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">Online Token Booking</h1>
        <p style="color: var(--text-muted); font-size: 0.95rem;">
            Reserve your consultation token number online from home before visiting the hospital
        </p>
    </div>

    <!-- Booking Form Card -->
    <div class="card" style="margin-bottom: 2rem;">
        <div class="form-row">
            <!-- 1. Select Department -->
            <div class="form-group">
                <label class="form-label" for="departmentSelect">1. Select Department</label>
                <select id="departmentSelect" class="form-control" onchange="onDepartmentChanged()">
                    <option value="">-- Choose Department --</option>
                    <% for (Department d : departments) {
                        boolean isSelected = prefDept != null && prefDept.equalsIgnoreCase(d.getDepartmentName());
                    %>
                        <option value="<%= d.getDepartmentId() %>" data-name="<%= d.getDepartmentName() %>" <%= isSelected ? "selected" : "" %>>
                            <%= d.getDepartmentName() %>
                        </option>
                    <% } %>
                </select>
                <% if (prefDept != null) { %>
                    <small style="color: var(--secondary); font-size: 0.75rem;">&#10003; Preselected from your saved preference</small>
                <% } %>
            </div>

            <!-- 2. Select Doctor -->
            <div class="form-group">
                <label class="form-label" for="doctorSelect">2. Select Doctor</label>
                <select id="doctorSelect" class="form-control" onchange="loadAvailableTokens()">
                    <option value="">-- Choose Doctor --</option>
                </select>
            </div>

            <!-- 3. Select Date -->
            <div class="form-group">
                <label class="form-label" for="appointmentDate">3. Consultation Date</label>
                <input type="date" id="appointmentDate" class="form-control"
                       value="<%= todayStr %>" min="<%= todayStr %>" onchange="loadAvailableTokens()">
            </div>
        </div>

        <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 0.5rem;">
            <label style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.85rem; cursor: pointer;">
                <input type="checkbox" id="rememberPreference" checked>
                Remember my department & doctor selection
            </label>
            <button type="button" class="btn btn-outline" style="padding: 0.4rem 0.85rem; font-size: 0.85rem;" onclick="loadAvailableTokens()">
                Refresh Availability
            </button>
        </div>
    </div>

    <!-- Doctor Info & Current Status Banner -->
    <div id="doctorStatusBanner" class="card" style="display: none; margin-bottom: 2rem; background: #f8fafc;">
        <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 1rem;">
            <div>
                <span class="card-title">Doctor Details</span>
                <h3 id="bannerDoctorName" style="color: var(--primary); font-size: 1.2rem; font-weight: 700;"></h3>
                <div style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.25rem;">
                    Room: <strong id="bannerRoomNumber"></strong> &bull;
                    Avg Consultation: <strong id="bannerAvgTime"></strong> mins &bull;
                    Status: <span id="bannerDoctorStatus" class="badge badge-completed">AVAILABLE</span>
                </div>
            </div>
            <div style="text-align: right;">
                <div class="card-title">Current Serving Token</div>
                <div id="bannerCurrentToken" style="font-size: 1.85rem; font-weight: 800; color: var(--secondary);">0</div>
            </div>
        </div>
    </div>

    <!-- Available Tokens Selection Area -->
    <div id="tokensArea" class="card" style="display: none;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
            <div>
                <h3 style="font-size: 1.15rem; font-weight: 700;">Available Token Numbers</h3>
                <p style="color: var(--text-muted); font-size: 0.85rem;">Select your desired token number for consultation</p>
            </div>
            <div>
                Available Slots: <strong id="availableCount" style="color: var(--primary);">0</strong>
            </div>
        </div>

        <div id="tokenGrid" class="token-grid">
            <!-- Dynamically populated via AJAX Fetch -->
        </div>

        <!-- Selected Token Action Panel -->
        <div id="selectedActionPanel" style="display: none; border-top: 1px solid var(--border-color); padding-top: 1.25rem; margin-top: 1rem;">
            <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 1rem;">
                <div>
                    Selected Token: <strong id="selectedTokenDisplay" style="font-size: 1.5rem; color: var(--primary); margin-left: 0.35rem;">None</strong>
                </div>
                <button type="button" class="btn btn-primary" style="padding: 0.75rem 2rem; font-size: 1rem;" onclick="showConfirmationModal()">
                    Book Token Online
                </button>
            </div>
        </div>
    </div>

    <!-- Loading Spinner / Message -->
    <div id="tokensLoading" style="display: none; text-align: center; padding: 2rem; color: var(--text-muted);">
        Loading available tokens from server...
    </div>

    <!-- Confirmation Modal -->
    <div id="confirmationModal" class="modal-backdrop">
        <div class="modal">
            <div class="modal-header">
                <h3 style="font-size: 1.15rem; font-weight: 700;">Token Booking Confirmation</h3>
                <button type="button" style="border: none; background: transparent; font-size: 1.25rem; cursor: pointer;" onclick="closeConfirmationModal()">&times;</button>
            </div>
            <div class="modal-body">
                <div class="token-card" style="padding: 1.25rem; margin-bottom: 1.25rem;">
                    <span class="card-title">Token Number</span>
                    <div class="token-number-hero" id="modalTokenNum">0</div>
                    <span class="badge badge-booked">STATUS: BOOKED ONLINE</span>
                </div>

                <table class="table" style="font-size: 0.9rem;">
                    <tr>
                        <th style="width: 40%;">Doctor:</th>
                        <td id="modalDoctorName"></td>
                    </tr>
                    <tr>
                        <th>Department:</th>
                        <td id="modalDepartmentName"></td>
                    </tr>
                    <tr>
                        <th>Room Number:</th>
                        <td id="modalRoomNumber"></td>
                    </tr>
                    <tr>
                        <th>Appointment Date:</th>
                        <td id="modalDate"></td>
                    </tr>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline" onclick="closeConfirmationModal()">Change Token</button>
                <button type="button" class="btn btn-success" id="btnConfirmBooking" onclick="submitBooking()">Confirm & Book</button>
            </div>
        </div>
    </div>
</main>

<script>
let selectedToken = null;
let currentDoctorData = null;
const prefDocId = "<%= prefDocId != null ? prefDocId : "" %>";

document.addEventListener("DOMContentLoaded", () => {
    const deptSelect = document.getElementById("departmentSelect");
    if (deptSelect.value) {
        onDepartmentChanged();
    }
});

async function onDepartmentChanged() {
    const deptId = document.getElementById("departmentSelect").value;
    const docSelect = document.getElementById("doctorSelect");
    docSelect.innerHTML = '<option value="">-- Choose Doctor --</option>';

    document.getElementById("doctorStatusBanner").style.display = "none";
    document.getElementById("tokensArea").style.display = "none";
    document.getElementById("selectedActionPanel").style.display = "none";
    selectedToken = null;

    if (!deptId) return;

    try {
        const resp = await HospitalAJAX.request(contextPath + '/api/doctors?departmentId=' + deptId);
        if (resp && resp.success && resp.data) {
            if (resp.data.length === 0) {
                docSelect.innerHTML = '<option value="">-- No doctors currently in this department --</option>';
            } else {
                resp.data.forEach(doc => {
                    const opt = document.createElement("option");
                    opt.value = doc.doctorId;
                    opt.textContent = doc.doctorName + " (" + doc.specialization + ")";
                    if (prefDocId && prefDocId == doc.doctorId) {
                        opt.selected = true;
                    }
                    docSelect.appendChild(opt);
                });
            }

            if (docSelect.value) {
                loadAvailableTokens();
            }
        }
    } catch (e) {
        console.error("Error loading doctors:", e);
    }
}

async function loadAvailableTokens() {
    const docId = document.getElementById("doctorSelect").value;
    const date = document.getElementById("appointmentDate").value;

    if (!docId || !date) {
        document.getElementById("doctorStatusBanner").style.display = "none";
        document.getElementById("tokensArea").style.display = "none";
        return;
    }

    const loader = document.getElementById("tokensLoading");
    loader.style.display = "block";
    document.getElementById("tokensArea").style.display = "none";
    document.getElementById("selectedActionPanel").style.display = "none";
    selectedToken = null;

    try {
        const resp = await HospitalAJAX.getAvailableTokens(docId, date);
        loader.style.display = "none";

        if (resp && resp.success && resp.data) {
            currentDoctorData = resp.data;

            // Update Doctor Status Banner
            document.getElementById("bannerDoctorName").innerText = resp.data.doctorName;
            document.getElementById("bannerRoomNumber").innerText = resp.data.roomNumber || "101";
            document.getElementById("bannerAvgTime").innerText = resp.data.averageMinutes;
            document.getElementById("bannerDoctorStatus").innerText = resp.data.doctorStatus;

            // Fetch current serving token for this doctor
            const qResp = await HospitalAJAX.getQueueStatus(null, docId);
            if (qResp && qResp.success && qResp.data) {
                document.getElementById("bannerCurrentToken").innerText = qResp.data.currentToken || 0;
            }

            document.getElementById("doctorStatusBanner").style.display = "block";

            // Render Tokens Grid
            const grid = document.getElementById("tokenGrid");
            grid.innerHTML = "";
            const tokens = resp.data.availableTokens || [];
            document.getElementById("availableCount").innerText = tokens.length;

            if (tokens.length === 0) {
                grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--danger); padding: 1.5rem;">
                    Daily token capacity reached or no slots available for this doctor on this date.
                </div>`;
            } else {
                tokens.forEach(tok => {
                    const btn = document.createElement("button");
                    btn.type = "button";
                    btn.className = "token-btn";
                    btn.innerText = tok;
                    btn.onclick = () => selectToken(tok, btn);
                    grid.appendChild(btn);
                });
            }

            document.getElementById("tokensArea").style.display = "block";
        } else {
            alert(resp ? resp.message : "Failed to load tokens");
        }
    } catch (e) {
        loader.style.display = "none";
        alert("Error loading tokens: " + e.message);
    }
}

function selectToken(tok, btn) {
    selectedToken = tok;
    document.querySelectorAll(".token-btn").forEach(b => b.classList.remove("selected"));
    btn.classList.add("selected");
    document.getElementById("selectedTokenDisplay").innerText = tok;
    document.getElementById("selectedActionPanel").style.display = "block";
}

function showConfirmationModal() {
    if (!selectedToken || !currentDoctorData) return;

    const deptSelect = document.getElementById("departmentSelect");
    const deptName = deptSelect.options[deptSelect.selectedIndex].getAttribute("data-name") || "General Medicine";

    document.getElementById("modalTokenNum").innerText = selectedToken;
    document.getElementById("modalDoctorName").innerText = currentDoctorData.doctorName;
    document.getElementById("modalDepartmentName").innerText = deptName;
    document.getElementById("modalRoomNumber").innerText = currentDoctorData.roomNumber || "101";
    document.getElementById("modalDate").innerText = document.getElementById("appointmentDate").value;

    document.getElementById("confirmationModal").classList.add("active");
}

function closeConfirmationModal() {
    document.getElementById("confirmationModal").classList.remove("active");
}

async function submitBooking() {
    if (!selectedToken || !currentDoctorData) return;

    const btnConfirm = document.getElementById("btnConfirmBooking");
    btnConfirm.disabled = true;
    btnConfirm.innerText = "Booking...";

    const deptSelect = document.getElementById("departmentSelect");
    const deptName = deptSelect.options[deptSelect.selectedIndex].getAttribute("data-name");
    const remember = document.getElementById("rememberPreference").checked;
    const date = document.getElementById("appointmentDate").value;

    try {
        const resp = await HospitalAJAX.bookToken(
            currentDoctorData.doctorId,
            date,
            selectedToken,
            deptName,
            remember
        );

        if (resp && resp.success) {
            closeConfirmationModal();
            alert("Token " + selectedToken + " booked successfully!");
            window.location.href = contextPath + "/patient/dashboard.jsp";
        } else {
            alert(resp ? resp.message : "Booking failed.");
            btnConfirm.disabled = false;
            btnConfirm.innerText = "Confirm & Book";
        }
    } catch (e) {
        alert("Booking error: " + e.message);
        btnConfirm.disabled = false;
        btnConfirm.innerText = "Confirm & Book";
    }
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
