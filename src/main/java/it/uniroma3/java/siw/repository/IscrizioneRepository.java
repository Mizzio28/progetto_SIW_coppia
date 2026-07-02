package it.uniroma3.java.siw.repository;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Iscrizione;
import it.uniroma3.java.siw.model.Utente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IscrizioneRepository extends CrudRepository<Iscrizione, Long> {

    boolean existsByUtenteAndCorso(Utente utente, Corso corso);

    List<Iscrizione> findByUtente(Utente utente);

    @Query("SELECT i FROM Iscrizione i JOIN FETCH i.corso WHERE i.utente = :utente")
    List<Iscrizione> findByUtenteWithCorso(@Param("utente") Utente utente);
}

