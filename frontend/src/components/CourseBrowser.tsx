import React, { useEffect, useState } from 'react';
import { useAppStore } from '../store';

const CourseBrowser: React.FC = () => {
  const { sections, fetchSections, enroll, selectedStudentId, schedule, loading } = useAppStore();

  const [filterType, setFilterType] = useState('');
  const [filterGrade, setFilterGrade] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchSections();
  }, []);

  const enrolledIds = new Set(schedule?.sections.map((s) => s.id) || []);

  const filtered = sections.filter((s) => {
    if (filterType && s.courseType !== filterType) return false;
    if (filterGrade) {
      const g = Number(filterGrade);
      if (s.gradeLevelMin > g || s.gradeLevelMax < g) return false;
    }
    if (search) {
      const q = search.toLowerCase();
      if (!s.courseCode.toLowerCase().includes(q) && !s.courseName.toLowerCase().includes(q)) return false;
    }
    return true;
  });

  return (
    <div style={card}>
      <h2 style={{ margin: '0 0 12px' }}>Course Browser</h2>

      <div style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap' }}>
        <input
          placeholder="Search code or name..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={inputStyle}
        />
        <select value={filterType} onChange={(e) => setFilterType(e.target.value)} style={inputStyle}>
          <option value="">All Types</option>
          <option value="core">Core</option>
          <option value="elective">Elective</option>
        </select>
        <select value={filterGrade} onChange={(e) => setFilterGrade(e.target.value)} style={inputStyle}>
          <option value="">All Grades</option>
          {[9, 10, 11, 12].map((g) => (
            <option key={g} value={g}>Grade {g}</option>
          ))}
        </select>
        <span style={{ alignSelf: 'center', fontSize: 13, color: '#666' }}>
          {filtered.length} courses
        </span>
      </div>

      <div style={{ overflowX: 'auto' }}>
        <table style={tableStyle}>
          <thead>
            <tr>
              {['Code', 'Name', 'Type', 'Credits', 'Grade', 'Prerequisite', 'Days', 'Time', 'Teacher', ''].map((h) => (
                <th key={h} style={thStyle}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {filtered.map((s) => {
              const isEnrolled = enrolledIds.has(s.id);
              const isFull = (schedule?.totalEnrolled || 0) >= (schedule?.maxAllowed || 5);

              return (
                <tr key={s.id} style={{ background: isEnrolled ? '#f0f0f0' : 'white' }}>
                  <td style={tdStyle}><strong>{s.courseCode}</strong></td>
                  <td style={tdStyle}>{s.courseName}</td>
                  <td style={tdStyle}>{s.courseType}</td>
                  <td style={tdStyle}>{s.credits}</td>
                  <td style={tdStyle}>{s.gradeLevelMin}–{s.gradeLevelMax}</td>
                  <td style={tdStyle}>
                    {s.prerequisiteCode
                      ? <span title={s.prerequisiteName || ''}>{s.prerequisiteCode}</span>
                      : <span style={{ color: '#999' }}>—</span>}
                  </td>
                  <td style={tdStyle}>{s.daysOfWeek}</td>
                  <td style={tdStyle}>{s.startTime}–{s.endTime}</td>
                  <td style={tdStyle}>{s.teacherFullName}</td>
                  <td style={tdStyle}>
                    {isEnrolled ? (
                      <span style={{ color: '#666', fontSize: 12 }}>✓ Enrolled</span>
                    ) : (
                      <button
                        onClick={() => enroll(selectedStudentId, s.id)}
                        disabled={loading || isFull}
                        style={{ ...addBtn, opacity: isFull ? 0.4 : 1 }}
                        title={isFull ? 'Max 5 courses reached' : 'Enroll'}
                      >
                        + Add
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

const card: React.CSSProperties = { border: '1px solid black', padding: 16, marginBottom: 16 };
const tableStyle: React.CSSProperties = { width: '100%', borderCollapse: 'collapse', fontSize: 12 };
const thStyle: React.CSSProperties = { border: '1px solid black', padding: '4px 8px', background: 'black', color: 'white', textAlign: 'left', whiteSpace: 'nowrap' };
const tdStyle: React.CSSProperties = { border: '1px solid #ccc', padding: '4px 8px', whiteSpace: 'nowrap' };
const inputStyle: React.CSSProperties = { padding: '6px 8px', border: '1px solid black', fontSize: 13 };
const addBtn: React.CSSProperties = { padding: '2px 10px', border: '1px solid black', background: 'black', color: 'white', cursor: 'pointer', fontSize: 12 };

export default CourseBrowser;