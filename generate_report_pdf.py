import os
import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib.units import inch
from reportlab.lib.colors import HexColor
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(HexColor("#64748b"))
        
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 755, "City Care Hospital — Real-Time Appointment & Queue Management System")
            self.drawRightString(612 - 54, 755, "Project Technical Report")
            self.setStrokeColor(HexColor("#cbd5e1"))
            self.setLineWidth(0.5)
            self.line(54, 747, 612 - 54, 747)

        # Footer (all pages)
        self.setStrokeColor(HexColor("#cbd5e1"))
        self.setLineWidth(0.5)
        self.line(54, 45, 612 - 54, 45)
        self.drawString(54, 32, "Confidential — Academic & Technical Evaluation Document")
        page_str = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(612 - 54, 32, page_str)
        self.restoreState()

def build_pdf(filename="Hospital_Queue_Management_System_Project_Report.pdf"):
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    
    # Custom styles
    title_project = ParagraphStyle(
        'ProjectTitleLabel',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=18,
        leading=22,
        alignment=1, # Center
        textColor=HexColor('#0f172a'),
        spaceAfter=4
    )

    title_sub = ParagraphStyle(
        'ProjectSubLabel',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=15,
        leading=19,
        alignment=1,
        textColor=HexColor('#0284c7'),
        spaceAfter=6
    )

    title_desc = ParagraphStyle(
        'ProjectDescLabel',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        alignment=1,
        textColor=HexColor('#1e293b'),
        spaceAfter=14
    )

    meta_style = ParagraphStyle(
        'MetaStyle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        alignment=0,
        textColor=HexColor('#334155'),
        spaceAfter=4
    )

    sec_heading = ParagraphStyle(
        'SecHeading',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12.5,
        leading=16,
        textColor=HexColor('#0f172a'),
        spaceBefore=12,
        spaceAfter=8
    )

    subsec_heading = ParagraphStyle(
        'SubSecHeading',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10.5,
        leading=14,
        textColor=HexColor('#0284c7'),
        spaceBefore=8,
        spaceAfter=4
    )

    body_style = ParagraphStyle(
        'BodyDark',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.2,
        leading=13.5,
        textColor=HexColor('#1e293b'),
        spaceAfter=6
    )

    bullet_style = ParagraphStyle(
        'BulletDark',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.0,
        leading=13.0,
        textColor=HexColor('#1e293b'),
        leftIndent=14,
        firstLineIndent=-10,
        spaceAfter=4
    )

    code_box_style = ParagraphStyle(
        'CodeBoxStyle',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8.0,
        leading=10.5,
        textColor=HexColor('#0f172a')
    )

    story = []

    # =========================================================================
    # PAGE 1: COVER & ABSTRACT
    # =========================================================================
    story.append(Spacer(1, 15))
    story.append(Paragraph("PROJECT", title_project))
    story.append(Paragraph("CITY CARE HOSPITAL", title_sub))
    story.append(Paragraph("Real-Time Hospital Appointment & Queue Management System", title_desc))
    
    story.append(Spacer(1, 4))
    story.append(Paragraph("<b>Domain:</b> Distributed Healthcare Systems, Real-Time Web Engineering, Enterprise Java", meta_style))
    story.append(Paragraph("<b>Architecture:</b> Model-View-Controller (MVC) Pattern & Asynchronous Polling Architecture", meta_style))
    story.append(Paragraph("<b>Platforms:</b> Apache Tomcat 10.1 EE & MySQL 8.0 Relational Database (Cloud & Container Ready)", meta_style))
    story.append(Spacer(1, 10))

    story.append(Paragraph("1. PROJECT ABSTRACT & DESCRIPTION", sec_heading))
    story.append(Paragraph("<b>Abstract :</b>", subsec_heading))
    abstract_text = (
        "The Real-Time Hospital Appointment and Queue Management System (City Care Hospital) is an "
        "enterprise-grade outpatient department (OPD) queue coordination and token management platform. "
        "The system models a synchronized clinical workflow connecting outpatient visitors, attending "
        "medical specialists, and hospital administrative staff. Patients can browse specialized clinical "
        "departments (General Medicine, Cardiology, Orthopedics, Pediatrics), inspect doctor availability "
        "schedules, and reserve numbered consultation tokens without physical waiting line congestion. "
        "When patients arrive, attending physicians manage their live consultation queue via an interactive "
        "clinical console, calling tokens sequentially and advancing status flags (Waiting &rarr; Called &rarr; "
        "In Consultation &rarr; Completed / Absent). A lightweight asynchronous polling engine (AJAX Fetch API "
        "at 3-second intervals) keeps patient status displays in real-time synchronization with doctor actions "
        "without full-page reloads. Additionally, the system features an enterprise XML configuration subsystem "
        "with Java DOM Parser (<code>DocumentBuilderFactory</code>), dual DTD/XSD schema verification, and "
        "real-time XPath query evaluation."
    )
    story.append(Paragraph(abstract_text, body_style))

    story.append(Spacer(1, 4))
    story.append(Paragraph("<b>Motivation & Problem Statement :</b>", subsec_heading))
    
    bullets_p1 = [
        "<b>Outpatient Hall Congestion & Extended Wait Times:</b> Traditional OPD queues force vulnerable patients into overcrowded waiting halls, increasing cross-infection contagion risks, patient anxiety, and operational friction.",
        "<b>Lack of Real-Time Queue Transparency:</b> Patients historically lack visibility into how many individuals precede them or when their turn is imminent, preventing timely arrival and disrupting doctor schedules.",
        "<b>Scheduling Clashes & Token Double-Booking:</b> Uncoordinated manual token dispatch often leads to token number collisions, overbooked clinic hours, and high doctor burnout without automated guardrails.",
        "<b>Comprehensive Enterprise Web Standards Implementation:</b> Serves as an end-to-end realization of modern Web Technology standards—combining classical Java Enterprise architecture (Jakarta Servlet 6.0, JSP 3.1, JDBC, XML DOM, DTD/XSD validation, XPath) with dynamic client-side DOM mutation and asynchronous Fetch APIs."
    ]
    for b in bullets_p1:
        story.append(Paragraph(f"&bull; {b}", bullet_style))

    story.append(PageBreak())

    # =========================================================================
    # PAGE 2: SOFTWARE SPECIFICATIONS & SYSTEM ARCHITECTURE
    # =========================================================================
    story.append(Paragraph("2. SOFTWARE SPECIFICATIONS", sec_heading))
    
    spec_data = [
        ["Tier / Layer", "Technology", "Version"],
        ["Server Runtime (Java)", "Apache Tomcat", "10.1.x"],
        ["Language (Backend)", "Java SE (JDK)", "21 LTS / 17 LTS"],
        ["Java Standards", "Jakarta Servlet & JSP", "6.0 / 3.1"],
        ["Database Tier", "MySQL Relational Database", "8.0.x"],
        ["Asynchronous Engine", "AJAX (JavaScript Fetch API)", "ES6+ Modern Standard"],
        ["Data Interchange", "XML, DTD, XSD & Java DOM Parser", "W3C DOM / JAXP"],
        ["Query & Path Evaluation", "XPath API (javax.xml.xpath)", "W3C XPath 1.0/2.0"],
        ["Security & Cryptography", "PBKDF2 with HMAC-SHA256", "RFC 2898 Standard"],
        ["State & Personalization", "HTTP Session & Persistent Cookies", "RFC 6265 Standard"],
        ["Build Automation", "Apache Maven", "3.9.x"],
        ["Client Presentation", "HTML5, CSS3 Modern Tokens, Vanilla JS", "W3C Standards"]
    ]

    t_spec = Table(spec_data, colWidths=[150, 220, 134])
    t_spec.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), HexColor('#f1f5f9')),
        ('TEXTCOLOR', (0, 0), (-1, 0), HexColor('#0f172a')),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, -1), 8.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('GRID', (0, 0), (-1, -1), 0.5, HexColor('#cbd5e1')),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [HexColor('#ffffff'), HexColor('#f8fafc')])
    ]))
    story.append(t_spec)

    story.append(Spacer(1, 10))
    story.append(Paragraph("3. SYSTEM ARCHITECTURE & DESIGN PATTERNS", sec_heading))

    arch_diagram = """
                       Client Web Browser (HTML5 / CSS3 / JavaScript Fetch API)
                                              |
                   HTTP Requests (GET/POST) / Asynchronous Polling (3s Interval)
                                              |
                                  Authentication Filter (AuthFilter)
                                              |
       +--------------------------------------+--------------------------------------+
       |                                      |                                      |
Patient Servlets                       Doctor Servlets                        Admin Servlets
 - BookTokenServlet                     - CallNextPatientServlet               - DepartmentServlet
 - AvailableTokensServlet               - StartConsultationServlet             - DoctorServlet
 - QueueStatusServlet                   - CompleteConsultationServlet          - RoomServlet
 - CheckInServlet                       - MarkAbsentServlet                    - PatientServlet
 - CancelTokenServlet                   - DoctorQueueServlet                   - XmlConfigServlet
       |                                      |                                      |
       +--------------------------------------+--------------------------------------+
                                              |
                          Business Service Layer & XML Subsystem
                       - HospitalConfigParser (DOM DocumentBuilder)
                       - HospitalXPathService (XPath Evaluator)
                       - XMLValidator (DTD & XSD Validators)
                       - PasswordUtil (PBKDF2 HMAC-SHA256)
                                              |
                                  Data Access Objects (DAO)
             [UserDAO]  [DoctorDAO]  [QueueDAO]  [DepartmentDAO]  [RoomDAO]
                                              |
                            JDBC Connection Pool (DBConnection)
                                              |
                         MySQL Relational Database (hospital_queue_db)
"""
    
    diagram_table = Table([[Paragraph(f"<pre>{arch_diagram.strip()}</pre>", code_box_style)]], colWidths=[504])
    diagram_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), HexColor('#f8fafc')),
        ('BOX', (0, 0), (-1, -1), 0.5, HexColor('#cbd5e1')),
        ('TOPPADDING', (0, 0), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 6),
        ('LEFTPADDING', (0, 0), (-1, -1), 10),
        ('RIGHTPADDING', (0, 0), (-1, -1), 10),
    ]))
    story.append(diagram_table)

    story.append(PageBreak())

    # =========================================================================
    # PAGE 3: DESIGN PATTERNS & WORKFLOW LIFECYCLE
    # =========================================================================
    story.append(Paragraph("<b>Design Patterns Implemented:</b>", subsec_heading))
    patterns = [
        "<b>Model-View-Controller (MVC):</b> Decouples presentation (JSP / modern CSS / DOM rendering), request routing and controller handling (Jakarta Servlets), and persistent domain entity models (Java Beans).",
        "<b>Data Access Object (DAO) Pattern:</b> Encapsulates all raw SQL statements and transactional operations into dedicated classes (QueueDAO, DoctorDAO, UserDAO, DepartmentDAO), ensuring database independence and clean separation of concerns.",
        "<b>Singleton Connection Pattern:</b> <code>DBConnection.java</code> centralizes database connection retrieval, managing socket life-cycles, credentials, and connection health across concurrent requests.",
        "<b>Observer & Polling Pattern:</b> Lightweight client-side asynchronous Fetch loops poll the server every 3 seconds to observe queue state transitions, dynamically updating DOM nodes without page reloads.",
        "<b>Intercepting Filter Pattern:</b> <code>AuthFilter.java</code> intercepts every incoming HTTP request to enforce strict Role-Based Access Control (RBAC), preventing privilege escalation across Patient, Doctor, and Admin zones."
    ]
    for idx, p in enumerate(patterns, 1):
        story.append(Paragraph(f"{idx}. {p}", bullet_style))

    story.append(Spacer(1, 8))
    story.append(Paragraph("4. WORKFLOW & SYSTEM LIFECYCLE", sec_heading))

    workflows = [
        ("1. Authentication & Session Initialization:", [
            "Patients, Doctors, and Administrators log in via a unified secure entry point at <code>/auth/login.jsp</code>.",
            "Credentials are authenticated against PBKDF2 with HMAC-SHA256 salted password hashes stored in MySQL.",
            "A secure <code>HttpSession</code> is established, persisting user ID, full name, and granted security role.",
            "Optional 30-day 'Remember Me' and clinic preference cookies are stored securely in the client browser."
        ]),
        ("2. Clinic Selection & Online Token Booking:", [
            "Patient selects a specialty department (e.g., General Medicine, Cardiology, Orthopedics, Pediatrics).",
            "An asynchronous AJAX call retrieves available specialists, room assignments, and consultation dates.",
            "The client renders an interactive 30-token slot grid showing available, booked, and current queue tokens.",
            "Upon selection, an atomic database transaction reserves the token, preventing race condition double-bookings."
        ]),
        ("3. Real-Time Queue Synchronization & Live Status Tracking:", [
            "Patient accesses the Live Queue or Dashboard, which initiates background 3-second polling.",
            "The system continuously calculates and returns: Current Calling Token, People Ahead, and Estimated Wait Time.",
            "Threshold alerts trigger instantly: When 0 patients remain, the banner signals <b>'YOU ARE NEXT'</b>.",
            "When the token is actively called, the display updates to <b>'YOUR TOKEN IS CALLED. PROCEED TO ROOM [X]'</b>."
        ]),
        ("4. Doctor Clinical Console & Queue Progression:", [
            "The attending physician views their live roster of waiting patients ordered sequentially by token number.",
            "Doctor clicks <b>'Call Next Patient'</b>, updating queue status from <code>WAITING</code> to <code>CALLED</code>.",
            "Doctor marks <b>'Start Consultation'</b> (advances to <code>IN_CONSULTATION</code>) and <b>'Complete'</b> (advances to <code>COMPLETED</code>).",
            "Unresponsive patients can be flagged as <code>ABSENT</code>, automatically advancing the waiting line."
        ]),
        ("5. Administrative Oversight & XML Configuration:", [
            "Administrator manages hospital metadata, department departments, rooms, and doctor assignments.",
            "The XML console parses <code>hospital-config.xml</code> using Java DOM Parser (<code>DocumentBuilder</code>).",
            "Live DTD and XSD validation controls verify document syntax, schema constraints, and structural integrity.",
            "An XPath evaluation console executes real-time node queries across departments and operational parameters."
        ])
    ]

    for title, items in workflows:
        story.append(Paragraph(f"<b>{title}</b>", subsec_heading))
        for it in items:
            story.append(Paragraph(f"&bull; {it}", bullet_style))

    story.append(PageBreak())

    # =========================================================================
    # PAGE 4: CORE FEATURES BREAKDOWN
    # =========================================================================
    story.append(Paragraph("5. CORE FEATURES BREAKDOWN", sec_heading))

    features = [
        ("Feature 1: Asynchronous Real-Time Queue Synchronization", [
            "Client-side <code>setInterval()</code> poller queries <code>/patient/queue-status</code> every 3,000 milliseconds.",
            "Updates Current Token, Number of Patients Ahead, and Estimated Minutes remaining with zero full-page reloads.",
            "Dynamic badge and alert mutations keep waiting patients informed in real time."
        ]),
        ("Feature 2: Concurrency-Safe Online Token Reservation", [
            "Interactive digital token grid rendering 30 daily consultation slots per physician.",
            "Backend unique composite constraint <code>UNIQUE (doctor_id, queue_date, token_number)</code> guarantees no duplicate allocations.",
            "Instant booking confirmation with room routing, consultation date, and persistent token summary."
        ]),
        ("Feature 3: Role-Based Access Control (RBAC) & Multi-Role Architecture", [
            "Three distinct privilege levels: <b>PATIENT</b>, <b>DOCTOR</b>, and <b>ADMIN</b>.",
            "<code>AuthFilter</code> intercepts and protects restricted URI paths (<code>/patient/*</code>, <code>/doctor/*</code>, <code>/admin/*</code>).",
            "Automatic redirect to login with session expiry detection and unauthorized access blocking."
        ]),
        ("Feature 4: Physician Clinical Console & Queue Orchestration", [
            "Dedicated doctor workstation displaying waiting patient list, token progression, and daily consultation totals.",
            "Action triggers: <i>Call Next Patient</i>, <i>Start Consultation</i>, <i>Complete Consultation</i>, and <i>Mark Absent</i>.",
            "Doctor availability toggle (<code>AVAILABLE</code> vs. <code>OFFLINE</code>) reflects immediately on public rosters."
        ]),
        ("Feature 5: Clinical Department Directory & Specialist Routing", [
            "Multi-specialty support: General Medicine (Room 101), Cardiology (Room 202), Orthopedics (Room 305), Pediatrics (Room 401).",
            "Full CRUD management of departments, physical rooms, and doctor clinic schedules."
        ]),
        ("Feature 6: Dynamic XML Hospital Configuration (Java DOM Parser)", [
            "Centralized hospital configuration maintained in standard <code>hospital-config.xml</code>.",
            "Backend parsing via Java JAXP <code>DocumentBuilderFactory</code> and <code>DocumentBuilder</code>.",
            "Extracts hospital metadata, average consultation durations, capacity caps, and department definitions."
        ]),
        ("Feature 7: DTD & XSD Dual Schema Validation Engine", [
            "Structural validation against Document Type Definition (<code>hospital-config.dtd</code>).",
            "Strict schema validation against XML Schema Definition (<code>hospital-config.xsd</code>) for types and ranges.",
            "Live admin console executing asynchronous validation requests and reporting validation status."
        ]),
        ("Feature 8: Real-Time XPath Query Evaluator", [
            "Executes real-time XPath expressions using Java <code>javax.xml.xpath.XPath</code>.",
            "Supports parameterized queries (e.g., <code>//department[name='Cardiology']</code>, <code>//department[averageTime > 5]</code>).",
            "Renders query result nodes directly into formatted administrative data tables."
        ]),
        ("Feature 9: Patient Preference Persistence & Session Management", [
            "Custom <code>CookieUtil</code> handles client preferences (Preferred Department, Preferred Language, Display Mode).",
            "Secure session tracking with CSRF safeguards, session fixation protection, and PBKDF2 password cryptography."
        ]),
        ("Feature 10: Executive Administration & Hospital Monitoring", [
            "Executive dashboard displaying today's patients, online bookings, walk-in tokens, and active clinics.",
            "Global queue monitor tracking real-time queue states across all hospital consultation rooms simultaneously."
        ])
    ]

    for f_title, f_items in features[:6]:
        story.append(Paragraph(f"<b>{f_title}</b>", subsec_heading))
        for fi in f_items:
            story.append(Paragraph(f"&bull; {fi}", bullet_style))

    story.append(PageBreak())

    # =========================================================================
    # PAGE 5: CORE FEATURES (CONT.) & CONCLUSION
    # =========================================================================
    for f_title, f_items in features[6:]:
        story.append(Paragraph(f"<b>{f_title}</b>", subsec_heading))
        for fi in f_items:
            story.append(Paragraph(f"&bull; {fi}", bullet_style))

    story.append(Spacer(1, 8))
    story.append(Paragraph("6. CONCLUSION & FUTURE ENHANCEMENTS", sec_heading))
    
    concl_text = (
        "The Real-Time Hospital Appointment and Queue Management System successfully bridges classical "
        "enterprise Java architectural patterns (Jakarta Servlets, JSP, JDBC, XML DOM, DTD/XSD, XPath) with modern "
        "responsive user interfaces and asynchronous polling workflows. By replacing manual waiting hall crowding with "
        "transparent digital tokens and sub-second queue tracking, the platform dramatically reduces patient anxiety, "
        "optimizes doctor utilization, and provides hospital leadership with continuous operational visibility."
    )
    story.append(Paragraph(concl_text, body_style))

    story.append(Spacer(1, 4))
    story.append(Paragraph("<b>Future Enhancements :</b>", subsec_heading))
    enhancements = [
        "<b>SMS & WhatsApp Notification Gateway:</b> Dispatching automated SMS alerts when a patient is 2 tokens away, allowing patients to arrive just in time from outside the hospital premises.",
        "<b>Automated Kiosk & QR Code Check-in:</b> Allowing patients with online bookings to scan a dynamic QR code at the hospital lobby kiosk to confirm arrival and activate their queue slot.",
        "<b>Machine Learning Wait Time Prediction:</b> Leveraging historical consultation durations, patient age, and diagnostic categories to dynamically refine estimated waiting times beyond fixed averages.",
        "<b>Integrated Telemedicine & E-Prescription:</b> Enabling remote audio/video consultations for follow-up patients with automated PDF prescription generation."
    ]
    for enh in enhancements:
        story.append(Paragraph(f"&bull; {enh}", bullet_style))

    story.append(PageBreak())

    # =========================================================================
    # PAGE 6: SYSTEM WALKTHROUGH & SCREENSHOT REPRESENTATIONS (PART 1)
    # =========================================================================
    story.append(Paragraph("7. SYSTEM USER INTERFACE & WORKFLOW WALKTHROUGH", sec_heading))
    story.append(Paragraph("The following representations illustrate the core interactive modules deployed across the application:", body_style))
    story.append(Spacer(1, 6))

    # Screen 1: Homepage
    screen1_title = Paragraph("<b>Figure 1: Hospital Landing Portal & Patient Entry (<code>index.jsp</code>)</b>", subsec_heading)
    screen1_desc = (
        "A modern hospital homepage featuring hospital branding, quick action buttons (Book Token, Live Queue, "
        "Doctor Directory), 3-step patient journey guide, active departments directory with direct booking links, "
        "and 24/7 emergency support contact information."
    )
    s1_table = Table([
        [Paragraph("<font size=8><b>+ City Care Hospital</b> &nbsp;&bull;&nbsp; Home | Book Token | Live Queue | Login | Register</font>", code_box_style)],
        [Paragraph("<font size=10 color='#0284c7'><b>Your Health, On Time. Without the Waiting.</b></font><br/>"
                   "<font size=8>Reserve your consultation token online, track your turn live from your phone, and arrive exactly when the doctor is ready.</font><br/><br/>"
                   "<font size=8><b>[ + Book Token ]</b> &nbsp;&nbsp; <b>[ &bull; View Live Queue ]</b> &nbsp;&nbsp; <b>[ Find a Doctor ]</b></font>", code_box_style)],
        [Paragraph("<font size=8><b>Clinical Specialties:</b><br/>"
                   "&bull; General Medicine (Room 101) &mdash; Dr. Kumar (Available)<br/>"
                   "&bull; Cardiology (Room 202) &mdash; Dr. Priya (Available)<br/>"
                   "&bull; Orthopedics (Room 305) &mdash; Dr. Arun (Available)<br/>"
                   "&bull; Pediatrics (Room 401) &mdash; Child Healthcare Specialists</font>", code_box_style)]
    ], colWidths=[504])
    s1_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), HexColor('#f8fafc')),
        ('BOX', (0, 0), (-1, -1), 0.5, HexColor('#cbd5e1')),
        ('GRID', (0, 0), (-1, -1), 0.5, HexColor('#e2e8f0')),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))

    story.append(screen1_title)
    story.append(Paragraph(screen1_desc, body_style))
    story.append(s1_table)
    story.append(Spacer(1, 14))

    # Screen 2: Patient Token Booking
    screen2_title = Paragraph("<b>Figure 2: Concurrency-Safe Token Booking Interface (<code>book-token.jsp</code>)</b>", subsec_heading)
    screen2_desc = (
        "Interactive token booking interface where patients select department, doctor, and date. "
        "A dynamic 30-token slot grid displays booked vs available slots, ensuring real-time slot selection with zero duplicate collisions."
    )
    s2_table = Table([
        [Paragraph("<font size=8><b>Book Consultation Token &mdash; Dr. Kumar (General Medicine)</b></font>", code_box_style)],
        [Paragraph("<font size=8>Selected Date: <b>Today</b> &nbsp;|&nbsp; Room: <b>101</b> &nbsp;|&nbsp; Est. Time per Patient: <b>5 mins</b></font>", code_box_style)],
        [Paragraph("<font size=8>"
                   "[ 01 Booked ] [ 02 Booked ] ... [ 20 CALLED ] [ 21 Waiting ] [ 22 Waiting ]<br/>"
                   "<b>[ 25 SELECTED - YOUR TOKEN ]</b> [ 26 Available ] [ 27 Available ] ... [ 30 Available ]<br/><br/>"
                   "Available Slots: <b>5 of 30</b> &nbsp;|&nbsp; <b>[ Confirm & Book Token #25 ]</b></font>", code_box_style)]
    ], colWidths=[504])
    s2_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), HexColor('#f8fafc')),
        ('BOX', (0, 0), (-1, -1), 0.5, HexColor('#cbd5e1')),
        ('GRID', (0, 0), (-1, -1), 0.5, HexColor('#e2e8f0')),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))

    story.append(screen2_title)
    story.append(Paragraph(screen2_desc, body_style))
    story.append(s2_table)

    story.append(PageBreak())

    # =========================================================================
    # PAGE 7: SYSTEM WALKTHROUGH & SCREENSHOT REPRESENTATIONS (PART 2)
    # =========================================================================
    story.append(Paragraph("7. SYSTEM USER INTERFACE & WORKFLOW WALKTHROUGH (CONT.)", sec_heading))
    story.append(Spacer(1, 4))

    # Screen 3: Patient Live Queue & Dashboard
    screen3_title = Paragraph("<b>Figure 3: Real-Time Patient Live Queue & Status Monitor (<code>dashboard.jsp</code>)</b>", subsec_heading)
    screen3_desc = (
        "Patient dashboard polling every 3 seconds via AJAX. Displays active token number, doctor room, "
        "calling status, countdown of patients ahead, and estimated wait minutes with instant alert transitions."
    )
    s3_table = Table([
        [Paragraph("<font size=8><b>Welcome, Arun &bull; Live Queue Consultation Status</b> (Auto-Syncing Active)</font>", code_box_style)],
        [Paragraph("<font size=8 color='#b45309'><b>[ ALERT ] Please wait. 4 patients are ahead of you. (Status: WAITING)</b></font>", code_box_style)],
        [Paragraph("<font size=9><b>Your Token: #25</b> &nbsp;|&nbsp; Doctor: <b>Dr. Kumar</b> &nbsp;|&nbsp; Room: <b>101</b><br/>"
                   "Current Calling Token: <font color='#0284c7'><b>#20</b></font> &nbsp;|&nbsp; "
                   "People Ahead: <font color='#ea580c'><b>4</b></font> &nbsp;|&nbsp; "
                   "Est. Wait Time: <font color='#16a34a'><b>20 mins</b></font></font>", code_box_style)],
        [Paragraph("<font size=8>Dynamic Alert States: "
                   "<i>1 Ahead &rarr; '1 patient ahead'</i> | "
                   "<i>0 Ahead &rarr; <b>'YOU ARE NEXT'</b></i> | "
                   "<i>Called &rarr; <b>'YOUR TOKEN IS CALLED. PROCEED TO ROOM 101'</b></i></font>", code_box_style)]
    ], colWidths=[504])
    s3_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), HexColor('#f8fafc')),
        ('BOX', (0, 0), (-1, -1), 0.5, HexColor('#cbd5e1')),
        ('GRID', (0, 0), (-1, -1), 0.5, HexColor('#e2e8f0')),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))

    story.append(screen3_title)
    story.append(Paragraph(screen3_desc, body_style))
    story.append(s3_table)
    story.append(Spacer(1, 14))

    # Screen 4: Doctor Console & Admin XML Hub
    screen4_title = Paragraph("<b>Figure 4: Doctor Clinical Console & Admin XML Configuration Hub</b>", subsec_heading)
    screen4_desc = (
        "Doctor console for advancing queue states, alongside the Admin XML Console featuring DOM parsing, "
        "DTD/XSD validation buttons, and real-time XPath evaluation against <code>hospital-config.xml</code>."
    )
    s4_table = Table([
        [Paragraph("<font size=8><b>Doctor Console (Dr. Kumar &bull; Room 101)</b></font>", code_box_style)],
        [Paragraph("<font size=8>"
                   "Currently In Consultation: <b>Token #20 (Completed)</b> &nbsp;|&nbsp; Next in Line: <b>Token #21 (Waiting)</b><br/>"
                   "<b>[ &bull; Call Next Patient ]</b> &nbsp;&nbsp; "
                   "<b>[ Start Consultation ]</b> &nbsp;&nbsp; "
                   "<b>[ Finish Consultation ]</b> &nbsp;&nbsp; "
                   "<b>[ Mark Absent ]</b></font>", code_box_style)],
        [Paragraph("<font size=8><b>Admin XML Configuration & Verification Console (<code>xml-config.jsp</code>)</b></font>", code_box_style)],
        [Paragraph("<font size=8>"
                   "Hospital Master Configuration: <code>hospital-config.xml</code> [ Active & Synchronized ]<br/>"
                   "&bull; <b>[ Run DTD Validation ]</b> &rarr; <font color='#16a34a'>VALID: hospital-config.xml conforms to hospital-config.dtd</font><br/>"
                   "&bull; <b>[ Run XSD Validation ]</b> &rarr; <font color='#16a34a'>VALID: Schema conforms to hospital-config.xsd</font><br/>"
                   "&bull; <b>XPath Query:</b> <code>//department[averageTime &gt; 5]</code> &rarr; <b>[ Execute XPath ]</b><br/>"
                   "&nbsp;&nbsp; Results: <i>Cardiology (10 mins), Orthopedics (8 mins), Pediatrics (6 mins)</i></font>", code_box_style)]
    ], colWidths=[504])
    s4_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), HexColor('#f8fafc')),
        ('BOX', (0, 0), (-1, -1), 0.5, HexColor('#cbd5e1')),
        ('GRID', (0, 0), (-1, -1), 0.5, HexColor('#e2e8f0')),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))

    story.append(screen4_title)
    story.append(Paragraph(screen4_desc, body_style))
    story.append(s4_table)

    # Build the document
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"Report generated successfully: {filename}")

if __name__ == '__main__':
    out = "c:\\WT_micro_project\\Hospital_Queue_Management_System_Project_Report.pdf"
    build_pdf(out)
