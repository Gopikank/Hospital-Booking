<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.DepartmentDAO, com.hospital.model.Department, java.util.List" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="Manage Departments - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    DepartmentDAO deptDAO = new DepartmentDAO();
    List<Department> list = deptDAO.getAllDepartments();
    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>

<main class="main-content">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">Hospital Departments</h1>
            <p style="color: var(--text-muted); font-size: 0.95rem;">Configure hospital medical divisions and consultation units</p>
        </div>
        <button type="button" class="btn btn-primary" onclick="openAddModal()">+ Add New Department</button>
    </div>

    <% if ("added".equals(success)) { %>
        <div class="alert alert-success">Department added successfully!</div>
    <% } else if ("updated".equals(success)) { %>
        <div class="alert alert-success">Department details updated!</div>
    <% } else if (error != null) { %>
        <div class="alert alert-danger"><%= error %></div>
    <% } %>

    <div class="card">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Department Name</th>
                        <th>Description</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Department d : list) { %>
                        <tr>
                            <td>#<%= d.getDepartmentId() %></td>
                            <td><strong><%= d.getDepartmentName() %></strong></td>
                            <td><%= d.getDescription() %></td>
                            <td>
                                <span class="badge <%= "ACTIVE".equals(d.getStatus()) ? "badge-completed" : "badge-absent" %>">
                                    <%= d.getStatus() %>
                                </span>
                            </td>
                            <td>
                                <button type="button" class="btn btn-outline" style="padding: 0.25rem 0.65rem; font-size: 0.8rem;"
                                        onclick="openEditModal(<%= d.getDepartmentId() %>, '<%= d.getDepartmentName() %>', '<%= d.getDescription() %>', '<%= d.getStatus() %>')">
                                    Edit
                                </button>
                                <button type="button" class="btn btn-outline" style="padding: 0.25rem 0.65rem; font-size: 0.8rem;"
                                        onclick="toggleDept(<%= d.getDepartmentId() %>, '<%= "ACTIVE".equals(d.getStatus()) ? "INACTIVE" : "ACTIVE" %>')">
                                    <%= "ACTIVE".equals(d.getStatus()) ? "Disable" : "Enable" %>
                                </button>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Add/Edit Department Modal -->
    <div id="deptModal" class="modal-backdrop">
        <div class="modal">
            <div class="modal-header">
                <h3 id="modalTitle" style="font-size: 1.15rem; font-weight: 700;">Add Department</h3>
                <button type="button" style="border: none; background: transparent; font-size: 1.25rem; cursor: pointer;" onclick="closeModal()">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/departments-action" method="POST">
                <input type="hidden" id="actionInput" name="action" value="add">
                <input type="hidden" id="deptIdInput" name="departmentId" value="">

                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label" for="deptNameInput">Department Name</label>
                        <input type="text" id="deptNameInput" name="departmentName" class="form-control" required placeholder="e.g. Neurology">
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="deptDescInput">Description</label>
                        <textarea id="deptDescInput" name="description" class="form-control" rows="3" placeholder="Brief description..."></textarea>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="deptStatusInput">Status</label>
                        <select id="deptStatusInput" name="status" class="form-control">
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="INACTIVE">INACTIVE</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="closeModal()">Cancel</button>
                    <button type="submit" class="btn btn-primary" id="btnModalSave">Save Department</button>
                </div>
            </form>
        </div>
    </div>
</main>

<script>
function openAddModal() {
    document.getElementById("modalTitle").innerText = "Add Department";
    document.getElementById("actionInput").value = "add";
    document.getElementById("deptIdInput").value = "";
    document.getElementById("deptNameInput").value = "";
    document.getElementById("deptDescInput").value = "";
    document.getElementById("deptStatusInput").value = "ACTIVE";
    document.getElementById("deptModal").classList.add("active");
}

function openEditModal(id, name, desc, status) {
    document.getElementById("modalTitle").innerText = "Edit Department #" + id;
    document.getElementById("actionInput").value = "update";
    document.getElementById("deptIdInput").value = id;
    document.getElementById("deptNameInput").value = name;
    document.getElementById("deptDescInput").value = desc;
    document.getElementById("deptStatusInput").value = status;
    document.getElementById("deptModal").classList.add("active");
}

function closeModal() {
    document.getElementById("deptModal").classList.remove("active");
}

async function toggleDept(id, targetStatus) {
    if (!confirm("Are you sure you want to mark this department as " + targetStatus + "?")) return;
    try {
        const formData = new URLSearchParams();
        formData.append("action", "toggle");
        formData.append("departmentId", id);
        formData.append("status", targetStatus);

        const resp = await fetch(contextPath + "/admin/departments-action", {
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
