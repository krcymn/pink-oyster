import { create } from 'zustand';
import { Course, Student, CourseSection, Schedule } from './types';
import { coursesApi, studentsApi, sectionsApi, enrollmentsApi } from './api';

interface AppState {
  courses: Course[];
  sections: CourseSection[];
  currentStudent: Student | null;
  schedule: Schedule | null;
  selectedStudentId: number;
  loading: boolean;
  error: string | null;
  successMessage: string | null;

  setSelectedStudentId: (id: number) => void;
  fetchCourses: (filters?: { gradeLevel?: number; semesterOrder?: number; courseType?: string }) => Promise<void>;
  fetchSections: () => Promise<void>;
  fetchStudentProfile: (id: number) => Promise<void>;
  fetchSchedule: (studentId: number) => Promise<void>;
  enroll: (studentId: number, sectionId: number) => Promise<void>;
  unenroll: (studentId: number, sectionId: number) => Promise<void>;
  clearMessages: () => void;
}

export const useAppStore = create<AppState>((set) => ({
  courses: [],
  sections: [],
  currentStudent: null,
  schedule: null,
  selectedStudentId: 1,
  loading: false,
  error: null,
  successMessage: null,

  setSelectedStudentId: (id) => set({ selectedStudentId: id }),

  fetchCourses: async (filters) => {
    set({ loading: true, error: null });
    try {
      const courses = await coursesApi.getAll(filters);
      set({ courses, loading: false });
    } catch (e: any) {
      set({ error: e.message, loading: false });
    }
  },

  fetchSections: async () => {
    set({ loading: true, error: null });
    try {
      const sections = await sectionsApi.getAll();
      set({ sections, loading: false });
    } catch (e: any) {
      set({ error: e.message, loading: false });
    }
  },

  fetchStudentProfile: async (id) => {
    set({ loading: true, error: null });
    try {
      const student = await studentsApi.getProfile(id);
      set({ currentStudent: { ...student }, loading: false });
    } catch (e: any) {
      set({ error: e.message, loading: false });
    }
  },

  fetchSchedule: async (studentId) => {
    set({ loading: true, error: null });
    try {
      const schedule = await enrollmentsApi.getSchedule(studentId);
      set({ schedule: { ...schedule }, loading: false });
    } catch (e: any) {
      set({ error: e.message, loading: false });
    }
  },

  enroll: async (studentId, sectionId) => {
    set({ loading: true, error: null, successMessage: null });
    try {
      await enrollmentsApi.enroll(studentId, sectionId);
      const schedule = await enrollmentsApi.getSchedule(studentId);
      set({
        schedule: { ...schedule },
        loading: false,
        successMessage: 'Successfully enrolled in course!',
      });
    } catch (e: any) {
      set({ error: e.message, loading: false });
    }
  },

  unenroll: async (studentId, sectionId) => {
    set({ loading: true, error: null, successMessage: null });
    try {
      await enrollmentsApi.unenroll(studentId, sectionId);
      const schedule = await enrollmentsApi.getSchedule(studentId);
      set({
        schedule: { ...schedule },
        loading: false,
        successMessage: 'Successfully dropped course.',
      });
    } catch (e: any) {
      set({ error: e.message, loading: false });
    }
  },

  clearMessages: () => set({ error: null, successMessage: null }),
}));