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