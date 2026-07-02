package it.uniroma3.java.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "istruttori")
public class Istruttore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il nome è obbligatorio")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Column(nullable = false)
    private String cognome;

    @Past(message = "La data di nascita deve essere nel passato")
    private LocalDate dataDiNascita;

    private String specializzazione;

    @PositiveOrZero(message = "Gli anni di esperienza non possono essere negativi")
    private Integer anniDiEsperienza;

    private String urlFoto;

    @OneToMany(mappedBy = "istruttore", fetch = FetchType.LAZY)
    private List<Corso> corsi = new ArrayList<>();

    public Istruttore() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public LocalDate getDataDiNascita() { return dataDiNascita; }
    public void setDataDiNascita(LocalDate dataDiNascita) { this.dataDiNascita = dataDiNascita; }

    public String getSpecializzazione() { return specializzazione; }
    public void setSpecializzazione(String specializzazione) { this.specializzazione = specializzazione; }

    public Integer getAnniDiEsperienza() { return anniDiEsperienza; }
    public void setAnniDiEsperienza(Integer anniDiEsperienza) { this.anniDiEsperienza = anniDiEsperienza; }

    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }

    public List<Corso> getCorsi() { return corsi; }
    public void setCorsi(List<Corso> corsi) { this.corsi = corsi; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Istruttore)) return false;
        Istruttore that = (Istruttore) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Istruttore{id=" + id + ", nome='" + nome + "', cognome='" + cognome + "'}";
    }
}
