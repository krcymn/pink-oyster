import React, { useEffect } from 'react';
import StudentSelector from './components/StudentSelector';
import StudentDashboard from './components/StudentDashboard';
import Schedule from './components/Schedule';
import CourseBrowser from './components/CourseBrowser';
import { useAppStore } from './store';

const App: React.FC = () => {
  const { error, successMessage, clearMessages, loading } = useAppStore();

  // Success 3 saniye sonra kapanır, error kullanıcı kapatana kadar durur
  useEffect(() => {
    if (successMessage) {
      const t = setTimeout(clearMessages, 3000);
      return () => clearTimeout(t);
    }
  }, [successMessage]);

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: 24, fontFamily: 'monospace' }}>

      <style>{`
        @keyframes progress {
          0%   { width: 0%;   left: 0; }
          50%  { width: 60%;  left: 20%; }
          100% { width: 0%;   left: 100%; }
        }
      `}</style>

      {/* Loading bar */}
      {loading && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '3px',
          background: 'black',
          animation: 'progress 0.8s ease-in-out infinite',
          zIndex: 9999,
        }} />
      )}

      {/* Header */}
      <div style={{ borderBottom: '3px solid black', marginBottom: 20, paddingBottom: 12 }}>
        <h1 style={{ margin: 0, fontSize: 22, letterSpacing: 2 }}>
          MAPLEWOOD HIGH SCHOOL — COURSE PLANNING
        </h1>
      </div>

      {/* Error — kullanıcı kapatana kadar durur */}
      {error && (
        <div style={{ ...notifStyle('#fee', 'red'), display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>⚠ {error}</span>
          <button onClick={clearMessages} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 16, color: 'red', fontWeight: 'bold' }}>✕</button>
        </div>
      )}

      {/* Success — 3 saniye sonra kapanır */}
      {successMessage && (
        <div style={notifStyle('#efe', 'green')}>✓ {successMessage}</div>
      )}

      <StudentSelector />

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginTop: 16 }}>
        <StudentDashboard />
        <Schedule />
      </div>

      <CourseBrowser />
    </div>
  );
};

const notifStyle = (bg: string, color: string): React.CSSProperties => ({
  padding: '10px 16px',
  marginBottom: 12,
  background: bg,
  border: `1px solid ${color}`,
  color,
  fontWeight: 'bold',
  fontSize: 13,
});

export default App;