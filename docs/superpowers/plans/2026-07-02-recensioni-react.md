# Recensioni sui corsi (React) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a "Recensioni" (reviews) feature to the course detail page, backed by a new REST API, with the UI implemented entirely in React — satisfying the project requirement that at least one part of the frontend use React while the rest stays Thymeleaf.

**Architecture:** New JPA entity `Recensione` (many-to-one to `Utente` and `Corso`, unique per user+course) behind a standard repository → service → REST controller stack, mirroring the existing MVC layering. A small Vite-built React widget (IIFE bundle, no dev server needed at runtime) mounts into a `<div>` inside the existing `corsi/show.html` Thymeleaf template and talks to the new REST API using the session cookie + CSRF meta tags already available on the page.

**Tech Stack:** Spring Boot 3.2.5, Spring Data JPA, Spring Security (session + CSRF), PostgreSQL, Thymeleaf (existing pages, unchanged except one template), React 18 + Vite 5 (new `frontend/` module), `frontend-maven-plugin` for Maven integration.

## Global Constraints

- Base package for all new Java classes: `it.uniroma3.java.siw` (matches existing code — do NOT use `it.uniroma3.siw`, that was a bug fixed earlier in this project).
- Java 17, Spring Boot 3.2.5 (already pinned in `pom.xml`).
- One review per `(utente, corso)` pair — enforced both at DB level (unique constraint) and service level (explicit check with a clear error message).
- Only a user **iscritto** to the course (checked via `IscrizioneRepository`) may create a review for it.
- A user may edit/delete only their own review; ADMIN may delete any review.
- REST API base path: `/api/corsi/{corsoId}/recensioni` — CSRF stays **enabled** for it (session-based, same as the rest of the app — no JWT, no CSRF-disable).
- React build produces a single self-contained JS file (no separate CSS bundle — new styles are appended to the existing global `src/main/resources/static/css/style.css`, reusing class names already defined there from a previous copy: `.recensioni-section`, `.recensioni-header`, `.voto-medio`, `.recensioni-list`, `.recensione-card`, `.recensione-header`, `.recensione-autore`, `.recensione-voto`, `.recensione-data`, `.recensione-testo`).
- `mvn spring-boot:run` / `mvn clean package` must regenerate the React bundle automatically (via Maven's `generate-resources` phase) — no manual `npm run build` step required for either teammate.
- Entity/service unit tests use Mockito (already on the classpath via `spring-boot-starter-test`); no new test-only database dependency (e.g. H2) is introduced — DB-level behavior (unique constraint, average query) is verified manually against the real PostgreSQL dev database, consistent with how this project has been verified so far.

---

### Task 1: `Recensione` entity + `RecensioneRepository`

**Files:**
- Create: `src/main/java/it/uniroma3/java/siw/model/Recensione.java`
- Create: `src/main/java/it/uniroma3/java/siw/repository/RecensioneRepository.java`

**Interfaces:**
- Produces: `Recensione` (getters/setters: `getId/setId`, `getVoto/setVoto`, `getTesto/setTesto`, `getDataCreazione/setDataCreazione`, `getUtente/setUtente`, `getCorso/setCorso`); `RecensioneRepository` methods: `findByCorsoOrderByDataCreazioneDesc(Corso)`, `findByUtenteAndCorso(Utente, Corso)`, `existsByUtenteAndCorso(Utente, Corso)`, `mediaVotoByCorsoId(Long)`.

No dedicated unit test for this task: `Recensione` is a plain JPA-mapped data class (no business logic — its validation annotations are exercised indirectly by the controller test in Task 3), and `RecensioneRepository` is a Spring Data interface with derived/`@Query` methods that only make sense to verify against a real database, which happens manually against PostgreSQL in Task 8. This mirrors how every other entity/repository pair in this project (`Corso`, `Iscrizione`, etc.) has no dedicated test.

- [ ] **Step 1: Create the entity**

```java
package it.uniroma3.java.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "recensioni", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"utente_id", "corso_id"})
})
public class Recensione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Il voto è obbligatorio")
    @Min(value = 1, message = "Il voto minimo è 1")
    @Max(value = 5, message = "Il voto massimo è 5")
    @Column(nullable = false)
    private Integer voto;

    @Size(max = 500, message = "Il testo non può superare i 500 caratteri")
    @Column(length = 500)
    private String testo;

    @Column(nullable = false)
    private LocalDateTime dataCreazione;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corso_id", nullable = false)
    private Corso corso;

    public Recensione() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVoto() { return voto; }
    public void setVoto(Integer voto) { this.voto = voto; }

    public String getTesto() { return testo; }
    public void setTesto(String testo) { this.testo = testo; }

    public LocalDateTime getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(LocalDateTime dataCreazione) { this.dataCreazione = dataCreazione; }

    public Utente getUtente() { return utente; }
    public void setUtente(Utente utente) { this.utente = utente; }

    public Corso getCorso() { return corso; }
    public void setCorso(Corso corso) { this.corso = corso; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recensione)) return false;
        Recensione that = (Recensione) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Recensione{id=" + id + ", voto=" + voto + "}";
    }
}
```

- [ ] **Step 2: Create the repository**

```java
package it.uniroma3.java.siw.repository;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Recensione;
import it.uniroma3.java.siw.model.Utente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecensioneRepository extends CrudRepository<Recensione, Long> {

    List<Recensione> findByCorsoOrderByDataCreazioneDesc(Corso corso);

    Optional<Recensione> findByUtenteAndCorso(Utente utente, Corso corso);

    boolean existsByUtenteAndCorso(Utente utente, Corso corso);

    @Query("SELECT AVG(r.voto) FROM Recensione r WHERE r.corso.id = :corsoId")
    Double mediaVotoByCorsoId(@Param("corsoId") Long corsoId);
}
```

- [ ] **Step 3: Compile**

Run: `mvn -q compile`
Expected: exits with no output (success). If you see errors, check the package declarations match the file paths exactly.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/it/uniroma3/java/siw/model/Recensione.java src/main/java/it/uniroma3/java/siw/repository/RecensioneRepository.java
git commit -m "Aggiunge entity Recensione e RecensioneRepository"
```

---

### Task 2: `RecensioneService` (TDD)

**Files:**
- Create: `src/main/java/it/uniroma3/java/siw/service/RecensioneService.java`
- Test: `src/test/java/it/uniroma3/java/siw/service/RecensioneServiceTest.java`

**Interfaces:**
- Consumes: `Recensione`, `RecensioneRepository` (Task 1); `IscrizioneRepository.existsByUtenteAndCorso(Utente, Corso)` (already exists in the codebase, used identically by `IscrizioneService.esisteIscrizione`); `Utente.getId()`, `Utente.getRuolo()`, `Corso` (existing).
- Produces: `RecensioneService` with methods `findByCorso(Corso): List<Recensione>`, `mediaVoto(Long corsoId): Double`, `crea(Utente, Corso, Integer voto, String testo): Recensione`, `modifica(Long id, Utente utenteCorrente, Integer voto, String testo): Recensione`, `elimina(Long id, Utente utenteCorrente): void`. `crea`/`modifica` throw `IllegalStateException` for business-rule violations (not iscritto, duplicate review); `modifica`/`elimina` throw `IllegalArgumentException` if the review id doesn't exist, and `SecurityException` if the current user isn't allowed to modify/delete it.

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/it/uniroma3/java/siw/service/RecensioneServiceTest.java`:

```java
package it.uniroma3.java.siw.service;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Recensione;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.model.enums.Ruolo;
import it.uniroma3.java.siw.repository.IscrizioneRepository;
import it.uniroma3.java.siw.repository.RecensioneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecensioneServiceTest {

    @Mock private RecensioneRepository recensioneRepository;
    @Mock private IscrizioneRepository iscrizioneRepository;
    @InjectMocks private RecensioneService recensioneService;

    private Utente utente;
    private Utente altroUtente;
    private Utente admin;
    private Corso corso;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setId(1L);
        utente.setRuolo(Ruolo.USER);

        altroUtente = new Utente();
        altroUtente.setId(2L);
        altroUtente.setRuolo(Ruolo.USER);

        admin = new Utente();
        admin.setId(3L);
        admin.setRuolo(Ruolo.ADMIN);

        corso = new Corso();
        corso.setId(10L);
    }

    @Test
    void crea_lanciaEccezione_seUtenteNonIscritto() {
        when(iscrizioneRepository.existsByUtenteAndCorso(utente, corso)).thenReturn(false);

        assertThatThrownBy(() -> recensioneService.crea(utente, corso, 5, "Ottimo corso"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("iscritto");

        verify(recensioneRepository, never()).save(any());
    }

    @Test
    void crea_lanciaEccezione_seRecensioneGiaEsistente() {
        when(iscrizioneRepository.existsByUtenteAndCorso(utente, corso)).thenReturn(true);
        when(recensioneRepository.existsByUtenteAndCorso(utente, corso)).thenReturn(true);

        assertThatThrownBy(() -> recensioneService.crea(utente, corso, 5, "Ottimo corso"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("già recensito");

        verify(recensioneRepository, never()).save(any());
    }

    @Test
    void crea_salvaRecensione_seIscrittoESenzaRecensionePrecedente() {
        when(iscrizioneRepository.existsByUtenteAndCorso(utente, corso)).thenReturn(true);
        when(recensioneRepository.existsByUtenteAndCorso(utente, corso)).thenReturn(false);
        when(recensioneRepository.save(any(Recensione.class))).thenAnswer(inv -> inv.getArgument(0));

        Recensione risultato = recensioneService.crea(utente, corso, 4, "Bello");

        assertThat(risultato.getVoto()).isEqualTo(4);
        assertThat(risultato.getTesto()).isEqualTo("Bello");
        assertThat(risultato.getUtente()).isEqualTo(utente);
        assertThat(risultato.getCorso()).isEqualTo(corso);
        assertThat(risultato.getDataCreazione()).isNotNull();
    }

    @Test
    void modifica_lanciaEccezione_seRecensioneNonTrovata() {
        when(recensioneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recensioneService.modifica(99L, utente, 3, "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void modifica_lanciaEccezione_seNonAutore() {
        Recensione esistente = new Recensione();
        esistente.setId(5L);
        esistente.setUtente(utente);
        when(recensioneRepository.findById(5L)).thenReturn(Optional.of(esistente));

        assertThatThrownBy(() -> recensioneService.modifica(5L, altroUtente, 3, "x"))
                .isInstanceOf(SecurityException.class);

        verify(recensioneRepository, never()).save(any());
    }

    @Test
    void modifica_aggiornaCampi_seAutore() {
        Recensione esistente = new Recensione();
        esistente.setId(5L);
        esistente.setUtente(utente);
        esistente.setVoto(2);
        esistente.setTesto("vecchio");
        when(recensioneRepository.findById(5L)).thenReturn(Optional.of(esistente));
        when(recensioneRepository.save(any(Recensione.class))).thenAnswer(inv -> inv.getArgument(0));

        Recensione risultato = recensioneService.modifica(5L, utente, 5, "nuovo");

        assertThat(risultato.getVoto()).isEqualTo(5);
        assertThat(risultato.getTesto()).isEqualTo("nuovo");
    }

    @Test
    void elimina_lanciaEccezione_seNonAutoreNeAdmin() {
        Recensione esistente = new Recensione();
        esistente.setId(5L);
        esistente.setUtente(utente);
        when(recensioneRepository.findById(5L)).thenReturn(Optional.of(esistente));

        assertThatThrownBy(() -> recensioneService.elimina(5L, altroUtente))
                .isInstanceOf(SecurityException.class);

        verify(recensioneRepository, never()).deleteById(any());
    }

    @Test
    void elimina_permetteEliminazione_seAutore() {
        Recensione esistente = new Recensione();
        esistente.setId(5L);
        esistente.setUtente(utente);
        when(recensioneRepository.findById(5L)).thenReturn(Optional.of(esistente));

        recensioneService.elimina(5L, utente);

        verify(recensioneRepository).deleteById(5L);
    }

    @Test
    void elimina_permetteEliminazione_seAdmin() {
        Recensione esistente = new Recensione();
        esistente.setId(5L);
        esistente.setUtente(utente);
        when(recensioneRepository.findById(5L)).thenReturn(Optional.of(esistente));

        recensioneService.elimina(5L, admin);

        verify(recensioneRepository).deleteById(5L);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn -q test -Dtest=RecensioneServiceTest`
Expected: FAIL to compile — `RecensioneService` does not exist yet.

- [ ] **Step 3: Write the implementation**

```java
package it.uniroma3.java.siw.service;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Recensione;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.model.enums.Ruolo;
import it.uniroma3.java.siw.repository.IscrizioneRepository;
import it.uniroma3.java.siw.repository.RecensioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecensioneService {

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Autowired
    private IscrizioneRepository iscrizioneRepository;

    @Transactional(readOnly = true)
    public List<Recensione> findByCorso(Corso corso) {
        return recensioneRepository.findByCorsoOrderByDataCreazioneDesc(corso);
    }

    @Transactional(readOnly = true)
    public Double mediaVoto(Long corsoId) {
        return recensioneRepository.mediaVotoByCorsoId(corsoId);
    }

    /**
     * Crea una recensione, verificando che l'utente sia iscritto al corso
     * e che non abbia già recensito lo stesso corso in precedenza.
     */
    @Transactional
    public Recensione crea(Utente utente, Corso corso, Integer voto, String testo) {
        if (!iscrizioneRepository.existsByUtenteAndCorso(utente, corso)) {
            throw new IllegalStateException("Devi essere iscritto al corso per recensirlo");
        }
        if (recensioneRepository.existsByUtenteAndCorso(utente, corso)) {
            throw new IllegalStateException("Hai già recensito questo corso");
        }
        Recensione recensione = new Recensione();
        recensione.setUtente(utente);
        recensione.setCorso(corso);
        recensione.setVoto(voto);
        recensione.setTesto(testo);
        recensione.setDataCreazione(LocalDateTime.now());
        return recensioneRepository.save(recensione);
    }

    @Transactional
    public Recensione modifica(Long id, Utente utenteCorrente, Integer voto, String testo) {
        Recensione recensione = recensioneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata con id: " + id));
        if (!recensione.getUtente().getId().equals(utenteCorrente.getId())) {
            throw new SecurityException("Non puoi modificare la recensione di un altro utente");
        }
        recensione.setVoto(voto);
        recensione.setTesto(testo);
        return recensioneRepository.save(recensione);
    }

    @Transactional
    public void elimina(Long id, Utente utenteCorrente) {
        Recensione recensione = recensioneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata con id: " + id));
        boolean isAutore = recensione.getUtente().getId().equals(utenteCorrente.getId());
        boolean isAdmin = utenteCorrente.getRuolo() == Ruolo.ADMIN;
        if (!isAutore && !isAdmin) {
            throw new SecurityException("Non puoi eliminare la recensione di un altro utente");
        }
        recensioneRepository.deleteById(id);
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn -q test -Dtest=RecensioneServiceTest`
Expected: `Tests run: 8, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/it/uniroma3/java/siw/service/RecensioneService.java src/test/java/it/uniroma3/java/siw/service/RecensioneServiceTest.java
git commit -m "Aggiunge RecensioneService con regole di business (iscrizione, unicità, autorizzazione)"
```

---

### Task 3: REST API — DTO, exception handler, `RecensioneRestController` (TDD)

**Files:**
- Create: `src/main/java/it/uniroma3/java/siw/controller/dto/RecensioneDTO.java`
- Create: `src/main/java/it/uniroma3/java/siw/controller/dto/RecensioneRequest.java`
- Create: `src/main/java/it/uniroma3/java/siw/controller/rest/RecensioneRestController.java`
- Create: `src/main/java/it/uniroma3/java/siw/controller/rest/RestExceptionHandler.java`
- Test: `src/test/java/it/uniroma3/java/siw/controller/rest/RecensioneRestControllerTest.java`

**Interfaces:**
- Consumes: `RecensioneService` (Task 2), `CorsoService.findById(Long): Optional<Corso>` (existing), `UtenteService.findByUsername(String): Optional<Utente>` (existing).
- Produces: HTTP API — `GET /api/corsi/{corsoId}/recensioni` → `200` `{"recensioni": [...], "mediaVoto": number}`; `POST /api/corsi/{corsoId}/recensioni` → `201` with the created `RecensioneDTO`; `PUT /api/corsi/{corsoId}/recensioni/{id}` → `200` with the updated `RecensioneDTO`; `DELETE /api/corsi/{corsoId}/recensioni/{id}` → `204`. Error bodies: `{"messaggio": "..."}` for `404`/`409`/`403`, `{"<campo>": "<messaggio>"}` for `400`. `RecensioneDTO` fields: `id, voto, testo, dataCreazione, autoreUsername`.

- [ ] **Step 1: Create the DTOs**

`src/main/java/it/uniroma3/java/siw/controller/dto/RecensioneDTO.java`:

```java
package it.uniroma3.java.siw.controller.dto;

import it.uniroma3.java.siw.model.Recensione;

import java.time.LocalDateTime;

public record RecensioneDTO(
        Long id,
        Integer voto,
        String testo,
        LocalDateTime dataCreazione,
        String autoreUsername
) {
    public static RecensioneDTO from(Recensione r) {
        return new RecensioneDTO(
                r.getId(),
                r.getVoto(),
                r.getTesto(),
                r.getDataCreazione(),
                r.getUtente().getUsername()
        );
    }
}
```

`src/main/java/it/uniroma3/java/siw/controller/dto/RecensioneRequest.java`:

```java
package it.uniroma3.java.siw.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecensioneRequest(
        @NotNull(message = "Il voto è obbligatorio")
        @Min(value = 1, message = "Il voto minimo è 1")
        @Max(value = 5, message = "Il voto massimo è 5")
        Integer voto,

        @Size(max = 500, message = "Il testo non può superare i 500 caratteri")
        String testo
) {}
```

- [ ] **Step 2: Write the failing controller tests**

Create `src/test/java/it/uniroma3/java/siw/controller/rest/RecensioneRestControllerTest.java`:

```java
package it.uniroma3.java.siw.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.uniroma3.java.siw.controller.dto.RecensioneRequest;
import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Recensione;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.service.CorsoService;
import it.uniroma3.java.siw.service.RecensioneService;
import it.uniroma3.java.siw.service.UtenteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecensioneRestController.class)
@AutoConfigureMockMvc(addFilters = false) // sicurezza reale verificata manualmente (Task 8); qui testiamo solo la logica del controller
class RecensioneRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RecensioneService recensioneService;
    @MockBean private CorsoService corsoService;
    @MockBean private UtenteService utenteService;

    private Corso corso;
    private Utente utente;

    private void datiBase() {
        corso = new Corso();
        corso.setId(10L);
        utente = new Utente();
        utente.setId(1L);
        utente.setUsername("mario");
    }

    @Test
    void get_restituisceListaEMedia() throws Exception {
        datiBase();
        Recensione r = new Recensione();
        r.setId(1L); r.setVoto(5); r.setTesto("bello"); r.setDataCreazione(LocalDateTime.now()); r.setUtente(utente); r.setCorso(corso);

        when(corsoService.findById(10L)).thenReturn(Optional.of(corso));
        when(recensioneService.findByCorso(corso)).thenReturn(List.of(r));
        when(recensioneService.mediaVoto(10L)).thenReturn(5.0);

        mockMvc.perform(get("/api/corsi/10/recensioni"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recensioni[0].autoreUsername").value("mario"))
                .andExpect(jsonPath("$.mediaVoto").value(5.0));
    }

    @Test
    void get_corsoInesistente_restituisce404() throws Exception {
        when(corsoService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/corsi/999/recensioni"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messaggio").exists());
    }

    @Test
    @WithMockUser(username = "mario")
    void post_creaRecensione_restituisce201() throws Exception {
        datiBase();
        Recensione creata = new Recensione();
        creata.setId(2L); creata.setVoto(4); creata.setTesto("ok"); creata.setDataCreazione(LocalDateTime.now()); creata.setUtente(utente); creata.setCorso(corso);

        when(corsoService.findById(10L)).thenReturn(Optional.of(corso));
        when(utenteService.findByUsername("mario")).thenReturn(Optional.of(utente));
        when(recensioneService.crea(eq(utente), eq(corso), eq(4), eq("ok"))).thenReturn(creata);

        RecensioneRequest richiesta = new RecensioneRequest(4, "ok");

        mockMvc.perform(post("/api/corsi/10/recensioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(richiesta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voto").value(4));
    }

    @Test
    @WithMockUser(username = "mario")
    void post_votoNonValido_restituisce400() throws Exception {
        RecensioneRequest richiesta = new RecensioneRequest(9, "ok");

        mockMvc.perform(post("/api/corsi/10/recensioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(richiesta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.voto").exists());
    }

    @Test
    @WithMockUser(username = "mario")
    void post_utenteNonIscritto_restituisce409() throws Exception {
        datiBase();
        when(corsoService.findById(10L)).thenReturn(Optional.of(corso));
        when(utenteService.findByUsername("mario")).thenReturn(Optional.of(utente));
        when(recensioneService.crea(any(), any(), anyInt(), any()))
                .thenThrow(new IllegalStateException("Devi essere iscritto al corso per recensirlo"));

        RecensioneRequest richiesta = new RecensioneRequest(4, "ok");

        mockMvc.perform(post("/api/corsi/10/recensioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(richiesta)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.messaggio").value("Devi essere iscritto al corso per recensirlo"));
    }

    @Test
    @WithMockUser(username = "altro")
    void put_nonAutore_restituisce403() throws Exception {
        datiBase();
        when(utenteService.findByUsername("altro")).thenReturn(Optional.of(new Utente()));
        when(recensioneService.modifica(anyLong(), any(), anyInt(), any()))
                .thenThrow(new SecurityException("Non puoi modificare la recensione di un altro utente"));

        RecensioneRequest richiesta = new RecensioneRequest(3, "x");

        mockMvc.perform(put("/api/corsi/10/recensioni/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(richiesta)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "mario")
    void delete_restituisce204() throws Exception {
        when(utenteService.findByUsername("mario")).thenReturn(Optional.of(new Utente()));

        mockMvc.perform(delete("/api/corsi/10/recensioni/1"))
                .andExpect(status().isNoContent());
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `mvn -q test -Dtest=RecensioneRestControllerTest`
Expected: FAIL to compile — `RecensioneRestController` and `RestExceptionHandler` don't exist yet.

- [ ] **Step 4: Write the exception handler**

`src/main/java/it/uniroma3/java/siw/controller/rest/RestExceptionHandler.java`:

```java
package it.uniroma3.java.siw.controller.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestione errori dedicata alle API REST sotto /api/**: risponde sempre con JSON,
 * a differenza di GlobalExceptionHandler che gestisce le view Thymeleaf.
 * Ambito limitato al package controller.rest per non interferire con i controller MVC.
 */
@RestControllerAdvice(basePackages = "it.uniroma3.java.siw.controller.rest")
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("messaggio", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("messaggio", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("messaggio", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errori = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errori.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errori);
    }
}
```

- [ ] **Step 5: Write the REST controller**

`src/main/java/it/uniroma3/java/siw/controller/rest/RecensioneRestController.java`:

```java
package it.uniroma3.java.siw.controller.rest;

import it.uniroma3.java.siw.controller.dto.RecensioneDTO;
import it.uniroma3.java.siw.controller.dto.RecensioneRequest;
import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Recensione;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.service.CorsoService;
import it.uniroma3.java.siw.service.RecensioneService;
import it.uniroma3.java.siw.service.UtenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/corsi/{corsoId}/recensioni")
public class RecensioneRestController {

    @Autowired private RecensioneService recensioneService;
    @Autowired private CorsoService corsoService;
    @Autowired private UtenteService utenteService;

    @GetMapping
    public Map<String, Object> lista(@PathVariable Long corsoId) {
        Corso corso = corsoService.findById(corsoId)
                .orElseThrow(() -> new IllegalArgumentException("Corso non trovato con id: " + corsoId));
        List<RecensioneDTO> recensioni = recensioneService.findByCorso(corso).stream()
                .map(RecensioneDTO::from)
                .collect(Collectors.toList());
        Double media = recensioneService.mediaVoto(corsoId);
        return Map.of("recensioni", recensioni, "mediaVoto", media == null ? 0 : media);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecensioneDTO crea(@PathVariable Long corsoId,
                              @Valid @RequestBody RecensioneRequest request,
                              @AuthenticationPrincipal UserDetails userDetails) {
        Corso corso = corsoService.findById(corsoId)
                .orElseThrow(() -> new IllegalArgumentException("Corso non trovato con id: " + corsoId));
        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));
        Recensione recensione = recensioneService.crea(utente, corso, request.voto(), request.testo());
        return RecensioneDTO.from(recensione);
    }

    @PutMapping("/{id}")
    public RecensioneDTO modifica(@PathVariable Long corsoId,
                                  @PathVariable Long id,
                                  @Valid @RequestBody RecensioneRequest request,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));
        Recensione recensione = recensioneService.modifica(id, utente, request.voto(), request.testo());
        return RecensioneDTO.from(recensione);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long corsoId,
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserDetails userDetails) {
        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));
        recensioneService.elimina(id, utente);
    }
}
```

Note: `id` in the path is the global primary key of the review; this plan does not cross-check that the review actually belongs to `corsoId` in the URL (accepted simplification — not a security issue, since ownership is still enforced by `utenteCorrente`, just a minor REST-purity gap).

- [ ] **Step 6: Run tests to verify they pass**

Run: `mvn -q test -Dtest=RecensioneRestControllerTest`
Expected: `Tests run: 7, Failures: 0, Errors: 0`

- [ ] **Step 7: Run the full test suite to check nothing else broke**

Run: `mvn -q test`
Expected: `BUILD SUCCESS`, all tests (including `RecensioneServiceTest` from Task 2) pass.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/it/uniroma3/java/siw/controller/dto src/main/java/it/uniroma3/java/siw/controller/rest src/test/java/it/uniroma3/java/siw/controller/rest
git commit -m "Aggiunge API REST /api/corsi/{id}/recensioni con gestione errori JSON dedicata"
```

---

### Task 4: `SecurityConfig` — regole per `/api/corsi/**`

**Files:**
- Modify: `src/main/java/it/uniroma3/java/siw/config/SecurityConfig.java`

**Interfaces:**
- Consumes: existing `SecurityFilterChain` bean structure (unchanged shape, only new `.requestMatchers(...)` lines added).
- Produces: `GET /api/corsi/*/recensioni` public; every other `/api/corsi/**` request (POST/PUT/DELETE) requires `USER` or `ADMIN`.

- [ ] **Step 1: Add the new rules**

In `src/main/java/it/uniroma3/java/siw/config/SecurityConfig.java`, find this block (around line 53-54):

```java
                // Abbonamenti — pubblici (sola lettura)
                .requestMatchers("/abbonamenti", "/abbonamenti/**").permitAll()
                // Pagine di errore — sempre accessibili
                .requestMatchers("/error/**").permitAll()
```

Replace it with:

```java
                // Abbonamenti — pubblici (sola lettura)
                .requestMatchers("/abbonamenti", "/abbonamenti/**").permitAll()
                // API Recensioni — lettura pubblica, scrittura autenticata (deve stare
                // prima di una eventuale regola generica su /api/** se mai aggiunta)
                .requestMatchers(new AntPathRequestMatcher("/api/corsi/*/recensioni", "GET")).permitAll()
                .requestMatchers("/api/corsi/**").hasAnyRole("USER", "ADMIN")
                // Pagine di errore — sempre accessibili
                .requestMatchers("/error/**").permitAll()
```

- [ ] **Step 2: Compile**

Run: `mvn -q compile`
Expected: no output (success). `AntPathRequestMatcher` is already imported in this file (used for `/corsi/*/iscriviti`).

- [ ] **Step 3: Commit**

```bash
git add src/main/java/it/uniroma3/java/siw/config/SecurityConfig.java
git commit -m "Aggiunge regole di sicurezza per /api/corsi/**"
```

(Manual end-to-end verification of these rules — anonymous GET works, anonymous POST is rejected, authenticated POST works — happens in Task 8 together with the rest of the feature, once the UI exists to exercise it through.)

---

### Task 5: Thymeleaf — mount point in `corsi/show.html` + CSS additions

**Files:**
- Modify: `src/main/resources/templates/corsi/show.html`
- Modify: `src/main/resources/static/css/style.css`

**Interfaces:**
- Produces: a `<div id="recensioni-root" data-corso-id="..." data-username="..." data-admin="...">` element that Task 6's React bundle looks for by id; CSRF `<meta>` tags read by Task 6's `frontend/src/api.js`.

- [ ] **Step 1: Add CSRF meta tags and the mount point to `corsi/show.html`**

Open `src/main/resources/templates/corsi/show.html`. In the `<head>` (currently just `th:replace="~{fragments/header :: head(${corso.nome})}"`), add CSRF meta tags right after the opening `<body>` tag is not correct — meta tags belong in `<head>`, but this page's `<head>` is entirely replaced by the `fragments/header :: head` fragment. Instead, add the meta tags directly after `<nav th:replace="~{fragments/header :: navbar}"></nav>` (still valid HTML‑wise for Thymeleaf's purposes, and simplest — avoids touching the shared fragment used by every page):

Find:
```html
<nav th:replace="~{fragments/header :: navbar}"></nav>

<main class="container">
```

Replace with:
```html
<nav th:replace="~{fragments/header :: navbar}"></nav>

<meta name="_csrf" th:content="${_csrf.token}"/>
<meta name="_csrf_header" th:content="${_csrf.headerName}"/>

<main class="container">
```

Then find the closing of the `corso-detail` section (right before `</main>`):
```html
    </section>

</main>
```

Replace with:
```html
    </section>

    <section class="recensioni-section">
        <div id="recensioni-root"
             th:attr="data-corso-id=${corso.id},
                      data-username=${#authorization.expression('isAuthenticated()') ? #authentication.name : ''},
                      data-admin=${#authorization.expression('hasRole(''ADMIN'')')}">
        </div>
        <script src="/react/recensioni/recensioni.js"></script>
    </section>

</main>
```

- [ ] **Step 2: Add the missing CSS classes used by the React widget**

The file already has most classes we need (`.recensioni-section`, `.recensioni-header`, `.voto-medio`, `.recensioni-list`, `.recensione-card`, `.recensione-header`, `.recensione-autore`, `.recensione-voto`, `.recensione-data`, `.recensione-testo` — copied earlier from the reference project but currently unused). Append these three missing ones at the end of `src/main/resources/static/css/style.css`:

```css

/* ===== Recensioni — widget React ===== */
.recensioni-loading { color: #64748b; font-size: 0.9rem; padding: 1rem 0; }

.recensione-form {
    display: flex;
    flex-direction: column;
    gap: 0.6rem;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 1rem;
    margin-bottom: 1.25rem;
    max-width: 480px;
}
.recensione-form select { padding: 0.4rem; border-radius: 6px; border: 1px solid #cbd5e1; width: 80px; }
.recensione-form textarea { padding: 0.5rem; border-radius: 6px; border: 1px solid #cbd5e1; font-family: inherit; resize: vertical; min-height: 70px; }

.recensione-actions { display: flex; gap: 0.75rem; margin-top: 0.5rem; }
```

- [ ] **Step 3: Verify the template still renders**

This can't be fully verified until the React bundle exists (Task 6/7); for now just confirm the app still starts without a Thymeleaf parsing error:

Run: `mvn -q clean compile`
Expected: no output (success — this only checks Java compiles; template syntax errors surface at runtime, which we check in Task 8).

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/corsi/show.html src/main/resources/static/css/style.css
git commit -m "Aggiunge punto di montaggio React e stili mancanti per le recensioni"
```

---

### Task 6: React widget (`frontend/`)

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.js`
- Create: `frontend/src/api.js`
- Create: `frontend/src/RecensioniWidget.jsx`
- Create: `frontend/src/main.jsx`

**Interfaces:**
- Consumes: REST API from Task 3 (`GET/POST/PUT/DELETE /api/corsi/{corsoId}/recensioni`); DOM contract from Task 5 (`#recensioni-root` with `data-corso-id`, `data-username`, `data-admin`); CSRF `<meta name="_csrf">` / `<meta name="_csrf_header">` from Task 5.
- Produces: `frontend/dist/recensioni.js` after `npm run build` (an IIFE bundle that self-mounts on load — no exported API, Task 5's `<script>` tag is the only consumer).

No automated JS tests for this task (no JS test framework was part of the design — consistent with keeping scope to what's specified). Verified manually in Task 8 through the running app in a browser.

- [ ] **Step 1: Create `frontend/package.json`**

```json
{
  "name": "recensioni-widget",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "build": "vite build"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1"
  },
  "devDependencies": {
    "@vitejs/plugin-react": "^4.3.1",
    "vite": "^5.4.0"
  }
}
```

- [ ] **Step 2: Create `frontend/vite.config.js`**

Output goes straight into Spring Boot's static resources folder, so Maven's normal `process-resources` phase (which runs right after `generate-resources`, where this build is triggered — see Task 7) picks the files up automatically.

```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: '../src/main/resources/static/react/recensioni',
    emptyOutDir: true,
    lib: {
      entry: 'src/main.jsx',
      name: 'RecensioniWidget',
      formats: ['iife'],
      fileName: () => 'recensioni.js',
    },
  },
})
```

- [ ] **Step 3: Create `frontend/src/api.js`**

```js
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
```

- [ ] **Step 4: Create `frontend/src/RecensioniWidget.jsx`**

```jsx
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
```

- [ ] **Step 5: Create `frontend/src/main.jsx`**

```jsx
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
```

- [ ] **Step 6: Install dependencies and build locally to verify the widget compiles**

Run:
```bash
cd frontend
npm install
npm run build
cd ..
```
Expected: `npm run build` ends with `✓ built in ...` and creates `src/main/resources/static/react/recensioni/recensioni.js`.

- [ ] **Step 7: Commit**

```bash
git add frontend/package.json frontend/vite.config.js frontend/src
git commit -m "Aggiunge il widget React delle recensioni (Vite, build IIFE)"
```

(The generated `frontend/node_modules/`, `frontend/package-lock.json` is fine to commit for reproducible installs — but `dist/` output and `node_modules/` are excluded from git in Task 7.)

---

### Task 7: Maven ↔ Vite build integration

**Files:**
- Modify: `pom.xml`
- Modify: `.gitignore`

**Interfaces:**
- Produces: `mvn clean package` (and `mvn spring-boot:run`) automatically run `npm install && npm run build` inside `frontend/` during the `generate-resources` phase, before Spring Boot's own resource processing, so `src/main/resources/static/react/recensioni/recensioni.js` always exists when the app starts.

- [ ] **Step 1: Add `frontend-maven-plugin` to `pom.xml`**

Inside the existing `<build><plugins>` block (which currently only has `spring-boot-maven-plugin`), add:

```xml
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
                <version>1.15.0</version>
                <configuration>
                    <workingDirectory>frontend</workingDirectory>
                </configuration>
                <executions>
                    <execution>
                        <id>install-node-and-npm</id>
                        <goals>
                            <goal>install-node-and-npm</goal>
                        </goals>
                        <phase>generate-resources</phase>
                        <configuration>
                            <nodeVersion>v20.15.1</nodeVersion>
                        </configuration>
                    </execution>
                    <execution>
                        <id>npm-install</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <phase>generate-resources</phase>
                        <configuration>
                            <arguments>install</arguments>
                        </configuration>
                    </execution>
                    <execution>
                        <id>npm-build</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <phase>generate-resources</phase>
                        <configuration>
                            <arguments>run build</arguments>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
```

So the full `<build>` block becomes:

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
                <version>1.15.0</version>
                <configuration>
                    <workingDirectory>frontend</workingDirectory>
                </configuration>
                <executions>
                    <execution>
                        <id>install-node-and-npm</id>
                        <goals>
                            <goal>install-node-and-npm</goal>
                        </goals>
                        <phase>generate-resources</phase>
                        <configuration>
                            <nodeVersion>v20.15.1</nodeVersion>
                        </configuration>
                    </execution>
                    <execution>
                        <id>npm-install</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <phase>generate-resources</phase>
                        <configuration>
                            <arguments>install</arguments>
                        </configuration>
                    </execution>
                    <execution>
                        <id>npm-build</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <phase>generate-resources</phase>
                        <configuration>
                            <arguments>run build</arguments>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

- [ ] **Step 2: Update `.gitignore`**

Append to `.gitignore`:

```

### React frontend (build artifacts — regenerated by frontend-maven-plugin) ###
frontend/node/
frontend/node_modules/
frontend/dist/
src/main/resources/static/react/
```

- [ ] **Step 3: Verify the full build regenerates the widget from a clean checkout**

Run:
```bash
rm -rf src/main/resources/static/react frontend/dist
mvn -q clean package -DskipTests
```
Expected: `BUILD SUCCESS`, and `src/main/resources/static/react/recensioni/recensioni.js` exists again afterward:
```bash
ls -la src/main/resources/static/react/recensioni/recensioni.js
```
Expected: file listed (not "No such file or directory").

- [ ] **Step 4: Commit**

```bash
git add pom.xml .gitignore
git rm -r --cached src/main/resources/static/react 2>/dev/null || true
git commit -m "Integra la build di React nel ciclo di vita Maven (frontend-maven-plugin)"
```

---

### Task 8: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full clean build**

```bash
mvn -q clean package -DskipTests
```
Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Reset the dev database and start the app**

```bash
PGPASSWORD=postgres psql -h localhost -U postgres -c "DROP DATABASE IF EXISTS siw_palestra_coppia;"
PGPASSWORD=postgres psql -h localhost -U postgres -c "CREATE DATABASE siw_palestra_coppia;"
mvn spring-boot:run
```
Expected log line: `Started SiwPalestraApplication in ...`. Leave it running for the next steps.

- [ ] **Step 3: Verify the API directly**

In another terminal:
```bash
curl -s http://localhost:8080/api/corsi/1/recensioni
```
Expected: `{"recensioni":[],"mediaVoto":0}` (course id 1 is "Yoga per principianti" from `DataLoader`, no reviews yet).

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/api/corsi/1/recensioni \
  -H "Content-Type: application/json" -d '{"voto":5,"testo":"test anonimo"}'
```
Expected: `401` or `403` (anonymous write must be rejected — confirms Task 4's security rule works).

- [ ] **Step 4: Verify through the browser as a real user**

1. Open `http://localhost:8080`, log in as `mario` / `user123` (seeded by `DataLoader`, has a `Base` abbonamento).
2. Go to `/corsi`, open a course mario is **not** yet iscritto to, iscriviti (existing UC4 flow).
3. Open that course's detail page again — the "Recensioni" section should appear at the bottom with a form (mario is now iscritto).
4. Submit a review (pick a voto, write some testo) → it should appear in the list immediately, and "Recensioni" avg/count should update.
5. Refresh the page (full reload, not SPA nav) → the review is still there (confirms it's persisted, not just client state).
6. Try to submit a second review for the same course → the form should be replaced by the existing review (no duplicate-submission UI is shown once `mieRecensione` is set) — confirms the one-review-per-course rule surfaces correctly in the UI.
7. Click "Modifica" on your review, change the voto/testo, save → updated in place.
8. Log out, log in as `laura` / `user123` (also seeded, different abbonamento) on a course laura is **not** iscritta to → open that course's page, confirm **no** review form is shown (not iscritta) but existing reviews (if any) are still visible.
9. Log in as `admin` / `admin123` → open a course with mario's review on it → confirm the admin sees an "Elimina" button on mario's review (not "Modifica") and can delete it.

- [ ] **Step 5: Cross-check the average against the database directly**

```bash
PGPASSWORD=postgres psql -h localhost -U postgres -d siw_palestra_coppia -c "SELECT corso_id, AVG(voto), COUNT(*) FROM recensioni GROUP BY corso_id;"
```
Expected: matches what the "Recensioni" header showed in the browser for each course.

- [ ] **Step 6: Stop the app and do the final commit**

```bash
# Ctrl+C in the terminal running `mvn spring-boot:run`
git add -A
git status
git commit -m "Verifica end-to-end della funzionalità Recensioni (React + REST)"
```
(If `git status` shows nothing to commit because every task already committed its own changes, skip the final commit — that's fine, it means history is already clean.)

- [ ] **Step 7: Update project docs**

This plan and its design doc are already committed under `docs/superpowers/`. No further doc updates required — the React requirement gap identified earlier in this project is now closed.