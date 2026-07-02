package it.uniroma3.java.siw.repository;

import it.uniroma3.siw.model.Prenotazione;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.model.enums.StatoPrenotazione;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrenotazioneRepository extends CrudRepository<Prenotazione, Long> {

    @Query("SELECT p FROM Prenotazione p LEFT JOIN FETCH p.armadietto " +
           "WHERE p.utente = :utente AND p.stato = :stato")
    Optional<Prenotazione> findByUtenteAndStatoWithArmadietto(@Param("utente") Utente utente,
                                                               @Param("stato") StatoPrenotazione stato);
}

