export interface Course {
  id: number;
  code: string;
  name: string;
  description?: string;
  credits: number;
  hoursPerWeek: number;
  courseType: string;
  gradeLevelMin: number;
  gradeLevelMax: number;
  semesterOrder: number;
  specializationName?: string;
  prerequisiteId?: number;
  prerequisiteCode?: string;
  prerequisiteName?: string;
}

export interface CourseHistoryItem {
  courseId: number;
  courseCode: string;
  courseName: string;
  credits: number;
  status: 'passed' | 'failed';
  semesterName: string;
  semesterYear: number;
}

export interface Student {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  gradeLevel: number;
  enrollmentYear: number;
  expectedGraduationYear: number;
  status: string;
  gpa?: number;
  creditsEarned?: number;
  creditsRequired?: number;
  graduationEligible?: boolean;
  courseHistory?: CourseHistoryItem[];
}

export interface CourseSection {
  id: number;
  courseId: number;
  courseCode: string;
  courseName: string;
  courseType: string;
  credits: number;
  gradeLevelMin: number;
  gradeLevelMax: number;
  prerequisiteId?: number;
  prerequisiteCode?: string;
  prerequisiteName?: string;
  semesterId: number;
  semesterName: string;
  semesterYear: number;
  teacherId: number;
  teacherFullName: string;
  classroomId: number;
  classroomName: string;
  classroomCapacity: number;
  daysOfWeek: string;
  startTime: string;
  endTime: string;
}

export interface Schedule {
  studentId: number;
  studentFullName: string;
  gradeLevel: number;
  totalEnrolled: number;
  maxAllowed: number;
  sections: CourseSection[];
}

export interface EnrollmentResponse {
  id: number;
  studentId: number;
  studentFullName: string;
  courseSection: CourseSection;
}

export interface ApiError {
  error: string;
}