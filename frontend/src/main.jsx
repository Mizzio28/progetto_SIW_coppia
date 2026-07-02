import { createRoot } from 'react-dom/client';
import RecensioniWidget from './RecensioniWidget.jsx';

const el = document.getElementById('recensioni-root');
if (el) {
  const corsoId = el.dataset.corsoId;
  const username = el.dataset.username || '';
  const isAdmin = el.dataset.admin === 'true';
  createRoot(el).render(
    <RecensioniWidget corsoId={corsoId} username={username} isAdmin={isAdmin} />
  );
}