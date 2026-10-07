<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.DepartmentDAO, com.hospital.dao.DoctorDAO, com.hospital.model.Department, com.hospital.model.Doctor, java.util.List" %>
<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="title" value="City Care Hospital - Modern Healthcare & Smart Queue System" />
</jsp:include>
<jsp:include page="/WEB-INF/includes/navbar.jsp" />

<%
    DepartmentDAO deptDAO = new DepartmentDAO();
    DoctorDAO docDAO = new DoctorDAO();
    List<Department> departments = null;
    List<Doctor> allDoctors = null;
    try {
        departments = deptDAO.getActiveDepartments();
        allDoctors = docDAO.getAllDoctors();
    } catch (Exception e) {
        // Safe fallback in case of transient network latency
    }

    if (departments == null || departments.isEmpty()) {
        departments = new java.util.ArrayList<>();
        Department d1 = new Department(); d1.setDepartmentId(1); d1.setDepartmentName("General Medicine"); d1.setDescription("Primary healthcare, internal medicine, fever and routine consultations"); departments.add(d1);
        Department d2 = new Department(); d2.setDepartmentId(2); d2.setDepartmentName("Cardiology"); d2.setDescription("Heart health, cardiac checkups, hypertension and ECG services"); departments.add(d2);
        Department d3 = new Department(); d3.setDepartmentId(3); d3.setDepartmentName("Orthopedics"); d3.setDescription("Bone, joint, spine care, fractures and orthopedic surgery consultations"); departments.add(d3);
        Department d4 = new Department(); d4.setDepartmentId(4); d4.setDepartmentName("Pediatrics"); d4.setDescription("Comprehensive childcare, vaccinations and pediatric emergencies"); departments.add(d4);
    }
    if (allDoctors == null) {
        allDoctors = new java.util.ArrayList<>();
    }

    boolean isLoggedIn = (session.getAttribute("userId") != null);
    String userRole = (String) session.getAttribute("role");
%>

<main class="main-content">
    <!-- Hero Banner -->
    <div style="background: linear-gradient(135deg, #0284c7 0%, #0d9488 100%); color: white; padding: 3.5rem 2.5rem; border-radius: var(--radius-lg); margin-bottom: 2.5rem; box-shadow: var(--shadow-lg); position: relative; overflow: hidden;">
        <div style="max-width: 820px; position: relative; z-index: 2;">
            <div style="display: inline-flex; align-items: center; gap: 0.5rem; background: rgba(255, 255, 255, 0.2); padding: 0.35rem 0.85rem; border-radius: 9999px; font-size: 0.85rem; font-weight: 600; margin-bottom: 1.25rem; backdrop-filter: blur(4px);">
                <span>&#127973;</span> 24/7 Outpatient & Emergency Healthcare Services
            </div>
            <h1 style="font-size: 2.65rem; font-weight: 800; line-height: 1.2; margin-bottom: 1.2rem; letter-spacing: -0.02em;">
                Compassionate Care, Advanced Medicine, Zero Waiting Stress
            </h1>
            <p style="font-size: 1.15rem; opacity: 0.95; line-height: 1.6; margin-bottom: 2rem; max-width: 720px;">
                Welcome to City Care Hospital. Book your doctor consultation token online from home, track live queue progression on your device in real-time, and arrive comfortably right when the doctor is ready for you.
            </p>
            <div class="btn-group" style="gap: 1rem; flex-wrap: wrap;">
                <% if (!isLoggedIn) { %>
                    <a href="${pageContext.request.contextPath}/patient/book-token.jsp" class="btn btn-warning" style="font-size: 1.05rem; padding: 0.8rem 1.75rem; font-weight: 700; box-shadow: var(--shadow-md);">
                        &#128197; Book an Appointment
                    </a>
                    <a href="${pageContext.request.contextPath}/patient/live-queue.jsp" class="btn" style="background: white; color: var(--primary-dark); font-size: 1.05rem; padding: 0.8rem 1.5rem; font-weight: 700;">
                        &#128227; Live Queue Tracker
                    </a>
                    <a href="${pageContext.request.contextPath}/auth/login.jsp" class="btn btn-outline" style="border-color: rgba(255,255,255,0.7); color: white; padding: 0.8rem 1.5rem;">
                        Patient Login
                    </a>
                <% } else if ("PATIENT".equals(userRole)) { %>
                    <a href="${pageContext.request.contextPath}/patient/dashboard.jsp" class="btn btn-warning" style="font-size: 1.05rem; padding: 0.8rem 1.75rem; font-weight: 700;">
                        &#128100; My Patient Dashboard
                    </a>
                    <a href="${pageContext.request.contextPath}/patient/book-token.jsp" class="btn" style="background: white; color: var(--primary-dark); font-size: 1.05rem; padding: 0.8rem 1.5rem; font-weight: 700;">
                        &#128197; Book New Token
                    </a>
                    <a href="${pageContext.request.contextPath}/patient/live-queue.jsp" class="btn btn-outline" style="border-color: rgba(255,255,255,0.7); color: white; padding: 0.8rem 1.5rem;">
                        &#128227; Live Queue Tracker
                    </a>
                <% } else if ("DOCTOR".equals(userRole)) { %>
                    <a href="${pageContext.request.contextPath}/doctor/dashboard.jsp" class="btn btn-warning" style="font-size: 1.05rem; padding: 0.8rem 1.75rem; font-weight: 700;">
                        Doctor Console
                    </a>
                    <a href="${pageContext.request.contextPath}/doctor/queue.jsp" class="btn" style="background: white; color: var(--primary-dark); font-size: 1.05rem; padding: 0.8rem 1.5rem; font-weight: 700;">
                        Today's Queue
                    </a>
                <% } else if ("ADMIN".equals(userRole)) { %>
                    <a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="btn btn-warning" style="font-size: 1.05rem; padding: 0.8rem 1.75rem; font-weight: 700;">
                        Admin Dashboard
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/queue-monitor.jsp" class="btn" style="background: white; color: var(--primary-dark); font-size: 1.05rem; padding: 0.8rem 1.5rem; font-weight: 700;">
                        Queue Monitor
                    </a>
                <% } %>
            </div>
        </div>
    </div>

    <!-- Key Services & Advantages (4 Features) -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.5rem; margin-bottom: 3rem;">
        <div class="card" style="border-top: 4px solid var(--primary); padding: 1.5rem;">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">&#128197;</div>
            <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 0.5rem; color: var(--primary-dark);">Online Token Booking</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.5;">
                Avoid crowded hospital waiting halls. Select your doctor, view available slots, and reserve your token from home.
            </p>
        </div>

        <div class="card" style="border-top: 4px solid var(--secondary); padding: 1.5rem;">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">&#9201;</div>
            <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 0.5rem; color: var(--secondary);">Live Queue Tracking</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.5;">
                Real-time updates every 3 seconds show current tokens being served, people ahead of you, and estimated waiting time.
            </p>
        </div>

        <div class="card" style="border-top: 4px solid #8b5cf6; padding: 1.5rem;">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">&#129658;</div>
            <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 0.5rem; color: #7c3aed;">Expert Clinical Specialists</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.5;">
                Experienced senior physicians, cardiologists, orthopedic surgeons, and pediatric consultants ready to provide care.
            </p>
        </div>

        <div class="card" style="border-top: 4px solid var(--success); padding: 1.5rem;">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">&#128227;</div>
            <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 0.5rem; color: var(--success);">Smart Notifications</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.5;">
                Receive proactive alerts when your turn is approaching: "You Are Next" and "Your Token is Called, Proceed to Room".
            </p>
        </div>
    </div>

    <!-- How It Works Section -->
    <div class="card" style="padding: 2.5rem 2rem; margin-bottom: 3rem; background: #ffffff;">
        <div style="text-align: center; margin-bottom: 2rem;">
            <span class="card-title">Simple 3-Step Process</span>
            <h2 style="font-size: 1.75rem; font-weight: 800; color: var(--primary-dark);">How Online Queue Management Works</h2>
            <p style="color: var(--text-muted); font-size: 0.95rem; max-width: 600px; margin: 0.5rem auto 0;">
                Designed for maximum patient convenience and seamless hospital clinic flow.
            </p>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 2rem;">
            <div style="text-align: center; padding: 1.25rem;">
                <div style="width: 56px; height: 56px; border-radius: 50%; background: var(--primary-light); color: var(--primary-dark); font-size: 1.5rem; font-weight: 800; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem;">
                    1
                </div>
                <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.5rem;">Choose Doctor & Date</h3>
                <p style="color: var(--text-muted); font-size: 0.88rem; line-height: 1.5;">
                    Browse hospital departments, select a specialized doctor, and choose your consultation date.
                </p>
            </div>

            <div style="text-align: center; padding: 1.25rem;">
                <div style="width: 56px; height: 56px; border-radius: 50%; background: var(--secondary-light); color: var(--secondary); font-size: 1.5rem; font-weight: 800; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem;">
                    2
                </div>
                <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.5rem;">Pick Your Token Slot</h3>
                <p style="color: var(--text-muted); font-size: 0.88rem; line-height: 1.5;">
                    Select an available slot number from the interactive token grid and get instant digital confirmation.
                </p>
            </div>

            <div style="text-align: center; padding: 1.25rem;">
                <div style="width: 56px; height: 56px; border-radius: 50%; background: var(--success-light); color: var(--success); font-size: 1.5rem; font-weight: 800; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem;">
                    3
                </div>
                <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.5rem;">Track & Consult</h3>
                <p style="color: var(--text-muted); font-size: 0.88rem; line-height: 1.5;">
                    Follow live queue status on your phone, check in upon arrival, and walk into the consultation room when called.
                </p>
            </div>
        </div>
    </div>

    <!-- Active Departments & Specialists Directory -->
    <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 0.75rem;">
        <div>
            <span class="card-title">Specialized Clinics</span>
            <h2 style="font-size: 1.65rem; font-weight: 800; color: var(--primary-dark);">Our Medical Departments</h2>
        </div>
        <a href="${pageContext.request.contextPath}/patient/book-token.jsp" class="btn btn-primary" style="font-size: 0.9rem;">
            Book Consultation in Any Department &rarr;
        </a>
    </div>

    <div class="dashboard-grid" style="margin-bottom: 3rem;">
        <%
            for (Department d : departments) {
                List<Doctor> docs = new java.util.ArrayList<>();
                for (Doctor doc : allDoctors) {
                    if (doc.getDepartmentId() == d.getDepartmentId()) {
                        docs.add(doc);
                    }
                }
                String deptIcon = "🏥";
                String deptName = (d.getDepartmentName() != null) ? d.getDepartmentName().toLowerCase() : "";
                if (deptName.contains("cardio")) deptIcon = "❤️";
                else if (deptName.contains("ortho")) deptIcon = "🦴";
                else if (deptName.contains("pediatric")) deptIcon = "👶";
                else if (deptName.contains("general") || deptName.contains("medicine")) deptIcon = "🩺";
        %>
            <div class="card" style="display: flex; flex-direction: column; justify-content: space-between;">
                <div>
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                        <span style="font-size: 1.75rem;"><%= deptIcon %></span>
                        <span class="badge badge-completed"><%= docs.size() %> Doctor<%= docs.size() == 1 ? "" : "s" %></span>
                    </div>
                    <h3 style="font-size: 1.2rem; font-weight: 700; color: var(--primary-dark); margin-bottom: 0.5rem;">
                        <%= d.getDepartmentName() %>
                    </h3>
                    <p style="color: var(--text-muted); font-size: 0.88rem; margin-bottom: 1.25rem; line-height: 1.5;">
                        <%= d.getDescription() %>
                    </p>
                    
                    <div style="border-top: 1px solid var(--border-color); padding-top: 0.85rem; margin-bottom: 1rem;">
                        <div style="font-size: 0.8rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; margin-bottom: 0.5rem;">
                            Consulting Doctors:
                        </div>
                        <% if (docs.isEmpty()) { %>
                            <div style="font-size: 0.85rem; color: var(--text-muted); font-style: italic;">Specialist consultations on appointment</div>
                        <% } else {
                            for (Doctor doc : docs) { %>
                                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem; font-size: 0.9rem;">
                                    <strong style="color: var(--text-main);"><%= doc.getDoctorName() %></strong>
                                    <span style="font-size: 0.8rem; color: var(--text-muted); background: var(--bg-main); padding: 0.15rem 0.5rem; border-radius: var(--radius-sm);">
                                        Room <%= doc.getRoomNumber() != null ? doc.getRoomNumber() : "101" %>
                                    </span>
                                </div>
                            <% }
                        } %>
                    </div>
                </div>

                <a href="${pageContext.request.contextPath}/patient/book-token.jsp" class="btn btn-outline" style="width: 100%; text-align: center; margin-top: 0.5rem;">
                    Book Token Online
                </a>
            </div>
        <% } %>
    </div>

    <!-- Hospital Contact, Location & Emergency Assistance -->
    <div class="card" style="background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%); color: white; padding: 2.5rem 2rem; border-radius: var(--radius-md);">
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 2rem; align-items: center;">
            <div>
                <span class="badge" style="background: rgba(239, 68, 68, 0.25); color: #fca5a5; margin-bottom: 0.75rem;">
                    24x7 EMERGENCY & AMBULANCE
                </span>
                <h3 style="font-size: 1.5rem; font-weight: 800; margin-bottom: 0.5rem;">Need Immediate Medical Care?</h3>
                <p style="color: #cbd5e1; font-size: 0.9rem; line-height: 1.6; margin-bottom: 1.25rem;">
                    Our Emergency Department and Trauma Center are staffed 24 hours a day, 7 days a week with critical care doctors and life-support ambulances.
                </p>
                <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
                    <span style="background: #dc2626; color: white; font-weight: 800; padding: 0.5rem 1rem; border-radius: var(--radius-sm); font-size: 1rem;">
                        &#128222; Emergency: 108 / (044) 2800 1234
                    </span>
                </div>
            </div>

            <div style="border-left: 1px solid rgba(255,255,255,0.15); padding-left: 1.5rem;">
                <h4 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 1rem; color: #93c5fd;">Hospital Information</h4>
                <div style="font-size: 0.88rem; color: #e2e8f0; line-height: 1.7;">
                    <div>&#128205; <strong>Address:</strong> 124 Health Care Avenue, Medical District</div>
                    <div>&#128344; <strong>Outpatient Hours:</strong> Mon &ndash; Sat: 8:00 AM &ndash; 8:00 PM</div>
                    <div>&#9993; <strong>Patient Inquiries:</strong> helpdesk@citycarehospital.com</div>
                    <div>&#9742; <strong>Helpdesk:</strong> +91 (044) 2800 5678</div>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
