package it.uniroma3.java.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Objects;

import it.uniroma3.java.siw.model.enums.StatoIscrizione;

@Entity
@Table(name = "iscrizioni", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"utente_id", "corso_id"})
})
public class Iscrizione {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "La data di iscrizione è obbligatoria")
    @Column(nullable = false)
    private LocalDateTime dataIscrizione;

    @NotNull(message = "Lo stato è obbligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoIscrizione stato;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corso_id", nullable = false)
    private Corso corso;

    public Iscrizione() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDataIscrizione() { return dataIscrizione; }
    public void setDataIscrizione(LocalDateTime dataIscrizione) { this.dataIscrizione = dataIscrizione; }

    public StatoIscrizione getStato() { return stato; }
    public void setStato(StatoIscrizione stato) { this.stato = stato; }

    public Utente getUtente() { return utente; }
    public void setUtente(Utente utente) { this.utente = utente; }

    public Corso getCorso() { return corso; }
    public void setCorso(Corso corso) { this.corso = corso; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Iscrizione)) return false;
        Iscrizione that = (Iscrizione) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Iscrizione{id=" + id + ", stato=" + stato + ", dataIscrizione=" + dataIscrizione + "}";
    }
}
