package it.uniroma3.java.siw.repository;

import it.uniroma3.siw.model.Armadietto;
import it.uniroma3.siw.model.enums.Dimensione;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArmadiettoRepository extends CrudRepository<Armadietto, Long> {

    @Query("SELECT a FROM Armadietto a WHERE a.dimensione = :dimensione " +
           "AND a NOT IN (" +
           "  SELECT p.armadietto FROM Prenotazione p " +
           "  WHERE p.mese = :mese AND p.anno = :anno AND p.stato = 'ATTIVA'" +
           ") ORDER BY a.numero")
    List<Armadietto> findDisponibili(@Param("dimensione") Dimensione dimensione,
                                      @Param("mese") Integer mese,
                                      @Param("anno") Integer anno);
}

