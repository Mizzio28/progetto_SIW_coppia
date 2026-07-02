package it.uniroma3.java.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.java.siw.model.Istruttore;
import it.uniroma3.java.siw.repository.IstruttoreRepository;

import java.util.List;
import java.util.Optional;

@Service
public class IstruttoreService {

    @Autowired
    private IstruttoreRepository istruttoreRepository;

    @Transactional(readOnly = true)
    public List<Istruttore> findAll() {
        return istruttoreRepository.findAllWithCorsi();
    }

    @Transactional(readOnly = true)
    public Optional<Istruttore> findById(Long id) {
        return istruttoreRepository.findByIdWithCorsi(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByNomeAndCognome(String nome, String cognome) {
        return istruttoreRepository.existsByNomeAndCognome(nome, cognome);
    }

    @Transactional(readOnly = true)
    public boolean existsByNomeAndCognomeExcluding(String nome, String cognome, Long id) {
        return istruttoreRepository.existsByNomeAndCognomeAndIdNot(nome, cognome, id);
    }

    @Transactional
    public Istruttore save(Istruttore istruttore) {
        return istruttoreRepository.save(istruttore);
    }

    @Transactional
    public Istruttore update(Long id, Istruttore dati) {
        Istruttore istruttore = istruttoreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Istruttore non trovato con id: " + id));
        istruttore.setNome(dati.getNome());
        istruttore.setCognome(dati.getCognome());
        istruttore.setDataDiNascita(dati.getDataDiNascita());
        istruttore.setSpecializzazione(dati.getSpecializzazione());
        istruttore.setAnniDiEsperienza(dati.getAnniDiEsperienza());
        istruttore.setUrlFoto(dati.getUrlFoto());
        return istruttoreRepository.save(istruttore);
    }

    @Transactional
    public void delete(Long id) {
        if (!istruttoreRepository.existsById(id)) {
            throw new IllegalArgumentException("Istruttore non trovato con id: " + id);
        }
        if (istruttoreRepository.countCorsiByIstruttoreId(id) > 0) {
            throw new IllegalStateException("Impossibile eliminare: l'istruttore ha corsi associati");
        }
        istruttoreRepository.deleteById(id);
    }
}
