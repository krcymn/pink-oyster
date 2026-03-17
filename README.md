# Maplewood High School — Course Planning System

A full-stack web application that allows students to browse courses, build their semester schedule, and track graduation progress.

---

## System Overview

This system models a real-world high school course enrollment process.

- **Courses** are abstract curriculum items (e.g. "Algebra I")
- **CourseSections** represent scheduled offerings of a course (time, teacher, classroom)
- **Students enroll in sections**, not courses — enabling conflict detection and capacity management
- **Validation rules** enforce schedule correctness and academic constraints at the service layer

---

## How to Use

1. Open the app at `http://localhost:3000`
2. Select a student from the dropdown at the top
3. View your GPA, credits earned, and graduation progress on the left panel
4. Check your current schedule on the right panel
5. Browse available course sections in the Course Browser below
6. Click **+ Add** to enroll — validation errors appear in real-time if a rule is violated
7. Click **Drop** in your schedule to remove a course

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA |
| Database | SQLite (via Hibernate Community Dialects) |
| Frontend | React 18, TypeScript, Zustand |
| Testing | JUnit 5, Mockito |
| API Docs | SpringDoc OpenAPI (Swagger) |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 20+

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

Backend starts at `http://localhost:8080`

> **Note:** `maplewood_school.sqlite` must be in the project root (one level above `backend/`). On first run, `course_sections` and `enrollments` tables are created automatically, and 57 course sections are populated via `DataInitializer`.

### 2. Frontend

```bash
cd frontend
npm install
npm start
```

Frontend starts at `http://localhost:3000`

### 3. Run Tests

```bash
cd backend
mvn test
```

87 tests — 0 failures.

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

### Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/courses` | List courses (filter: gradeLevel, semesterOrder, courseType) |
| GET | `/api/students/{id}` | Student profile with GPA, credits, course history |
| GET | `/api/sections` | Active semester course sections |
| GET | `/api/enrollments/schedule/{studentId}` | Student's current schedule |
| POST | `/api/enrollments` | Enroll in a course section |
| DELETE | `/api/enrollments/{studentId}/{sectionId}` | Drop a course |

### Example: Enroll in a Course

**Request:**
```
POST /api/enrollments
Content-Type: application/json

{
  "studentId": 1,
  "sectionId": 5
}
```

**Success Response (201 Created):**
```json
{
  "id": 42,
  "studentId": 1,
  "studentFullName": "Laura Gonzalez",
  "courseSection": {
    "courseCode": "MAT101",
    "courseName": "Algebra I",
    "daysOfWeek": "MON,WED,FRI",
    "startTime": "09:00",
    "endTime": "10:00"
  }
}
```

**Error Response (409 Conflict):**
```json
{
  "timestamp": "2026-03-17T22:00:00",
  "errorCode": "PREREQ_MISSING",
  "message": "Prerequisite not met: must pass 'English I: Foundations' first",
  "suggestedAction": "Enroll in course 'ENG101' (ID: 1) first"
}
```

---

## Architecture

### Backend

```
com.maplewood/
├── controller/     # REST endpoints — thin layer, no business logic
├── service/        # Business logic
├── repository/     # Spring Data JPA repositories
├── model/          # JPA entities
├── dto/            # Data Transfer Objects (entities never exposed directly)
├── validation/     # Chain of Responsibility enrollment validators
├── mapper/         # MapStruct mappers (entity → DTO)
├── exception/      # Custom exceptions + GlobalExceptionHandler
├── aspect/         # AOP logging for enrollment operations
└── config/         # WebConfig (CORS), DataInitializer
```

### Enrollment Validation — Chain of Responsibility

Each business rule is a separate, independently testable validator ordered by performance (cheapest first via `@Order`):

```
@Order(1) GradeLevelValidator        → Is the student in the right grade?
@Order(2) LunchHourValidator         → Does the section overlap 12:00–13:00? *
@Order(3) MaxCoursesValidator        → Has the student reached the 5-course limit?
@Order(4) AlreadyEnrolledValidator   → Is the student already enrolled?
@Order(5) ClassroomCapacityValidator → Is the classroom full? (max 10)
@Order(6) PrerequisiteValidator      → Has the student passed the prerequisite?
@Order(7) TimeConflictValidator      → Does it conflict with existing schedule?
```

> \* Lunch hour protection ensures no classes are scheduled during 12:00–13:00, respecting both student and teacher break rights.

Adding a new rule requires only a new class — no changes to `EnrollmentService` (Open/Closed Principle).

### Frontend State Management — Zustand

```
store.ts
├── state:   courses, sections, currentStudent, schedule, loading, error, successMessage
└── actions: fetchCourses, fetchSections, fetchStudentProfile,
             fetchSchedule, enroll, unenroll, clearMessages
```

Components never call the API directly — all async operations go through the store, ensuring predictable state updates and consistent loading/error handling.

---

## Database Design

### Entity Relationship

```
students ──< student_course_history >── courses
students ──< enrollments >── course_sections >── courses
course_sections ──> semesters
course_sections ──> teachers
course_sections ──> classrooms
courses ──> specializations
teachers ──> specializations
classrooms ──> room_types
```

### Existing Tables (pre-populated)

| Table | Records | Description |
|-------|---------|-------------|
| `students` | 400 | 100 per grade level (9–12) |
| `courses` | 57 | 20 core + 37 elective |
| `student_course_history` | ~6,700 | Historical academic records |
| `semesters` | 9 | 6 historical + current + 2 future |
| `teachers` | 50 | Distributed across 9 specializations |
| `classrooms` | 60 | Various types (lab, gym, studio…) |

### Added Tables

**`course_sections`** — A scheduled offering of a course for a specific semester.

> **Why separate course from section?** A course is a curriculum item ("Algebra I"). A section is a scheduled instance — specific time, classroom, and teacher. This separation allows multiple sections per course and enables time conflict detection.

**`enrollments`** — A student's enrollment in a specific section.

---

## Business Rules

| Rule | Implementation |
|------|----------------|
| Max 5 courses/semester | `MaxCoursesValidator` |
| Prerequisite must be passed | `PrerequisiteValidator` |
| No time conflicts | `TimeConflictValidator` |
| Grade level appropriate | `GradeLevelValidator` |
| No lunch hour classes (12–13) | `LunchHourValidator` |
| Classroom capacity (max 10) | `ClassroomCapacityValidator` |
| No duplicate enrollment | `AlreadyEnrolledValidator` |
| 30 credits to graduate | `GpaCalculator.isGraduationEligible()` |

---

## Concurrency Considerations

Enrollment operations are handled within `@Transactional` boundaries to ensure consistency when multiple requests occur simultaneously. The unique constraint on `(student_id, course_section_id)` in the `enrollments` table provides an additional database-level guard against duplicate enrollments under concurrent load.

---

## Error Handling

All errors return a consistent JSON format with an actionable suggestion:

```json
{
  "timestamp": "2026-03-17T22:00:00",
  "errorCode": "TIME_CONFLICT",
  "message": "Time conflict with 'ENG101' (MON,WED,FRI 09:00-10:00)",
  "suggestedAction": "Choose a section with a different time slot"
}
```

Error codes: `PREREQ_MISSING`, `TIME_CONFLICT`, `MAX_COURSES_REACHED`, `GRADE_LEVEL_MISMATCH`, `CLASSROOM_FULL`, `ALREADY_ENROLLED`, `LUNCH_HOUR_CONFLICT`, `NOT_FOUND`, `VALIDATION_ERROR`.

---

## Trade-offs & Decisions

**SQLite over PostgreSQL** — Zero-setup for local development. The JPA/Hibernate abstraction means switching to PostgreSQL requires only a dependency and config change.

**Zustand over Redux** — Simpler API for this scale. Redux would add boilerplate without meaningful benefit for a single-user application.

**Chain of Responsibility for validation** — Each rule is independently testable and ordered by performance (memory checks before DB queries). Adding new rules requires no changes to existing code.

**String for time fields** — SQLite stores `LocalTime` as nanoseconds causing display issues. Stored as `"09:00"` strings and parsed with `LocalTime.parse()` on demand.

**Single prerequisite per course** — The data model supports one prerequisite chain (MAT101 → MAT102 → MAT201). A graph-based approach would be needed for multiple prerequisites or OR-logic, but is unnecessary for the current schema.

---

## Testing

```bash
mvn test
# Tests run: 87, Failures: 0, Errors: 0
```

| Test Class | Coverage |
|------------|---------|
| `EnrollmentServiceTest` | All 7 validation scenarios + unenroll |
| `GpaCalculatorTest` | GPA, credits earned, graduation eligibility |
| `TimeConflictValidatorTest` | Same days, partial overlap, back-to-back, different days |
| `GradeLevelValidatorTest` | Min/max boundary, above/below |
| `LunchHourValidatorTest` | Before, during, spanning, after lunch |
| `MaxCoursesValidatorTest` | 0, 4, 5 (limit), 6 courses |
| `AlreadyEnrolledValidatorTest` | Enrolled vs not enrolled |
| `ClassroomCapacityValidatorTest` | Empty, below, at, above capacity |
| `PrerequisiteValidatorTest` | No prereq, met, not met |
| `CourseServiceTest` | getAll, getById, filters |
| `StudentServiceTest` | Profile, GPA calculation, graduation |
| `CourseSectionServiceTest` | Active semester sections, not found |
