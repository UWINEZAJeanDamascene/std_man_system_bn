# Student Attendance Management System

A comprehensive web application for managing student attendance in educational institutions, built with Spring Boot backend and React frontend.

## Features

### 🔐 Authentication
- Secure JWT-based authentication
- User registration and login
- Session management

### 👨‍🎓 Student Management
- Add, edit, delete, and view student records
- Student information including name, email, class, and phone
- Filter students by class

### 📚 Course Management
- Create and manage courses/subjects
- Assign lecturers to courses
- Enroll/unenroll students in courses
- View course details and enrolled students

### 📝 Attendance Recording
- Mark attendance for course sessions (Present/Absent/Late)
- Date-based attendance tracking
- Bulk attendance marking for entire classes
- Update existing attendance records

### 📊 Reports & Analytics
- Generate student-specific attendance reports
- Course-wise attendance summaries
- Attendance percentage calculations
- Detailed attendance history

### 🎨 User Interface
- Modern, responsive Material-UI design
- Intuitive navigation and user experience
- Real-time data updates
- Error handling and validation

## Technology Stack

### Backend
- **Spring Boot 3.2.0** - Java framework
- **Spring Data JPA** - Database access
- **Spring Security** - Authentication and authorization
- **MySQL** - Database
- **JWT** - Token-based authentication
- **Maven** - Build tool

### Frontend
- **React 18** - JavaScript library
- **Material-UI** - UI component library
- **Axios** - HTTP client
- **React Router** - Client-side routing
- **npm** - Package manager

## Database Schema

```
users (lecturers/admins)
├── id, name, email, password, role

students
├── id, name, email, student_class, phone

courses
├── id, course_name, lecturer_id

enrollments (junction table)
├── course_id, student_id

attendance_records
├── id, student_id, course_id, date, status, lecturer_id
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Students
- `GET /api/students` - Get all students
- `GET /api/students/{id}` - Get student by ID
- `POST /api/students` - Create new student
- `PUT /api/students/{id}` - Update student
- `DELETE /api/students/{id}` - Delete student
- `GET /api/students/class/{class}` - Get students by class

### Courses
- `GET /api/courses` - Get all courses
- `GET /api/courses/{id}` - Get course by ID
- `POST /api/courses` - Create new course
- `PUT /api/courses/{id}` - Update course
- `DELETE /api/courses/{id}` - Delete course
- `GET /api/courses/lecturer/{lecturerId}` - Get courses by lecturer
- `POST /api/courses/{courseId}/enroll/{studentId}` - Enroll student
- `DELETE /api/courses/{courseId}/unenroll/{studentId}` - Unenroll student

### Attendance
- `GET /api/attendance/course/{courseId}?date={date}` - Get attendance by course and date
- `POST /api/attendance/course/{courseId}` - Mark attendance
- `GET /api/attendance/student/{studentId}` - Get student's attendance
- `GET /api/attendance/course/{courseId}/all` - Get all attendance for course
- `PUT /api/attendance/{id}` - Update attendance record

### Reports
- `GET /api/reports/student/{studentId}` - Generate student report
- `GET /api/reports/course/{courseId}` - Generate course report

## Installation & Setup

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher
- Node.js 16 or higher
- npm or yarn

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Configure MySQL database in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/student_management?createDatabaseIfNotExist=true
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the React development server:
   ```bash
   npm start
   ```

4. Open [http://localhost:3000](http://localhost:3000) in your browser

## Usage

1. **Login/Register**: Create an account or login with existing credentials
2. **Dashboard**: View system overview and statistics
3. **Students**: Manage student records (add, edit, delete, view)
4. **Courses**: Create courses and manage student enrollments
5. **Attendance**: Mark attendance for course sessions
6. **Reports**: Generate and view attendance reports

## Development

### Project Structure
```
student_management/
├── backend/
│   ├── src/main/java/com/studentmanagement/
│   │   ├── controller/     # REST controllers
│   │   ├── model/         # JPA entities
│   │   ├── repository/    # Data repositories
│   │   ├── service/       # Business logic
│   │   ├── security/      # JWT and security config
│   │   └── config/        # Application configuration
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/    # Reusable UI components
│   │   ├── pages/         # Page components
│   │   ├── services/      # API services
│   │   └── utils/         # Utility functions
│   ├── public/
│   └── package.json
└── README.md
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the GitHub repository.
