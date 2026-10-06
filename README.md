# Real-Time Hospital Appointment and Queue Management System

A college-level Web Technology laboratory project demonstrating standard Java Web Technologies, responsive DOM manipulation, real-time AJAX polling, HTTP sessions, cookies, XML validation (DTD & XSD), XML DOM parsing, XPath queries, and MySQL relational persistence.

---

## 1. Project Overview

The **Real-Time Hospital Appointment and Queue Management System** is an interactive web platform designed for outpatient departments. It bridges the gap between home and hospital by allowing patients to book consultation tokens online, monitor live queue progression in real time without refreshing the web page, and check in upon physical arrival at the hospital. Doctors call patients sequentially, conduct consultations, or mark absentees, while hospital administrators oversee department loads, manage clinic rooms, and monitor active queues across the facility.

---

## 2. Problem Statement

Traditional hospital outpatient queues force sick and vulnerable patients to wait for hours in crowded physical waiting areas without visibility into actual waiting times or doctor progress. This creates congestion, infection risks, and patient frustration. Existing enterprise solutions are either opaque proprietary software or overengineered cloud platforms. This project provides an academic reference implementation using standard web and Java technologies that solves queue transparency through automated queue progression algorithms, live polling, and client-side notifications.

---

## 3. Key Features

- **Patient Self-Service**:
  - Secure registration & login with PBKDF2 password hashing.
  - Department and doctor directory with real-time slot checking.
  - Online token booking from home with interactive token selection grid.
  - Live patient queue tracker updating dynamically every 3 seconds via AJAX (no page refresh).
  - Arrival check-in button to enter the active hospital queue.
  - Dynamic proximity notifications: *"Please wait. X patients ahead"*, *"YOU ARE NEXT"*, and *"YOUR TOKEN IS CALLED. PLEASE PROCEED TO ROOM [X]"*.
  - Token cancellation (releases slot back to other patients if not already called).
  - Personal consultation history and cookie preference customization.
- **Doctor Consultation Console**:
  - Clinic queue management with current serving status.
  - Sequential *"CALL NEXT PATIENT"*, *"START CONSULTATION"*, *"COMPLETE CONSULTATION"*, and *"MARK ABSENT"* workflows.
  - Today's live clinic metrics (waiting, completed, absent, total).
- **Hospital Administration**:
  - Global dashboard displaying patient loads, walk-in vs online breakdown, and department queues.
  - Full CRUD operations for medical departments, doctor profiles, and clinic rooms.
  - Cross-clinic multi-queue monitor screen.
  - Interactive XML inspection console demonstrating DOM Parsing, DTD/XSD validation, and XPath queries.

---

## 4. User Roles & Access Control

1. **PATIENT**: Books tokens, tracks live queue, checks in, cancels tokens, and views history.
2. **DOCTOR**: Manages clinic consultation flow, calls next token, starts/completes consultations.
3. **ADMIN**: Configures hospital structure (departments, doctors, rooms), monitors hospital-wide queues, and audits XML configuration.

*Enforcement*: `AuthFilter.java` intercepts every request, checks `session.getAttribute("role")`, and blocks unauthorized cross-role access (e.g., patient attempting to open `/doctor/dashboard.jsp` receives HTTP 302 redirect to `/error/403.jsp`).

---

## 5. Technology Stack

- **Frontend**: HTML5, Responsive CSS3 (Medical theme, grid, animations), JavaScript ES6+, DOM manipulation.
- **Asynchronous Communication**: Fetch API / AJAX polling (`setInterval` every 3000ms).
- **Backend**: Java 21, Jakarta Servlets 6.0, JavaServer Pages (JSP 3.1), JSTL.
- **Database & Persistence**: MySQL 8.0, JDBC (PreparedStatements, Transactions, Resource management).
- **State Management**: HTTP Session (credentials & roles), HTTP Cookies (preferences & remember me).
- **XML & Schemas**: XML, DTD (`hospital-config.dtd`), XSD (`hospital-config.xsd`), XML DOM Parser (`DocumentBuilderFactory`), XPath (`javax.xml.xpath.XPath`).
- **Server**: Apache Tomcat 10.1.
- **Build System**: Apache Maven 3.9+.

---

## 6. System Architecture

```
Browser (Patient / Doctor / Admin)
   │
   ├─► HTML5 / CSS3 / JavaScript DOM Engine
   ├─► AJAX Polling via Fetch API (Every 3 seconds)
   ├─► HTTP Cookies (Preferences)
   │
   ▼
Apache Tomcat 10.1 Web Container
   │
   ├─► AuthFilter (Role-based security guard)
   ├─► Jakarta Servlets (Controllers: Login, Booking, Queue, Doctor, XML)
   ├─► JSP Pages (Dynamic server-rendered views)
   ├─► XML Layer (hospital-config.xml, DOM Parser, XPath Service, DTD/XSD Validator)
   ├─► JDBC DAO Layer (Connection pooling, PreparedStatement, Atomic transactions)
   │
   ▼
MySQL 8.0 Database (hospital_queue_db)
```

---

## 7. Database Setup

1. Verify MySQL Server 8.0 is running on port `3306`.
2. Connect to MySQL and run the schema initialization script:
   ```cmd
   mysql -u root -p < database/hospital_queue.sql
   ```
   *(Enter your MySQL root password when prompted)*.
3. Verify connection settings in `src/main/resources/db.properties`.

---

## 8. MySQL Schema

The relational database `hospital_queue_db` contains 8 normalized tables:

1. **`users`**: `user_id`, `full_name`, `email` (UNIQUE), `phone`, `password_hash`, `role` (`PATIENT`, `DOCTOR`, `ADMIN`), timestamps.
2. **`departments`**: `department_id`, `department_name`, `description`, `status` (`ACTIVE`, `INACTIVE`), `created_at`.
3. **`rooms`**: `room_id`, `room_number` (UNIQUE), `department_id`, `status` (`AVAILABLE`, `OCCUPIED`, `MAINTENANCE`).
4. **`doctors`**: `doctor_id`, `user_id` (UNIQUE), `department_id`, `room_id`, `specialization`, `status` (`AVAILABLE`, `BUSY`, `OFFLINE`), `average_consultation_minutes`, `created_at`.
5. **`appointments`**: `appointment_id`, `patient_id`, `doctor_id`, `appointment_date`, `appointment_time`, `booking_source` (`ONLINE`, `WALK_IN`), `status`, timestamps.
6. **`queues`**: `queue_id`, `appointment_id`, `patient_id`, `doctor_id`, `token_number`, `queue_date`, `status` (`BOOKED`, `CHECKED_IN`, `WAITING`, `CALLED`, `IN_CONSULTATION`, `COMPLETED`, `CANCELLED`, `ABSENT`), `booking_source`, timestamps (`created_at`, `checked_in_at`, `called_at`, `consultation_started_at`, `completed_at`).
   - **Key Constraint**: `UNIQUE KEY unique_doctor_date_token (doctor_id, queue_date, token_number)`.
7. **`queue_settings`**: `setting_id`, `doctor_id`, `queue_date`, `starting_token`, `maximum_tokens`, `current_token`, `average_consultation_minutes`.
8. **`doctor_schedules`**: `schedule_id`, `doctor_id`, `day_of_week`, `start_time`, `end_time`, `maximum_patients`, `status`.

---

## 9. XML Structure (`hospital-config.xml`)

Stores baseline hospital operating parameters:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hospital SYSTEM "hospital-config.dtd">
<hospital xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="hospital-config.xsd">
    <name>City Care Hospital</name>
    <queueSettings>
        <defaultAverageConsultationTime>5</defaultAverageConsultationTime>
        <maximumOnlineBookings>30</maximumOnlineBookings>
        <checkInRequired>true</checkInRequired>
    </queueSettings>
    <departments>
        <department id="D01">
            <name>General Medicine</name>
            <room>101</room>
            <averageTime>5</averageTime>
        </department>
        ...
    </departments>
</hospital>
```

---

## 10. DTD (`hospital-config.dtd`)

Validates the element hierarchy and requires the `id` attribute on every department:
```dtd
<!ELEMENT hospital (name, queueSettings, departments)>
<!ATTLIST hospital
    xmlns:xsi CDATA #IMPLIED
    xsi:noNamespaceSchemaLocation CDATA #IMPLIED>
<!ELEMENT name (#PCDATA)>
<!ELEMENT queueSettings (defaultAverageConsultationTime, maximumOnlineBookings, checkInRequired)>
<!ELEMENT defaultAverageConsultationTime (#PCDATA)>
<!ELEMENT maximumOnlineBookings (#PCDATA)>
<!ELEMENT checkInRequired (#PCDATA)>
<!ELEMENT departments (department+)>
<!ELEMENT department (name, room, averageTime)>
<!ATTLIST department id ID #REQUIRED>
<!ELEMENT room (#PCDATA)>
<!ELEMENT averageTime (#PCDATA)>
```

---

## 11. XSD (`hospital-config.xsd`)

Enforces strong typing, positive integers, booleans, and range restrictions:
- `maximumOnlineBookings`: `xs:positiveInteger`
- `checkInRequired`: `xs:boolean`
- `averageTime`: integer restricted between `1` and `120` minutes.
- `department id`: `xs:ID` required attribute.

---

## 12. XML DOM Parser (`HospitalConfigParser.java`)

Demonstrates standard Java XML DOM parsing:
- Uses `DocumentBuilderFactory` and `DocumentBuilder`.
- Parses `hospital-config.xml` into a DOM `Document` tree.
- Traverses `NodeList` and `Element` nodes to retrieve hospital metadata and department definitions into Java models.

---

## 13. XPath (`HospitalXPathService.java`)

Uses `javax.xml.xpath.XPath` to execute queries directly against the configuration:
- Find department by name: `//department[name='Cardiology']`
- Find department by ID attribute: `//department[@id='D02']`
- Filter departments by consultation duration: `//department[averageTime > 5]`
- Interactive execution supported via `/admin/xml-config.jsp`.

---

## 14. AJAX Implementation (`ajax.js` & Fetch API)

Asynchronous requests are implemented without external libraries using the modern Fetch API:
- `HospitalAJAX.getQueueStatus(queueId, doctorId)`: Polls live queue metrics every 3 seconds.
- `HospitalAJAX.getAvailableTokens(doctorId, date)`: Retrieves open token numbers for slot selection.
- `HospitalAJAX.bookToken(...)`: Posts online token reservation.
- `HospitalAJAX.doctorCallNext()`: Calls the next patient in line.
- `HospitalAJAX.doctorStartConsultation(queueId)`: Transitions patient to consultation.
- `HospitalAJAX.doctorCompleteConsultation(queueId)`: Completes consultation and updates statistics.

---

## 15. HTTP Session Implementation

- Created upon successful authentication in `LoginServlet`:
  ```java
  session.setAttribute("userId", user.getUserId());
  session.setAttribute("role", user.getRole());
  session.setAttribute("name", user.getFullName());
  ```
- Checked by `AuthFilter` on all protected endpoints.
- Invalidated upon logout via `LogoutServlet` (`session.invalidate()`).

---

## 16. Cookie Implementation (`CookieUtil.java`)

Meaningful non-sensitive cookies stored in the client browser:
1. `rememberEmail`: Pre-populates the login form when "Remember my email" is checked.
2. `preferredDepartment`: Pre-selects the clinic dropdown on the booking screen.
3. `preferredDoctorId`: Pre-selects the patient's preferred doctor.
4. `preferredLanguage`: Stores UI language choice.
5. `queueDisplayPref`: Stores preferred queue view mode.
*(Zero passwords or sensitive credentials are ever stored in cookies).*

---

## 17. How to Run the Project

### Prerequisites
- JDK 21
- Apache Maven 3.9+
- MySQL 8.0
- Apache Tomcat 10.1

### Step 1: Database Initialization
```cmd
cd c:\WT_micro_project
mysql -u root -p < database\hospital_queue.sql
```

### Step 2: Build & Deploy
Run the included deployment script:
```cmd
deploy.bat
```
*(This cleans, compiles, packages `hospital-queue.war`, and copies it to Tomcat's `webapps/` directory).*

### Step 3: Start Tomcat Server
If Tomcat is not already running as a service, start it using:
```cmd
run.bat
```
Server starts at: **`http://localhost:8080/hospital-queue/`**

---

## 18. Demo Credentials

| Role | Email | Password | Details |
|---|---|---|---|
| **Admin** | `admin@hospital.com` | `Admin@123` | Hospital Administrator |
| **Doctor** | `kumar@hospital.com` | `Doctor@123` | Dr. Kumar &bull; General Medicine (Room 101) |
| **Doctor** | `priya@hospital.com` | `Doctor@123` | Dr. Priya &bull; Cardiology (Room 202) |
| **Doctor** | `arun@hospital.com` | `Doctor@123` | Dr. Arun &bull; Orthopedics (Room 305) |
| **Patient** | `arun.patient@gmail.com` | `Patient@123` | Patient Arun (Pre-booked Token 25) |
| **Patient** | `meena.patient@gmail.com` | `Patient@123` | Patient Meena (Token 21) |

*(Quick-fill buttons are provided on the login page for one-click testing)*.

---

## 19. Important URLs

- Landing Page: `http://localhost:8080/hospital-queue/`
- Login: `http://localhost:8080/hospital-queue/auth/login.jsp`
- Register: `http://localhost:8080/hospital-queue/auth/register.jsp`
- Patient Dashboard: `http://localhost:8080/hospital-queue/patient/dashboard.jsp`
- Book Online Token: `http://localhost:8080/hospital-queue/patient/book-token.jsp`
- Public Live Queue Screen: `http://localhost:8080/hospital-queue/patient/live-queue.jsp`
- Patient History: `http://localhost:8080/hospital-queue/patient/appointments.jsp`
- Doctor Console: `http://localhost:8080/hospital-queue/doctor/dashboard.jsp`
- Admin Dashboard: `http://localhost:8080/hospital-queue/admin/dashboard.jsp`
- Admin XML/XPath Console: `http://localhost:8080/hospital-queue/admin/xml-config.jsp`

---

## 20. Real-Time Two-Browser Demonstration Steps

1. **Browser 1 (Patient)**:
   - Go to `http://localhost:8080/hospital-queue/auth/login.jsp`.
   - Click **Patient (Arun)** and click **Sign In**.
   - Dashboard opens showing:
     - **Current Serving Token: 20**
     - **Your Token: 25**
     - **People Ahead: 4**
     - **Estimated Waiting Time: 20 mins**
     - Status: `WAITING`
2. **Browser 2 (Doctor)** (e.g. Incognito or separate browser):
   - Go to `http://localhost:8080/hospital-queue/auth/login.jsp`.
   - Click **Doctor (Dr. Kumar)** and click **Sign In**.
   - Doctor dashboard opens showing Currently Serving: None, Waiting: 5.
3. **Execution**:
   - In Browser 2, click **CALL NEXT PATIENT**.
   - Token 21 is called.
   - **Look at Browser 1 (Patient)**: Within 3 seconds, without touching or refreshing the page, Current Serving updates to `21`, People Ahead updates to `3`, and Estimated Wait updates to `15 mins`.
   - In Browser 2, click **CALL NEXT PATIENT** again (Token 22 called).
   - In Browser 1: People Ahead updates to `2`, Estimated Wait updates to `10 mins`.
   - Call Token 23: People Ahead updates to `1`.
   - Call Token 24: Notification banner pulses: **"YOU ARE NEXT"**.
   - Call Token 25: Patient dashboard updates to:
     - **"YOUR TOKEN IS CALLED. PLEASE PROCEED TO ROOM 101."** (with gentle alert tone).
   - In Browser 2, Doctor clicks **Start Consultation** &rarr; Patient status updates to `IN_CONSULTATION`.
   - Doctor clicks **Complete Consultation** &rarr; Patient status updates to `COMPLETED`.

---

## 21. Project Limitations

- **Simulated Healthcare Environment**: Built for academic demonstration; does not connect to real Electronic Health Record (EHR) systems, real payment gateways, or live SMS gateways.
- **Polling Frequency**: Uses 3-second AJAX polling over HTTP rather than WebSockets to strictly fulfill the Web Technology laboratory curriculum requirements.
