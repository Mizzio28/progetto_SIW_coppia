import { useEffect, useState } from 'react';
import { fetchRecensioni, creaRecensione, modificaRecensione, eliminaRecensione } from './api';

export default function RecensioniWidget({ corsoId, username, isAdmin }) {
  const [recensioni, setRecensioni] = useState([]);
  const [mediaVoto, setMediaVoto] = useState(0);
  const [loading, setLoading] = useState(true);
  const [errore, setErrore] = useState(null);
  const [voto, setVoto] = useState(5);
  const [testo, setTesto] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [salvando, setSalvando] = useState(false);

  const isAuthenticated = Boolean(username);
  const mieRecensione = recensioni.find(r => r.autoreUsername === username);

  async function carica() {
    setLoading(true);
    setErrore(null);
    try {
      const dati = await fetchRecensioni(corsoId);
      setRecensioni(dati.recensioni);
      setMediaVoto(dati.mediaVoto);
    } catch (e) {
      setErrore(e.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { carica(); }, [corsoId]);

  function resetForm() {
    setEditingId(null);
    setTesto('');
    setVoto(5);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSalvando(true);
    setErrore(null);
    try {
      if (editingId) {
        await modificaRecensione(corsoId, editingId, voto, testo);
      } else {
        await creaRecensione(corsoId, voto, testo);
      }
      resetForm();
      await carica();
    } catch (e) {
      setErrore(e.message);
    } finally {
      setSalvando(false);
    }
  }

  function handleModifica(r) {
    setEditingId(r.id);
    setVoto(r.voto);
    setTesto(r.testo || '');
  }

  async function handleElimina(id) {
    if (!window.confirm('Eliminare questa recensione?')) return;
    setErrore(null);
    try {
      await eliminaRecensione(corsoId, id);
      if (editingId === id) resetForm();
      await carica();
    } catch (e) {
      setErrore(e.message);
    }
  }

  if (loading) return <p className="recensioni-loading">Caricamento recensioni...</p>;

  return (
    <div className="recensioni-widget">
      <div className="recensioni-header">
        <h2>Recensioni</h2>
        <span className="voto-medio">
          {recensioni.length > 0
            ? `★ ${mediaVoto.toFixed(1)} / 5 (${recensioni.length})`
            : 'Nessuna recensione'}
        </span>
      </div>

      {errore && <div className="alert alert-error">{errore}</div>}

      {isAuthenticated && !mieRecensione && !editingId && (
        <form className="recensione-form" onSubmit={handleSubmit}>
          <label>
            Voto:
            <select value={voto} onChange={e => setVoto(Number(e.target.value))}>
              {[1, 2, 3, 4, 5].map(v => <option key={v} value={v}>{v}</option>)}
            </select>
          </label>
          <textarea
            placeholder="Scrivi una recensione (opzionale)"
            value={testo}
            onChange={e => setTesto(e.target.value)}
            maxLength={500}
          />
          <button type="submit" className="btn btn-primary" disabled={salvando}>
            {salvando ? 'Invio...' : 'Pubblica recensione'}
          </button>
        </form>
      )}

      {editingId && (
        <form className="recensione-form" onSubmit={handleSubmit}>
          <label>
            Voto:
            <select value={voto} onChange={e => setVoto(Number(e.target.value))}>
              {[1, 2, 3, 4, 5].map(v => <option key={v} value={v}>{v}</option>)}
            </select>
          </label>
          <textarea value={testo} onChange={e => setTesto(e.target.value)} maxLength={500} />
          <div className="recensione-actions">
            <button type="submit" className="btn btn-primary" disabled={salvando}>Salva modifiche</button>
            <button type="button" className="btn btn-link" onClick={resetForm}>Annulla</button>
          </div>
        </form>
      )}

      {recensioni.length === 0 && <p className="empty-state">Nessuna recensione per questo corso.</p>}

      <ul className="recensioni-list">
        {recensioni.map(r => (
          <li key={r.id} className="recensione-card">
            <div className="recensione-header">
              <span className="recensione-autore">{r.autoreUsername}</span>
              <span className="recensione-voto">{'★'.repeat(r.voto)}{'☆'.repeat(5 - r.voto)}</span>
              <span className="recensione-data">{new Date(r.dataCreazione).toLocaleDateString('it-IT')}</span>
            </div>
            {r.testo && <p className="recensione-testo">{r.testo}</p>}
            {(r.autoreUsername === username || isAdmin) && (
              <div className="recensione-actions">
                {r.autoreUsername === username && (
                  <button className="btn-link" onClick={() => handleModifica(r)}>Modifica</button>
                )}
                <button className="btn-link" onClick={() => handleElimina(r.id)}>Elimina</button>
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}