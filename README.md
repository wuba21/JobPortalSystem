# JavaFX Online Job Portal System

## Prerequisites
- Java JDK 17 or higher
- MySQL 8.0+
- Apache NetBeans IDE (12.0+)
- Maven (bundled with NetBeans)

## Setup Instructions

### Step 1: Database Setup
1. Open MySQL Workbench or command line
2. Run the SQL script: `src/main/resources/db/schema.sql`
3. This creates the `job_portal` database with all tables and a default admin user

### Step 2: Configure Database Connection
1. Open `src/main/java/com/jobportal/config/DBConnection.java`
2. Update the connection settings:
   - `URL` - your MySQL URL (default: `jdbc:mysql://localhost:3306/job_portal`)
   - `USERNAME` - your MySQL username (default: `root`)
   - `PASSWORD` - your MySQL password (default: empty)

### Step 3: Open in NetBeans
1. Open NetBeans IDE
2. File → Open Project → Select the `JobPortalSystem` folder
3. NetBeans will recognize it as a Maven project
4. Right-click project → Build (to download dependencies)
5. Right-click project → Run

### Step 4: Default Login
- **Admin**: admin@jobportal.com / admin123
- Register new accounts as Job Seeker or Employer

## Project Structure
```
src/main/java/com/jobportal/
├── MainApp.java          - Application entry point
├── config/               - Database configuration
├── model/                - Entity classes (User, Job, Application, Employer, Admin)
├── dao/                  - Data Access Objects (interfaces + implementations)
├── service/              - Business logic layer
├── controller/           - JavaFX FXML controllers
├── util/                 - Utilities (alerts, validation, session)
└── exception/            - Custom exceptions

src/main/resources/
├── fxml/                 - UI layout files (login, register, dashboard, job-list, job-form)
├── css/                  - Stylesheets
├── images/               - Application images
└── db/                   - SQL schema script
```

## Features
- User registration and login (Job Seeker, Employer, Admin)
- Job posting and management (Employer)
- Job search with filters (keyword, location, type)
- Job application with cover letter
- Application status tracking
- Role-based dashboards
- Modern UI with CSS styling
