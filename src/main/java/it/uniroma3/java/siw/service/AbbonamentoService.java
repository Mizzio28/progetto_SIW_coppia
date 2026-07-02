package it.uniroma3.java.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.java.siw.model.Abbonamento;
import it.uniroma3.java.siw.repository.AbbonamentoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AbbonamentoService {

    @Autowired
    private AbbonamentoRepository abbonamentoRepository;

    @Transactional(readOnly = true)
    public List<Abbonamento> findAll() {
        return (List<Abbonamento>) abbonamentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Abbonamento> findById(Long id) {
        return abbonamentoRepository.findById(id);
    }

    @Transactional
    public Abbonamento save(Abbonamento abbonamento) {
        return abbonamentoRepository.save(abbonamento);
    }

    @Transactional
    public Abbonamento update(Long id, Abbonamento dati) {
        Abbonamento abbonamento = abbonamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abbonamento non trovato con id: " + id));
        abbonamento.setNome(dati.getNome());
        abbonamento.setDescrizione(dati.getDescrizione());
        return abbonamentoRepository.save(abbonamento);
    }

    @Transactional
    public void delete(Long id) {
        if (!abbonamentoRepository.existsById(id)) {
            throw new IllegalArgumentException("Abbonamento non trovato con id: " + id);
        }
        abbonamentoRepository.deleteById(id);
    }
}
