package it.uniroma3.java.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.java.siw.model.Armadietto;
import it.uniroma3.java.siw.model.Prenotazione;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.model.enums.Dimensione;
import it.uniroma3.java.siw.model.enums.StatoPrenotazione;
import it.uniroma3.java.siw.repository.ArmadiettoRepository;
import it.uniroma3.java.siw.repository.PrenotazioneRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PrenotazioneService {
    @Autowired
    private PrenotazioneRepository prenotazioneRepository;
    @Autowired
    private ArmadiettoRepository armadiettoRepository;

    @Transactional(readOnly = true)
    public Optional<Prenotazione> findPrenotazioneAttiva(Utente utente) {
        return prenotazioneRepository.findByUtenteAndStatoWithArmadietto(utente, StatoPrenotazione.ATTIVA);
    }

    @Transactional
    public Prenotazione prenota(Utente utente, Dimensione dimensione, Integer mese, Integer anno) {
        if (findPrenotazioneAttiva(utente).isPresent()) {
            throw new IllegalStateException("Hai già una prenotazione attiva. Annullala prima di prenotarne un'altra.");
        }

        List<Armadietto> disponibili = armadiettoRepository.findDisponibili(dimensione, mese, anno);
        if (disponibili.isEmpty()) {
            throw new IllegalStateException("Nessun armadietto " + dimensione + " disponibile per il mese selezionato.");
        }

        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setUtente(utente);
        prenotazione.setArmadietto(disponibili.get(0));
        prenotazione.setMese(mese);
        prenotazione.setAnno(anno);
        prenotazione.setStato(StatoPrenotazione.ATTIVA);
        prenotazione.setDataPrenotazione(LocalDateTime.now());
        return prenotazioneRepository.save(prenotazione);
    }

    @Transactional
    public Prenotazione annulla(Long id, Utente utenteCorrente) {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata con id: " + id));
        if (!prenotazione.getUtente().getId().equals(utenteCorrente.getId())) {
            throw new IllegalStateException("Non puoi annullare la prenotazione di un altro utente");
        }
        prenotazione.setStato(StatoPrenotazione.ANNULLATA);
        return prenotazioneRepository.save(prenotazione);
    }
}
