package it.uniroma3.java.siw.repository;

import it.uniroma3.siw.model.Corso;
import it.uniroma3.siw.model.enums.Livello;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CorsoRepository extends CrudRepository<Corso, Long> {

    boolean existsByNome(String nome);

    @Query("SELECT COUNT(c) > 0 FROM Corso c WHERE c.nome = :nome AND c.id <> :id")
    boolean existsByNomeAndIdNot(@org.springframework.data.repository.query.Param("nome") String nome,
                                 @org.springframework.data.repository.query.Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Corso c LEFT JOIN FETCH c.istruttore")
    List<Corso> findAllWithIstruttore();

    //@Query("SELECT DISTINCT c FROM Corso c LEFT JOIN FETCH c.istruttore WHERE c.livello = :livello")
    //List<Corso> findByLivelloWithIstruttore(@org.springframework.data.repository.query.Param("livello") Livello livello);

    @Query("SELECT c FROM Corso c LEFT JOIN FETCH c.istruttore WHERE c.id = :id")
    Optional<Corso> findByIdWithIstruttore(@org.springframework.data.repository.query.Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Corso c LEFT JOIN FETCH c.istruttore " +
       "WHERE (:livello IS NULL OR c.livello = :livello) " +
       "AND (:durataMin IS NULL OR c.durataMinuti >= :durataMin) " +
       "AND (:durataMax IS NULL OR c.durataMinuti <= :durataMax)")
    List<Corso> findByFiltri(@org.springframework.data.repository.query.Param("livello") Livello livello,
                          @org.springframework.data.repository.query.Param("durataMin") Integer durataMin,
                          @org.springframework.data.repository.query.Param("durataMax") Integer durataMax);

}
