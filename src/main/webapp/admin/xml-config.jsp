<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.service.HospitalConfigParser, java.util.Map, java.util.List" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="XML Configuration - City Care Hospital" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    HospitalConfigParser parser = new HospitalConfigParser();
    parser.parseConfig();
%>

<main class="main-content">
    <div style="margin-bottom: 2rem;">
        <span class="badge" style="background: #e0e7ff; color: #4338ca; margin-bottom: 0.5rem;">System Configuration</span>
        <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--primary-dark);">
            Hospital XML Configuration & Schema Console
        </h1>
        <p style="color: var(--text-muted); font-size: 0.95rem;">
            Manage hospital XML definitions, validate schema integrity, and query clinical configuration nodes in real-time.
        </p>
    </div>

    <!-- 1. Parsed XML via DOM Parser -->
    <div class="card" style="margin-bottom: 2rem; border-left: 4px solid var(--primary);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
            <div>
                <span class="card-title">Hospital Master Configuration</span>
                <h3 style="font-size: 1.25rem; font-weight: 700; color: var(--primary-dark);">
                    hospital-config.xml
                </h3>
            </div>
            <span class="badge badge-completed">&#10003; Active & Synchronized</span>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; background: #f8fafc; padding: 1rem; border-radius: var(--radius-sm);">
            <div>
                <span class="card-title">Hospital Name:</span>
                <div style="font-size: 1.15rem; font-weight: 700; color: var(--primary);"><%= parser.getHospitalName() %></div>
            </div>
            <div>
                <span class="card-title">Default Avg Consultation:</span>
                <div style="font-size: 1.15rem; font-weight: 700;"><%= parser.getDefaultAverageConsultationTime() %> minutes</div>
            </div>
            <div>
                <span class="card-title">Max Online Bookings:</span>
                <div style="font-size: 1.15rem; font-weight: 700;"><%= parser.getMaximumOnlineBookings() %> patients</div>
            </div>
            <div>
                <span class="card-title">Arrival Check-in Required:</span>
                <div style="font-size: 1.15rem; font-weight: 700; color: var(--success);"><%= parser.isCheckInRequired() ? "YES" : "NO" %></div>
            </div>
        </div>

        <div class="card-title" style="margin-bottom: 0.5rem;">Configured Departments in XML:</div>
        <div class="table-responsive">
            <table class="table" style="font-size: 0.9rem;">
                <thead>
                    <tr>
                        <th>XML ID Attribute</th>
                        <th>Department Name</th>
                        <th>Room Element</th>
                        <th>Average Time (mins)</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, String> d : parser.getDepartments()) { %>
                        <tr>
                            <td><code><%= d.get("id") %></code></td>
                            <td><strong><%= d.get("name") %></strong></td>
                            <td>Room <%= d.get("room") %></td>
                            <td><%= d.get("averageTime") %> mins</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <!-- 2. XML Validation Controls (DTD and XSD) -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 1.5rem; margin-bottom: 2rem;">
        
        <!-- DTD Validation Card -->
        <div class="card">
            <span class="card-title">Validation 1</span>
            <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 0.5rem;">DTD Validation</h3>
            <p style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 1rem;">
                Validates <code>hospital-config.xml</code> against Document Type Definition (<code>hospital-config.dtd</code>).
            </p>
            <button type="button" class="btn btn-outline" style="width: 100%;" onclick="runValidation('validate-dtd', 'dtdResult')">
                &#9654; Run DTD Validation
            </button>
            <div id="dtdResult" style="margin-top: 1rem; display: none; font-size: 0.85rem;"></div>
        </div>

        <!-- XSD Validation Card -->
        <div class="card">
            <span class="card-title">Validation 2</span>
            <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 0.5rem;">XSD Schema Validation</h3>
            <p style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 1rem;">
                Validates types, ranges and attributes against XML Schema (<code>hospital-config.xsd</code>).
            </p>
            <button type="button" class="btn btn-outline" style="width: 100%;" onclick="runValidation('validate-xsd', 'xsdResult')">
                &#9654; Run XSD Validation
            </button>
            <div id="xsdResult" style="margin-top: 1rem; display: none; font-size: 0.85rem;"></div>
        </div>
    </div>

    <!-- 3. Configuration Query Console -->
    <div class="card">
        <span class="card-title">Configuration Search</span>
        <h3 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem;">XML Node Query Evaluator</h3>
        <p style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 1.25rem;">
            Query and filter clinical department nodes and properties from hospital-config.xml:
        </p>

        <!-- Preset Query Buttons -->
        <div class="btn-group" style="margin-bottom: 1.25rem;">
            <button type="button" class="btn btn-outline" style="font-size: 0.85rem;"
                    onclick="setAndRunXPath('//department[name=\'Cardiology\']')">
                Find Cardiology: <code>//department[name='Cardiology']</code>
            </button>
            <button type="button" class="btn btn-outline" style="font-size: 0.85rem;"
                    onclick="setAndRunXPath('//department[@id=\'D02\']')">
                Find ID D02: <code>//department[@id='D02']</code>
            </button>
            <button type="button" class="btn btn-outline" style="font-size: 0.85rem;"
                    onclick="setAndRunXPath('//department[averageTime > 5]')">
                Time &gt; 5 min: <code>//department[averageTime &gt; 5]</code>
            </button>
        </div>

        <div style="display: flex; gap: 0.75rem; margin-bottom: 1.25rem;">
            <input type="text" id="xpathQueryInput" class="form-control"
                   value="//department[averageTime > 5]" placeholder="Enter XPath query...">
            <button type="button" class="btn btn-primary" style="white-space: nowrap;" onclick="runCustomXPath()">
                Execute XPath
            </button>
        </div>

        <div id="xpathResultsArea" style="display: none;">
            <div style="font-size: 0.85rem; font-weight: 600; color: var(--text-muted); margin-bottom: 0.5rem;">
                Results Count: <span id="xpathCount" style="color: var(--primary);">0</span>
            </div>
            <div class="table-responsive">
                <table class="table" style="font-size: 0.9rem;">
                    <thead>
                        <tr>
                            <th>Department ID</th>
                            <th>Name</th>
                            <th>Room</th>
                            <th>Average Time</th>
                        </tr>
                    </thead>
                    <tbody id="xpathTableBody">
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<script>
async function runValidation(action, resultElementId) {
    const el = document.getElementById(resultElementId);
    el.style.display = "block";
    el.className = "alert alert-info";
    el.innerText = "Executing validation...";

    try {
        const resp = await HospitalAJAX.getXmlAction(action);
        if (resp && resp.data) {
            const d = resp.data;
            if (d.valid) {
                el.className = "alert alert-success";
                el.innerHTML = "<strong>&#10003; VALID:</strong> " + d.message;
            } else {
                el.className = "alert alert-danger";
                el.innerHTML = "<strong>&#10006; INVALID:</strong> " + d.message + "<br>" + (d.errors ? d.errors.join("<br>") : "");
            }
        }
    } catch (e) {
        el.className = "alert alert-danger";
        el.innerText = "Validation error: " + e.message;
    }
}

function setAndRunXPath(query) {
    document.getElementById("xpathQueryInput").value = query;
    runCustomXPath();
}

async function runCustomXPath() {
    const query = document.getElementById("xpathQueryInput").value;
    const resultsArea = document.getElementById("xpathResultsArea");
    const countSpan = document.getElementById("xpathCount");
    const tbody = document.getElementById("xpathTableBody");

    try {
        const resp = await HospitalAJAX.getXmlAction('xpath', query);
        if (resp && resp.data) {
            const res = resp.data.results || [];
            countSpan.innerText = res.length;
            if (res.length === 0) {
                tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No nodes matched this XPath expression.</td></tr>`;
            } else {
                tbody.innerHTML = res.map(node =>
                    '<tr>' +
                    '<td><code>' + (node.id || '--') + '</code></td>' +
                    '<td><strong>' + (node.name || '--') + '</strong></td>' +
                    '<td>Room ' + (node.room || '--') + '</td>' +
                    '<td>' + (node.averageTime ? (node.averageTime + ' mins') : '--') + '</td>' +
                    '</tr>'
                ).join('');
            }
            resultsArea.style.display = "block";
        }
    } catch (e) {
        alert("XPath evaluation error: " + e.message);
    }
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
