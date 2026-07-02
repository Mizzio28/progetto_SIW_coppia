package it.uniroma3.java.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

import it.uniroma3.java.siw.model.enums.StatoPrenotazione;

@Entity
@Table(name = "prenotazioni")
public class Prenotazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "armadietto_id", nullable = false)
    private Armadietto armadietto;

    @NotNull
    @Min(1) @Max(12)
    @Column(nullable = false)
    private Integer mese;

    @NotNull
    @Column(nullable = false)
    private Integer anno;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoPrenotazione stato;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dataPrenotazione;

     public Prenotazione() {}

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public Utente getUtente() { 
        return utente; 
    }
    public void setUtente(Utente utente) { 
        this.utente = utente; 
    }

    public Armadietto getArmadietto() { 
        return armadietto; 
    }
    public void setArmadietto(Armadietto armadietto) { 
        this.armadietto = armadietto; 
    }

    public Integer getMese() { 
        return mese; 
    }
    public void setMese(Integer mese) { 
        this.mese = mese; 
    }

    public Integer getAnno() { 
        return anno; 
    }
    public void setAnno(Integer anno) { 
        this.anno = anno; 
    }

    public StatoPrenotazione getStato() { 
        return stato; 
    }
    public void setStato(StatoPrenotazione stato) { 
        this.stato = stato; 
    }

    public LocalDateTime getDataPrenotazione() { 
        return dataPrenotazione; 
    }
    public void setDataPrenotazione(LocalDateTime dataPrenotazione) { 
        this.dataPrenotazione = dataPrenotazione; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prenotazione)) return false;
        Prenotazione that = (Prenotazione) o;       
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { 
        return Objects.hash(id);
    }

    @Override
    public String toString() {
    return "Prenotazione{id=" + id + ", mese=" + mese + ", anno=" + anno + ", stato=" + stato + "}";
    }   
}
