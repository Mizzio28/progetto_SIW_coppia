package it.uniroma3.java.siw.config;

import it.uniroma3.java.siw.model.*;
import it.uniroma3.java.siw.model.enums.Livello;
import it.uniroma3.java.siw.model.enums.Ruolo;
import it.uniroma3.java.siw.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private AbbonamentoRepository abbonamentoRepository;
    @Autowired private IstruttoreRepository  istruttoreRepository;
    @Autowired private CorsoRepository       corsoRepository;
    @Autowired private UtenteRepository      utenteRepository;
    @Autowired private PasswordEncoder       passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (utenteRepository.count() > 0) {
            return;
        }

        // ── Abbonamenti ──────────────────────────────────────────────────────
        Abbonamento base = abbonamento("Base",
                "Accesso alle attrezzature e alle aree comuni della palestra.");
        Abbonamento silver = abbonamento("Silver",
                "Tutto ciò che include il Base, più accesso ai corsi collettivi.");
        Abbonamento gold = abbonamento("Gold",
                "Accesso illimitato a tutte le strutture, corsi e sessioni con personal trainer.");

        abbonamentoRepository.save(base);
        abbonamentoRepository.save(silver);
        abbonamentoRepository.save(gold);

        // ── Istruttori ────────────────────────────────────────────────────────
        Istruttore marco = istruttore("Marco", "Rossi",
                LocalDate.of(1985, 3, 12), "Pilates e Yoga", 10,
                "https://images.unsplash.com/photo-1704223523303-a5ed14561b1f?w=400&q=80&auto=format&fit=crop");
        Istruttore sara = istruttore("Sara", "Bianchi",
                LocalDate.of(1990, 7, 25), "CrossFit e HIIT", 7,
                "https://images.unsplash.com/photo-1539794830467-1f1755804d13?w=400&q=80&auto=format&fit=crop");
        Istruttore luca = istruttore("Luca", "Verdi",
                LocalDate.of(1982, 11, 5), "Body Building e Forza", 15,
                "https://images.unsplash.com/photo-1519505907962-0a6cb0167c73?w=400&q=80&auto=format&fit=crop");
        Istruttore giulia = istruttore("Giulia", "Ferrari",
                LocalDate.of(1993, 4, 18), "Danza e Aerobica", 5,
                "https://images.unsplash.com/photo-1579758629938-03607ccdbaba?w=400&q=80&auto=format&fit=crop");
        Istruttore davide = istruttore("Davide", "Russo",
                LocalDate.of(1988, 9, 30), "Arti Marziali e Difesa Personale", 12,
                "https://images.unsplash.com/photo-1704223523169-52feeed90365?w=400&q=80&auto=format&fit=crop");

        istruttoreRepository.save(marco);
        istruttoreRepository.save(sara);
        istruttoreRepository.save(luca);
        istruttoreRepository.save(giulia);
        istruttoreRepository.save(davide);

        // ── Corsi ─────────────────────────────────────────────────────────────
        corsoRepository.save(corso("Yoga per principianti",
                "Introduzione alle posizioni base dello yoga. Ideale per chi non ha mai praticato.",
                Livello.PRINCIPIANTE, 60, marco,
                "https://images.unsplash.com/photo-1761035190790-aa1a3472f7fc?w=800&q=80&auto=format&fit=crop"));

        corsoRepository.save(corso("Pilates avanzato",
                "Sessione intensa di pilates con focus sul core e sulla stabilizzazione.",
                Livello.AVANZATO, 75, marco,
                "https://images.unsplash.com/photo-1747240549807-fc3962949818?w=800&q=80&auto=format&fit=crop"));

        corsoRepository.save(corso("CrossFit base",
                "Allenamento funzionale ad alta intensità per migliorare forza e resistenza.",
                Livello.PRINCIPIANTE, 50, sara,
                "https://images.unsplash.com/photo-1674834727206-4bc272bfd8da?w=800&q=80&auto=format&fit=crop"));

        corsoRepository.save(corso("HIIT Total Body",
                "Circuito ad intervalli ad alta intensità che coinvolge tutto il corpo.",
                Livello.INTERMEDIO, 45, sara,
                "https://images.unsplash.com/photo-1599058917212-d750089bc07e?w=800&q=80&auto=format&fit=crop"));

        corsoRepository.save(corso("Powerlifting",
                "Tecnica e programmazione per squat, panca e stacco. Solo per atleti esperti.",
                Livello.AVANZATO, 90, luca,
                "https://images.unsplash.com/photo-1653276526709-c424ad920de6?w=800&q=80&auto=format&fit=crop"));

        corsoRepository.save(corso("Muscolazione per principianti",
                "Introduzione al mondo del body building: esercizi base e corretta alimentazione.",
                Livello.PRINCIPIANTE, 60, luca,
                "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=800&q=80&auto=format&fit=crop"));

        corsoRepository.save(corso("Zumba",
                "Lezione di fitness danzante su ritmi latini. Divertimento garantito!",
                Livello.PRINCIPIANTE, 55, giulia,
                "https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?w=800&q=80&auto=format&fit=crop"));

        corsoRepository.save(corso("Difesa personale",
                "Tecniche di base di autodifesa tratte da varie discipline marziali.",
                Livello.INTERMEDIO, 80, davide,
                "https://images.unsplash.com/photo-1555597673-b21d5c935865?w=800&q=80&auto=format&fit=crop"));

        // ── Utenti ────────────────────────────────────────────────────────────
        utenteRepository.save(utente("admin", "admin123", "Admin", "Sistema",
                "admin@siwpalestra.it", Ruolo.ADMIN, gold));

        utenteRepository.save(utente("mario", "user123", "Mario", "Conti",
                "mario.conti@example.com", Ruolo.USER, base));

        utenteRepository.save(utente("laura", "user123", "Laura", "Galli",
                "laura.galli@example.com", Ruolo.USER, silver));

        System.out.println(">>> DataLoader: database inizializzato con i dati di esempio.");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Abbonamento abbonamento(String nome, String descrizione) {
        Abbonamento a = new Abbonamento();
        a.setNome(nome);
        a.setDescrizione(descrizione);
        return a;
    }

    private Istruttore istruttore(String nome, String cognome, LocalDate dataNascita,
                                   String specializzazione, int anni, String urlFoto) {
        Istruttore i = new Istruttore();
        i.setNome(nome);
        i.setCognome(cognome);
        i.setDataDiNascita(dataNascita);
        i.setSpecializzazione(specializzazione);
        i.setAnniDiEsperienza(anni);
        i.setUrlFoto(urlFoto);
        return i;
    }

    private Corso corso(String nome, String descrizione, Livello livello,
                        int durataMinuti, Istruttore istruttore, String urlImmagine) {
        Corso c = new Corso();
        c.setNome(nome);
        c.setDescrizione(descrizione);
        c.setLivello(livello);
        c.setDurataMinuti(durataMinuti);
        c.setIstruttore(istruttore);
        c.setUrlImmagine(urlImmagine);
        return c;
    }

    private Utente utente(String username, String passwordClear, String nome, String cognome,
                          String email, Ruolo ruolo, Abbonamento abbonamento) {
        Utente u = new Utente();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(passwordClear));
        u.setNome(nome);
        u.setCognome(cognome);
        u.setEmail(email);
        u.setRuolo(ruolo);
        u.setDataRegistrazione(LocalDateTime.now());
        u.setAbbonamento(abbonamento);
        return u;
    }
}

