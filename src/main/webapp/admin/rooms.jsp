<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.RoomDAO, com.hospital.dao.DepartmentDAO, com.hospital.model.Room, com.hospital.model.Department, java.util.List" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Manage Rooms - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    RoomDAO roomDAO = new RoomDAO();
    DepartmentDAO deptDAO = new DepartmentDAO();
    List<Room> rooms = roomDAO.getAllRooms();
    List<Department> departments = deptDAO.getActiveDepartments();

    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>

<main class="main-content">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">Consultation Rooms</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">Configure physical clinic rooms and departmental allocations</p>
        </div>
        <button type="button" class="btn btn-primary" onclick="openAddRoomModal()">+ Add New Room</button>
    </div>

    <% if ("added".equals(success)) { %>
        <div class="alert alert-success">Room created successfully!</div>
    <% } else if ("updated".equals(success)) { %>
        <div class="alert alert-success">Room updated!</div>
    <% } else if (error != null) { %>
        <div class="alert alert-danger"><%= error %></div>
    <% } %>

    <div class="card">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Room ID</th>
                        <th>Room Number</th>
                        <th>Assigned Department</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Room r : rooms) { %>
                        <tr>
                            <td>#<%= r.getRoomId() %></td>
                            <td><strong style="font-size: 1.1rem; color: var(--primary);">Room <%= r.getRoomNumber() %></strong></td>
                            <td><%= r.getDepartmentName() != null ? r.getDepartmentName() : "<em style='color: var(--text-muted);'>Unassigned</em>" %></td>
                            <td>
                                <span class="badge <%= "AVAILABLE".equals(r.getStatus()) ? "badge-completed" : ("OCCUPIED".equals(r.getStatus()) ? "badge-waiting" : "badge-absent") %>">
                                    <%= r.getStatus() %>
                                </span>
                            </td>
                            <td>
                                <button type="button" class="btn btn-outline" style="padding: 0.25rem 0.65rem; font-size: 0.8rem;"
                                        onclick="openEditRoomModal(<%= r.getRoomId() %>, '<%= r.getRoomNumber() %>', '<%= r.getDepartmentId() != null ? r.getDepartmentId() : "" %>', '<%= r.getStatus() %>')">
                                    Edit
                                </button>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Room Modal -->
    <div id="roomModal" class="modal-backdrop">
        <div class="modal">
            <div class="modal-header">
                <h3 id="roomModalTitle" style="font-size: 1.15rem; font-weight: 700;">Add Room</h3>
                <button type="button" style="border: none; background: transparent; font-size: 1.25rem; cursor: pointer;" onclick="closeRoomModal()">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/rooms-action" method="POST">
                <input type="hidden" id="roomAction" name="action" value="add">
                <input type="hidden" id="roomIdInput" name="roomId" value="">

                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label" for="roomNumberInput">Room Number / Identifier</label>
                        <input type="text" id="roomNumberInput" name="roomNumber" class="form-control" placeholder="e.g. 101, 202, OP-1" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="roomDeptSelect">Department</label>
                        <select id="roomDeptSelect" name="departmentId" class="form-control">
                            <option value="">-- None / General Purpose --</option>
                            <% for (Department d : departments) { %>
                                <option value="<%= d.getDepartmentId() %>"><%= d.getDepartmentName() %></option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="roomStatusSelect">Status</label>
                        <select id="roomStatusSelect" name="status" class="form-control">
                            <option value="AVAILABLE">AVAILABLE</option>
                            <option value="OCCUPIED">OCCUPIED</option>
                            <option value="MAINTENANCE">MAINTENANCE</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="closeRoomModal()">Cancel</button>
                    <button type="submit" class="btn btn-primary">Save Room</button>
                </div>
            </form>
        </div>
    </div>
</main>

<script>
function openAddRoomModal() {
    document.getElementById("roomModalTitle").innerText = "Add Room";
    document.getElementById("roomAction").value = "add";
    document.getElementById("roomIdInput").value = "";
    document.getElementById("roomNumberInput").value = "";
    document.getElementById("roomDeptSelect").value = "";
    document.getElementById("roomStatusSelect").value = "AVAILABLE";
    document.getElementById("roomModal").classList.add("active");
}

function openEditRoomModal(id, num, deptId, status) {
    document.getElementById("roomModalTitle").innerText = "Edit Room #" + id;
    document.getElementById("roomAction").value = "update";
    document.getElementById("roomIdInput").value = id;
    document.getElementById("roomNumberInput").value = num;
    document.getElementById("roomDeptSelect").value = deptId;
    document.getElementById("roomStatusSelect").value = status;
    document.getElementById("roomModal").classList.add("active");
}

function closeRoomModal() {
    document.getElementById("roomModal").classList.remove("active");
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
