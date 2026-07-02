package it.uniroma3.java.siw.repository;

import it.uniroma3.siw.model.Istruttore;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface IstruttoreRepository extends CrudRepository<Istruttore, Long> {

    boolean existsByNomeAndCognome(String nome, String cognome);

    @Query("SELECT COUNT(i) > 0 FROM Istruttore i WHERE i.nome = :nome AND i.cognome = :cognome AND i.id <> :id")
    boolean existsByNomeAndCognomeAndIdNot(@org.springframework.data.repository.query.Param("nome") String nome,
                                           @org.springframework.data.repository.query.Param("cognome") String cognome,
                                           @org.springframework.data.repository.query.Param("id") Long id);

    @Query("SELECT COUNT(c) FROM Corso c WHERE c.istruttore.id = :id")
    long countCorsiByIstruttoreId(@org.springframework.data.repository.query.Param("id") Long id);

    @Query("SELECT DISTINCT i FROM Istruttore i LEFT JOIN FETCH i.corsi")
    List<Istruttore> findAllWithCorsi();

    @Query("SELECT DISTINCT i FROM Istruttore i LEFT JOIN FETCH i.corsi WHERE i.id = :id")
    Optional<Istruttore> findByIdWithCorsi(@org.springframework.data.repository.query.Param("id") Long id);


}

