package it.uniroma3.java.siw.repository;

import it.uniroma3.java.siw.model.Utente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UtenteRepository extends CrudRepository<Utente, Long> {

    Optional<Utente> findByUsername(String username);

    @Query("SELECT u FROM Utente u LEFT JOIN FETCH u.abbonamento WHERE u.username = :username")
    Optional<Utente> findByUsernameWithAbbonamento(@Param("username") String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

