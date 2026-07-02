const BASE = '/api/corsi';

function csrfHeaders() {
  const token = document.querySelector('meta[name="_csrf"]');
  const header = document.querySelector('meta[name="_csrf_header"]');
  if (!token || !header) return {};
  return { [header.content]: token.content };
}

async function parseErrore(res) {
  const dati = await res.json().catch(() => ({}));
  if (dati.messaggio) return dati.messaggio;
  const primoErrore = Object.values(dati)[0];
  return primoErrore || 'Si è verificato un errore';
}

export async function fetchRecensioni(corsoId) {
  const res = await fetch(`${BASE}/${corsoId}/recensioni`);
  if (!res.ok) throw new Error('Errore nel caricamento delle recensioni');
  return res.json();
}

export async function creaRecensione(corsoId, voto, testo) {
  const res = await fetch(`${BASE}/${corsoId}/recensioni`, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
    body: JSON.stringify({ voto, testo }),
  });
  if (!res.ok) throw new Error(await parseErrore(res));
  return res.json();
}

export async function modificaRecensione(corsoId, id, voto, testo) {
  const res = await fetch(`${BASE}/${corsoId}/recensioni/${id}`, {
    method: 'PUT',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
    body: JSON.stringify({ voto, testo }),
  });
  if (!res.ok) throw new Error(await parseErrore(res));
  return res.json();
}

export async function eliminaRecensione(corsoId, id) {
  const res = await fetch(`${BASE}/${corsoId}/recensioni/${id}`, {
    method: 'DELETE',
    credentials: 'same-origin',
    headers: { ...csrfHeaders() },
  });
  if (!res.ok) throw new Error(await parseErrore(res));
}