import axios from 'axios';
import { Course, Student, CourseSection, Schedule, EnrollmentResponse } from './types';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const data = error.response?.data;
    const message = data?.message || data?.error || error.message || 'Unknown error';
    return Promise.reject(new Error(message));
  }
);

export const coursesApi = {
  getAll: (params?: { gradeLevel?: number; semesterOrder?: number; courseType?: string }) =>
    api.get<Course[]>('/courses', { params }).then((r) => r.data),
  getById: (id: number) =>
    api.get<Course>(`/courses/${id}`).then((r) => r.data),
};

export const studentsApi = {
  getAll: () => api.get<Student[]>('/students').then((r) => r.data),
  getProfile: (id: number) => api.get<Student>(`/students/${id}`).then((r) => r.data),
};

export const sectionsApi = {
  getAll: () => api.get<CourseSection[]>('/sections').then((r) => r.data),
  getById: (id: number) => api.get<CourseSection>(`/sections/${id}`).then((r) => r.data),
};

export const enrollmentsApi = {
  getSchedule: (studentId: number) =>
    api.get<Schedule>(`/enrollments/schedule/${studentId}`).then((r) => r.data),
  enroll: (studentId: number, sectionId: number) =>
    api.post<EnrollmentResponse>('/enrollments', { studentId, sectionId }).then((r) => r.data),
  unenroll: (studentId: number, sectionId: number) =>
    api.delete(`/enrollments/${studentId}/${sectionId}`).then((r) => r.data),
};