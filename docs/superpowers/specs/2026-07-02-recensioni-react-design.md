# Recensioni sui corsi — parte React (design)

## Contesto e obiettivo

Il progetto è un'app Spring Boot (JPA/Hibernate, PostgreSQL, Thymeleaf, Spring Security)
per la gestione di una palestra. La consegna richiede che **almeno una parte del
frontend sia realizzata in React**; ad oggi il frontend è interamente Thymeleaf.

Si è scelto di aggiungere una **funzionalità nuova e isolata** — le recensioni sui
corsi — con interfaccia interamente in React, invece di convertire una pagina Thymeleaf
già funzionante. Questo:

- non tocca le pagine esistenti che già funzionano (basso rischio di regressioni)
- aggiunge un'ottava entità al modello, con un caso d'uso multi-entità in più
  (`Recensione` lega `Utente` e `Corso`)
- dà un motivo concreto per esporre una vera API REST accanto ai controller MVC
  esistenti

## Modello dati

Nuova entità `Recensione`:

| Campo | Tipo | Note |
|---|---|---|
| `id` | `Long` | PK, `GenerationType.IDENTITY` |
| `voto` | `Integer` | 1–5, `@Min(1) @Max(5) @NotNull` |
| `testo` | `String` | opzionale, `@Size(max = 500)` |
| `dataCreazione` | `LocalDateTime` | valorizzata dal service alla creazione |
| `utente` | `Utente` | `@ManyToOne`, `@JoinColumn(name = "utente_id", nullable = false)` |
| `corso` | `Corso` | `@ManyToOne`, `@JoinColumn(name = "corso_id", nullable = false)` |

Vincolo di unicità `(utente_id, corso_id)` a livello di `@Table` — stesso pattern già
usato per `Iscrizione`. Un utente può avere **una sola recensione per corso**.

## Regole di business (in `RecensioneService`)

- Solo un utente **iscritto al corso** (verificato via `IscrizioneRepository`/
  `IscrizioneService`) può creare una recensione per quel corso.
- Un utente può modificare o cancellare **solo la propria** recensione.
- L'ADMIN può cancellare qualunque recensione (moderazione).
- Tentativo di seconda recensione sullo stesso corso → errore (`IllegalStateException`,
  stesso stile già usato in `IscrizioneService`/`PrenotazioneService`).

## Persistence layer

`RecensioneRepository extends CrudRepository<Recensione, Long>`:
- `List<Recensione> findByCorsoOrderByDataCreazioneDesc(Corso corso)`
- `Optional<Recensione> findByUtenteAndCorso(Utente utente, Corso corso)`
- `boolean existsByUtenteAndCorso(Utente utente, Corso corso)`
- `@Query` per media voti di un corso (`SELECT AVG(r.voto) FROM Recensione r WHERE r.corso.id = :corsoId`)

## API REST

Nuovo `RecensioneRestController` (`@RestController`, non `@Controller` MVC come gli
altri) su `/api/corsi/{corsoId}/recensioni`. Risponde con DTO (record Java), mai con
le entity direttamente, per evitare problemi di serializzazione con le relazioni JPA
lazy (`Utente`/`Corso` collegati).

| Metodo | Path | Auth | Esito |
|---|---|---|---|
| `GET` | `/api/corsi/{corsoId}/recensioni` | pubblico | lista recensioni + media voti |
| `POST` | `/api/corsi/{corsoId}/recensioni` | USER/ADMIN, deve essere iscritto | crea, 201 |
| `PUT` | `/api/corsi/{corsoId}/recensioni/{id}` | autore | modifica, 200 |
| `DELETE` | `/api/corsi/{corsoId}/recensioni/{id}` | autore o ADMIN | cancella, 204 |

### Gestione errori

Il controller mappa le eccezioni di servizio su status HTTP coerenti:

- `IllegalArgumentException` ("corso/recensione non trovata") → `404`
- `IllegalStateException` ("non iscritto" / "recensione già esistente") → `409`
- Violazione autorizzazione (utente diverso dall'autore, non ADMIN) → `403`
- Errori di validazione bean (`voto` fuori range, `testo` troppo lungo) → `400` con
  messaggi di campo, gestiti da un `@ExceptionHandler` dedicato nel controller REST
  (il `GlobalExceptionHandler` esistente è pensato per le view Thymeleaf, non per
  risposte JSON — la REST API ha bisogno di un proprio handler che restituisca JSON).

Il componente React mostra questi errori inline nel form (es. "Devi essere iscritto al
corso per recensirlo") invece di una pagina di errore generica.

## Sicurezza

In `SecurityConfig`, nuove regole (stesso schema già usato per `/corsi/*/iscriviti`),
inserite prima delle regole generiche:

```java
.requestMatchers(new AntPathRequestMatcher("/api/corsi/*/recensioni", "GET")).permitAll()
.requestMatchers(new AntPathRequestMatcher("/api/corsi/*/recensioni/**", "POST")).hasAnyRole("USER", "ADMIN")
.requestMatchers(new AntPathRequestMatcher("/api/corsi/*/recensioni/**", "PUT")).hasAnyRole("USER", "ADMIN")
.requestMatchers(new AntPathRequestMatcher("/api/corsi/*/recensioni/**", "DELETE")).hasAnyRole("USER", "ADMIN")
```

**CSRF**: l'app usa sessioni + CSRF token (non JWT), quindi il widget React riusa la
sessione Spring Security già attiva nel browser. `corsi/show.html` espone il token in
un tag `<meta>`:

```html
<meta name="_csrf" th:content="${_csrf.token}"/>
<meta name="_csrf_header" th:content="${_csrf.headerName}"/>
```

React legge questi meta tag e imposta l'header corrispondente su ogni `POST`/`PUT`/
`DELETE`. Le chiamate `fetch` usano `credentials: 'same-origin'` per inviare il cookie
di sessione.

## Frontend React

- Nuova cartella `frontend/` alla radice del progetto (accanto a `src/`): progetto
  Vite + React standalone, **JavaScript semplice, niente TypeScript**.
- Un solo componente `RecensioniWidget`:
  - al mount: `GET /api/corsi/{id}/recensioni` → mostra media voti (stelle) ed elenco
    recensioni (autore, voto, testo, data)
  - se l'utente è loggato ed è iscritto al corso: form per creare/modificare la
    propria recensione (voto 1–5 + testo)
  - se la recensione visualizzata è la propria (o l'utente è ADMIN): pulsante elimina
  - stati gestiti: caricamento, lista vuota, errore di rete, errore di validazione dal
    backend (mostrato accanto al campo relativo)
- Punto di montaggio in `corsi/show.html` (Thymeleaf, resto della pagina invariato):

```html
<div id="recensioni-root" data-corso-id="{{corso.id}}"></div>
<link rel="stylesheet" href="/react/recensioni/recensioni.css"/>
<script type="module" src="/react/recensioni/recensioni.js"></script>
```

Il resto della pagina corso (dettaglio, iscrizione, azioni admin) resta Thymeleaf
com'è oggi — React gestisce solo il blocco recensioni.

## Build tooling — Vite + Maven

- `frontend/` è un progetto Vite indipendente (`package.json`, `vite.config.js`,
  sorgenti in `frontend/src/`).
- Vite configurato con nomi di output **fissi** (non hash-ati: `recensioni.js`,
  `recensioni.css`), altrimenti Thymeleaf non saprebbe che nome referenziare.
- In `pom.xml`, `frontend-maven-plugin` agganciato alla fase `generate-resources`:
  scarica una copia locale di Node (riproducibile anche su altre macchine, non serve
  installarlo a mano), esegue `npm install` + `npm run build` dentro `frontend/`, poi
  copia l'output in `src/main/resources/static/react/recensioni/`.
- Risultato: `mvn spring-boot:run` / `mvn package` ricostruiscono automaticamente
  anche la parte React — un solo comando avvia tutto.
- `.gitignore`: `frontend/node_modules/`, `frontend/dist/` e l'output generato in
  `src/main/resources/static/react/` — artefatti di build rigenerabili, non
  committati.

## Testing / verifica

1. `mvn clean package` → verifica che il bundle React venga generato in
   `static/react/recensioni/`.
2. Avvio app, utente di test iscritto a un corso: crea, modifica, elimina una
   recensione dalla UI React → verifica persistenza in PostgreSQL.
3. Utente NON iscritto al corso → tentativo di recensione bloccato con messaggio
   d'errore visibile nel widget (non un crash).
4. Utente prova a modificare/cancellare la recensione di un altro utente → bloccato
   (403).
5. ADMIN cancella la recensione di un altro utente → permesso.
6. Media voti mostrata nel widget corrisponde alla media calcolata via query SQL
   diretta.

## Fuori scope (non in questa iterazione)

- Paginazione delle recensioni (assumiamo pochi corsi/recensioni per corso, dato il
  contesto didattico del progetto)
- Moderazione avanzata (segnalazioni, approvazione recensioni)
- Notifiche all'istruttore/admin per nuove recensioni