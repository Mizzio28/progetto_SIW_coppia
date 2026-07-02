package it.uniroma3.java.siw.repository;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Recensione;
import it.uniroma3.java.siw.model.Utente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecensioneRepository extends CrudRepository<Recensione, Long> {

    List<Recensione> findByCorsoOrderByDataCreazioneDesc(Corso corso);

    Optional<Recensione> findByUtenteAndCorso(Utente utente, Corso corso);

    boolean existsByUtenteAndCorso(Utente utente, Corso corso);

    @Query("SELECT AVG(r.voto) FROM Recensione r WHERE r.corso.id = :corsoId")
    Double mediaVotoByCorsoId(@Param("corsoId") Long corsoId);
}