package it.uniroma3.java.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.enums.Livello;
import it.uniroma3.java.siw.repository.CorsoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CorsoService {

    @Autowired
    private CorsoRepository corsoRepository;

    @Transactional(readOnly = true)
    public List<Corso> findAll() {
        return corsoRepository.findAllWithIstruttore();
    }

    @Transactional(readOnly = true)
    public Optional<Corso> findById(Long id) {
        return corsoRepository.findByIdWithIstruttore(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByNome(String nome) {
        return corsoRepository.existsByNome(nome);
    }

    @Transactional(readOnly = true)
    public boolean existsByNomeExcluding(String nome, Long id) {
        return corsoRepository.existsByNomeAndIdNot(nome, id);
    }

    @Transactional
    public Corso save(Corso corso) {
        return corsoRepository.save(corso);
    }

    @Transactional
    public Corso update(Long id, Corso dati) {
        Corso corso = corsoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Corso non trovato con id: " + id));
        corso.setNome(dati.getNome());
        corso.setDescrizione(dati.getDescrizione());
        corso.setLivello(dati.getLivello());
        corso.setDurataMinuti(dati.getDurataMinuti());
        corso.setUrlImmagine(dati.getUrlImmagine());
        corso.setIstruttore(dati.getIstruttore());
        return corsoRepository.save(corso);
    }

    @Transactional
    public void delete(Long id) {
        if (!corsoRepository.existsById(id)) {
            throw new IllegalArgumentException("Corso non trovato con id: " + id);
        }
        corsoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Corso> findByFiltri(Livello livello, Integer durataMin, Integer durataMax) {
        return corsoRepository.findByFiltri(livello, durataMin, durataMax);
    }
}
