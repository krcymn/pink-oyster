import React, { useEffect, useState } from 'react';
import { useAppStore } from '../store';
import { CourseSection } from '../types';

const Schedule: React.FC = () => {
  const { schedule, selectedStudentId, fetchSchedule, unenroll, loading } = useAppStore();
  const [sections, setSections] = useState<CourseSection[]>([]);

  useEffect(() => {
    fetchSchedule(selectedStudentId);
  }, [selectedStudentId]);

  // schedule değişince sections'ı güncelle
  useEffect(() => {
    if (schedule) {
      setSections([...schedule.sections]);
    }
  }, [schedule]);

  if (!schedule) return <div style={card}>Loading schedule...</div>;

  return (
    <div style={card}>
      <h2 style={{ margin: '0 0 12px' }}>
        Current Schedule
        <span style={{ fontWeight: 'normal', fontSize: 14, marginLeft: 10 }}>
          {sections.length} / {schedule.maxAllowed} courses
        </span>
      </h2>

      {sections.length === 0 ? (
        <p style={{ color: '#999' }}>No courses enrolled yet. Browse courses below to add some.</p>
      ) : (
        <table style={tableStyle}>
          <thead>
            <tr>
              {['Code', 'Course', 'Credits', 'Days', 'Time', 'Teacher', 'Room', ''].map((h) => (
                <th key={h} style={thStyle}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {sections.map((s) => (
              <tr key={s.id}>
                <td style={tdStyle}><strong>{s.courseCode}</strong></td>
                <td style={tdStyle}>{s.courseName}</td>
                <td style={tdStyle}>{s.credits}</td>
                <td style={tdStyle}>{s.daysOfWeek}</td>
                <td style={tdStyle}>{s.startTime} – {s.endTime}</td>
                <td style={tdStyle}>{s.teacherFullName}</td>
                <td style={tdStyle}>{s.classroomName}</td>
                <td style={tdStyle}>
                  <button
                    onClick={() => unenroll(selectedStudentId, s.id)}
                    disabled={loading}
                    style={dropBtn}
                  >
                    Drop
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

const card: React.CSSProperties = { border: '1px solid black', padding: 16, marginBottom: 16 };
const tableStyle: React.CSSProperties = { width: '100%', borderCollapse: 'collapse', fontSize: 13 };
const thStyle: React.CSSProperties = { border: '1px solid black', padding: '4px 8px', background: 'black', color: 'white', textAlign: 'left' };
const tdStyle: React.CSSProperties = { border: '1px solid #ccc', padding: '4px 8px' };
const dropBtn: React.CSSProperties = { padding: '2px 10px', border: '1px solid black', background: 'white', cursor: 'pointer', fontSize: 12 };

export default Schedule;