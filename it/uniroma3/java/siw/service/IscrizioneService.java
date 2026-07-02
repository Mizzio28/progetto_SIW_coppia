package it.uniroma3.java.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Iscrizione;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.model.enums.StatoIscrizione;
import it.uniroma3.java.siw.repository.IscrizioneRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IscrizioneService {

    @Autowired
    private IscrizioneRepository iscrizioneRepository;

    @Transactional(readOnly = true)
    public List<Iscrizione> findAll() {
        return (List<Iscrizione>) iscrizioneRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Iscrizione> findById(Long id) {
        return iscrizioneRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Iscrizione> findByUtente(Utente utente) {
        return iscrizioneRepository.findByUtente(utente);
    }

    @Transactional(readOnly = true)
    public List<Iscrizione> findByUtenteWithCorso(Utente utente) {
        return iscrizioneRepository.findByUtenteWithCorso(utente);
    }

    @Transactional(readOnly = true)
    public boolean esisteIscrizione(Utente utente, Corso corso) {
        return iscrizioneRepository.existsByUtenteAndCorso(utente, corso);
    }

    /**
     * Iscrive un utente a un corso, verificando duplicati e abbonamento attivo.
     */
    @Transactional
    public Iscrizione iscriviUtente(Utente utente, Corso corso) {
        if (iscrizioneRepository.existsByUtenteAndCorso(utente, corso)) {
            throw new IllegalStateException("Sei già iscritto a questo corso");
        }
        if (utente.getAbbonamento() == null) {
            throw new IllegalStateException("È necessario un abbonamento attivo per iscriversi ai corsi");
        }
        Iscrizione iscrizione = new Iscrizione();
        iscrizione.setUtente(utente);
        iscrizione.setCorso(corso);
        iscrizione.setDataIscrizione(LocalDateTime.now());
        iscrizione.setStato(StatoIscrizione.ATTIVA);
        return iscrizioneRepository.save(iscrizione);
    }

    @Transactional
    public Iscrizione save(Iscrizione iscrizione) {
        return iscrizioneRepository.save(iscrizione);
    }

    @Transactional
    public void delete(Long id) {
        if (!iscrizioneRepository.existsById(id)) {
            throw new IllegalArgumentException("Iscrizione non trovata con id: " + id);
        }
        iscrizioneRepository.deleteById(id);
    }

    @Transactional
    public Iscrizione annulla(Long id, Utente utenteCorrente) {
        Iscrizione iscrizione = iscrizioneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Iscrizione non trovata con id: " + id));
        if (!iscrizione.getUtente().getId().equals(utenteCorrente.getId())) {
            throw new IllegalStateException("Non puoi annullare l'iscrizione di un altro utente");
        }
        iscrizione.setStato(StatoIscrizione.ANNULLATA);
        return iscrizioneRepository.save(iscrizione);
    }
}
