import React, { useEffect, useState } from 'react';
import { studentsApi } from '../api';
import { Student } from '../types';
import { useAppStore } from '../store';

const StudentSelector: React.FC = () => {
  const [students, setStudents] = useState<Student[]>([]);
  const { selectedStudentId, setSelectedStudentId, fetchStudentProfile, fetchSchedule } = useAppStore();

  useEffect(() => {
    studentsApi.getAll().then(setStudents);
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const id = Number(e.target.value);
    setSelectedStudentId(id);
    fetchStudentProfile(id);
    fetchSchedule(id);
  };

  return (
    <div style={{ padding: '12px 0', borderBottom: '2px solid black' }}>
      <label style={{ fontWeight: 'bold', marginRight: 8 }}>Student:</label>
      <select value={selectedStudentId} onChange={handleChange} style={selectStyle}>
        {students.map((s) => (
          <option key={s.id} value={s.id}>
            {s.firstName} {s.lastName} — Grade {s.gradeLevel}
          </option>
        ))}
      </select>
    </div>
  );
};

const selectStyle: React.CSSProperties = {
  padding: '6px 10px',
  border: '1px solid black',
  fontSize: 14,
  minWidth: 300,
};

export default StudentSelector;