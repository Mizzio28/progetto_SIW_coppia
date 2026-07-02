package it.uniroma3.java.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.java.siw.model.Abbonamento;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.model.enums.Ruolo;
import it.uniroma3.java.siw.repository.AbbonamentoRepository;
import it.uniroma3.java.siw.repository.UtenteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UtenteService {

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private AbbonamentoRepository abbonamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Utente> findAll() {
        return (List<Utente>) utenteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Utente> findById(Long id) {
        return utenteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Utente> findByUsername(String username) {
        return utenteRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Utente> findByUsernameWithAbbonamento(String username) {
        return utenteRepository.findByUsernameWithAbbonamento(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return utenteRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return utenteRepository.existsByEmail(email);
    }

    /**
     * Registra un nuovo utente verificando duplicati su username ed email,
     * cifrando la password con BCrypt e impostando ruolo USER di default.
     */
    @Transactional
    public Utente registra(Utente utente) {
        if (utenteRepository.existsByUsername(utente.getUsername())) {
            throw new IllegalStateException("Username già in uso: " + utente.getUsername());
        }
        if (utente.getEmail() != null && utenteRepository.existsByEmail(utente.getEmail())) {
            throw new IllegalStateException("Email già in uso: " + utente.getEmail());
        }
        utente.setPassword(passwordEncoder.encode(utente.getPassword()));
        utente.setRuolo(Ruolo.USER);
        utente.setDataRegistrazione(LocalDateTime.now());
        return utenteRepository.save(utente);
    }

    @Transactional
    public Utente save(Utente utente) {
        return utenteRepository.save(utente);
    }

    @Transactional
    public Utente update(Long id, Utente dati) {
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato con id: " + id));
        utente.setNome(dati.getNome());
        utente.setCognome(dati.getCognome());
        utente.setEmail(dati.getEmail());
        return utenteRepository.save(utente);
    }

    @Transactional
    public Utente sottoscriviAbbonamento(Long utenteId, Long abbonamentoId) {
        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato con id: " + utenteId));
        Abbonamento abbonamento = abbonamentoRepository.findById(abbonamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Abbonamento non trovato con id: " + abbonamentoId));
        utente.setAbbonamento(abbonamento);
        return utenteRepository.save(utente);
    }

    @Transactional
    public void delete(Long id) {
        if (!utenteRepository.existsById(id)) {
            throw new IllegalArgumentException("Utente non trovato con id: " + id);
        }
        utenteRepository.deleteById(id);
    }
}
