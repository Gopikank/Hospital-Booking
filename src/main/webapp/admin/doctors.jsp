<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.DoctorDAO, com.hospital.dao.DepartmentDAO, com.hospital.dao.RoomDAO, com.hospital.model.Doctor, com.hospital.model.Department, com.hospital.model.Room, java.util.List" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Manage Doctors - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    DoctorDAO doctorDAO = new DoctorDAO();
    DepartmentDAO deptDAO = new DepartmentDAO();
    RoomDAO roomDAO = new RoomDAO();

    List<Doctor> doctors = doctorDAO.getAllDoctors();
    List<Department> departments = deptDAO.getActiveDepartments();
    List<Room> rooms = roomDAO.getAllRooms();

    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>

<main class="main-content">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">Hospital Medical Staff</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">Manage doctors, clinic assignments, consultation times and availability</p>
        </div>
        <button type="button" class="btn btn-primary" onclick="openAddDoctorModal()">+ Add New Doctor</button>
    </div>

    <% if ("added".equals(success)) { %>
        <div class="alert alert-success">Doctor profile created successfully!</div>
    <% } else if ("updated".equals(success)) { %>
        <div class="alert alert-success">Doctor details updated!</div>
    <% } else if (error != null) { %>
        <div class="alert alert-danger"><%= error %></div>
    <% } %>

    <div class="card">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Doctor Name</th>
                        <th>Department</th>
                        <th>Room</th>
                        <th>Specialization</th>
                        <th>Avg Minutes</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Doctor doc : doctors) { %>
                        <tr>
                            <td>
                                <strong><%= doc.getDoctorName() %></strong><br>
                                <small style="color: var(--text-muted);"><%= doc.getEmail() %></small>
                            </td>
                            <td><%= doc.getDepartmentName() %></td>
                            <td><strong>Room <%= doc.getRoomNumber() != null ? doc.getRoomNumber() : "--" %></strong></td>
                            <td><%= doc.getSpecialization() %></td>
                            <td><%= doc.getAverageConsultationMinutes() %> mins</td>
                            <td>
                                <span class="badge <%= "AVAILABLE".equals(doc.getStatus()) ? "badge-completed" : ("BUSY".equals(doc.getStatus()) ? "badge-waiting" : "badge-absent") %>">
                                    <%= doc.getStatus() %>
                                </span>
                            </td>
                            <td>
                                <button type="button" class="btn btn-outline" style="padding: 0.25rem 0.65rem; font-size: 0.8rem;"
                                        onclick="openEditDoctorModal(<%= doc.getDoctorId() %>, <%= doc.getDepartmentId() %>, '<%= doc.getRoomId() != null ? doc.getRoomId() : "" %>', '<%= doc.getSpecialization() %>', <%= doc.getAverageConsultationMinutes() %>, '<%= doc.getStatus() %>')">
                                    Edit
                                </button>
                                <button type="button" class="btn btn-outline" style="padding: 0.25rem 0.65rem; font-size: 0.8rem;"
                                        onclick="toggleDoctorStatus(<%= doc.getDoctorId() %>, '<%= "AVAILABLE".equals(doc.getStatus()) ? "OFFLINE" : "AVAILABLE" %>')">
                                    <%= "AVAILABLE".equals(doc.getStatus()) ? "Go Offline" : "Set Available" %>
                                </button>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Add Doctor Modal -->
    <div id="addDoctorModal" class="modal-backdrop">
        <div class="modal" style="max-width: 580px;">
            <div class="modal-header">
                <h3 style="font-size: 1.15rem; font-weight: 700;">Add New Doctor</h3>
                <button type="button" style="border: none; background: transparent; font-size: 1.25rem; cursor: pointer;" onclick="closeModals()">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/doctors-action" method="POST">
                <input type="hidden" name="action" value="add">
                <div class="modal-body">
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="addDocName">Full Name</label>
                            <input type="text" id="addDocName" name="fullName" class="form-control" placeholder="Dr. Jane Doe" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="addDocEmail">Email</label>
                            <input type="email" id="addDocEmail" name="email" class="form-control" placeholder="doctor@hospital.com" required>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="addDocPhone">Phone</label>
                            <input type="tel" id="addDocPhone" name="phone" class="form-control" placeholder="Mobile Number" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="addDocPassword">Password</label>
                            <input type="password" id="addDocPassword" name="password" class="form-control" placeholder="••••••••" required>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="addDocDept">Department</label>
                            <select id="addDocDept" name="departmentId" class="form-control" required>
                                <% for (Department d : departments) { %>
                                    <option value="<%= d.getDepartmentId() %>"><%= d.getDepartmentName() %></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="addDocRoom">Assigned Room</label>
                            <select id="addDocRoom" name="roomId" class="form-control">
                                <option value="">-- None --</option>
                                <% for (Room r : rooms) { %>
                                    <option value="<%= r.getRoomId() %>">Room <%= r.getRoomNumber() %></option>
                                <% } %>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="addDocSpec">Specialization</label>
                            <input type="text" id="addDocSpec" name="specialization" class="form-control" placeholder="e.g. Cardiologist">
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="addDocAvg">Avg Consultation (min)</label>
                            <input type="number" id="addDocAvg" name="averageMinutes" class="form-control" value="5" min="1" max="120" required>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="closeModals()">Cancel</button>
                    <button type="submit" class="btn btn-primary">Create Doctor Profile</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Edit Doctor Modal -->
    <div id="editDoctorModal" class="modal-backdrop">
        <div class="modal" style="max-width: 540px;">
            <div class="modal-header">
                <h3 style="font-size: 1.15rem; font-weight: 700;">Edit Doctor Configuration</h3>
                <button type="button" style="border: none; background: transparent; font-size: 1.25rem; cursor: pointer;" onclick="closeModals()">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/doctors-action" method="POST">
                <input type="hidden" name="action" value="update">
                <input type="hidden" id="editDoctorId" name="doctorId" value="">

                <div class="modal-body">
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="editDocDept">Department</label>
                            <select id="editDocDept" name="departmentId" class="form-control" required>
                                <% for (Department d : departments) { %>
                                    <option value="<%= d.getDepartmentId() %>"><%= d.getDepartmentName() %></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="editDocRoom">Assigned Room</label>
                            <select id="editDocRoom" name="roomId" class="form-control">
                                <option value="">-- None --</option>
                                <% for (Room r : rooms) { %>
                                    <option value="<%= r.getRoomId() %>">Room <%= r.getRoomNumber() %></option>
                                <% } %>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="editDocSpec">Specialization</label>
                            <input type="text" id="editDocSpec" name="specialization" class="form-control">
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="editDocAvg">Avg Consultation (min)</label>
                            <input type="number" id="editDocAvg" name="averageMinutes" class="form-control" min="1" max="120" required>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="editDocStatus">Status</label>
                        <select id="editDocStatus" name="status" class="form-control">
                            <option value="AVAILABLE">AVAILABLE</option>
                            <option value="BUSY">BUSY</option>
                            <option value="OFFLINE">OFFLINE</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="closeModals()">Cancel</button>
                    <button type="submit" class="btn btn-primary">Save Changes</button>
                </div>
            </form>
        </div>
    </div>
</main>

<script>
function openAddDoctorModal() {
    document.getElementById("addDoctorModal").classList.add("active");
}

function openEditDoctorModal(id, deptId, roomId, spec, avg, status) {
    document.getElementById("editDoctorId").value = id;
    document.getElementById("editDocDept").value = deptId;
    document.getElementById("editDocRoom").value = roomId;
    document.getElementById("editDocSpec").value = spec;
    document.getElementById("editDocAvg").value = avg;
    document.getElementById("editDocStatus").value = status;
    document.getElementById("editDoctorModal").classList.add("active");
}

function closeModals() {
    document.getElementById("addDoctorModal").classList.remove("active");
    document.getElementById("editDoctorModal").classList.remove("active");
}

async function toggleDoctorStatus(docId, newStatus) {
    try {
        const formData = new URLSearchParams();
        formData.append("action", "status");
        formData.append("doctorId", docId);
        formData.append("status", newStatus);

        const resp = await fetch(contextPath + "/admin/doctors-action", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData.toString()
        });
        window.location.reload();
    } catch (e) {
        alert("Error: " + e.message);
    }
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
