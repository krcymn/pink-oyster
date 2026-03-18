import React, { useEffect } from 'react';
import { useAppStore } from '../store';

const StudentDashboard: React.FC = () => {
  const { currentStudent, selectedStudentId, fetchStudentProfile } = useAppStore();

  useEffect(() => {
    fetchStudentProfile(selectedStudentId);
  }, [selectedStudentId]);

  if (!currentStudent) return <div style={card}>Loading student...</div>;

  const { gpa, creditsEarned, creditsRequired, graduationEligible, courseHistory } = currentStudent;
  const creditPercent = Math.min(((creditsEarned || 0) / (creditsRequired || 30)) * 100, 100);

  return (
    <div style={card}>
      <h2 style={{ margin: '0 0 12px' }}>
        {currentStudent.firstName} {currentStudent.lastName}
        <span style={{ fontWeight: 'normal', fontSize: 14, marginLeft: 10 }}>
          Grade {currentStudent.gradeLevel} · {currentStudent.email}
        </span>
      </h2>

      <div style={{ display: 'flex', gap: 32, marginBottom: 16 }}>
        <Stat label="GPA" value={gpa?.toFixed(2) || '—'} />
        <Stat label="Credits Earned" value={`${creditsEarned || 0} / ${creditsRequired || 30}`} />
        <Stat label="Graduation" value={graduationEligible ? '✅ Eligible' : '❌ Not Yet'} />
        <Stat label="Expected Grad" value={String(currentStudent.expectedGraduationYear)} />
      </div>

      <div style={{ marginBottom: 16 }}>
        <div style={{ fontSize: 12, marginBottom: 4 }}>Credit Progress</div>
        <div style={{ background: '#eee', height: 12, border: '1px solid black' }}>
          <div style={{ background: 'black', height: '100%', width: `${creditPercent}%` }} />
        </div>
        <div style={{ fontSize: 11, marginTop: 2 }}>{creditPercent.toFixed(0)}%</div>
      </div>

      <details>
        <summary style={{ cursor: 'pointer', fontWeight: 'bold', marginBottom: 8 }}>
          Course History ({courseHistory?.length || 0} courses)
        </summary>
        <table style={tableStyle}>
          <thead>
            <tr>
              {['Code', 'Name', 'Credits', 'Semester', 'Status'].map((h) => (
                <th key={h} style={thStyle}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {courseHistory?.map((h, i) => (
              <tr key={i} style={{ background: h.status === 'failed' ? '#f5f5f5' : 'white' }}>
                <td style={tdStyle}>{h.courseCode}</td>
                <td style={tdStyle}>{h.courseName}</td>
                <td style={tdStyle}>{h.credits}</td>
                <td style={tdStyle}>{h.semesterName} {h.semesterYear}</td>
                <td style={{ ...tdStyle, fontWeight: 'bold', color: h.status === 'passed' ? 'black' : '#999' }}>
                  {h.status.toUpperCase()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </details>
    </div>
  );
};

const Stat: React.FC<{ label: string; value: string }> = ({ label, value }) => (
  <div>
    <div style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: 1 }}>{label}</div>
    <div style={{ fontSize: 20, fontWeight: 'bold' }}>{value}</div>
  </div>
);

const card: React.CSSProperties = { border: '1px solid black', padding: 16, marginBottom: 16 };
const tableStyle: React.CSSProperties = { width: '100%', borderCollapse: 'collapse', fontSize: 13 };
const thStyle: React.CSSProperties = { border: '1px solid black', padding: '4px 8px', background: 'black', color: 'white', textAlign: 'left' };
const tdStyle: React.CSSProperties = { border: '1px solid #ccc', padding: '4px 8px' };

export default StudentDashboard;