package it.uniroma3.java.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.uniroma3.java.siw.model.enums.Livello;

@Entity
@Table(name = "corsi")
public class Corso {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il nome è obbligatorio")
    @Column(nullable = false)
    private String nome;

    @Size(max = 500, message = "La descrizione non può superare i 500 caratteri")
    @Column(length = 500)
    private String descrizione;

    @NotNull(message = "Il livello è obbligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Livello livello;

    @Positive(message = "La durata deve essere un valore positivo")
    private Integer durataMinuti;

    private String urlImmagine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "istruttore_id")
    private Istruttore istruttore;

    @OneToMany(mappedBy = "corso", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Iscrizione> iscrizioni = new ArrayList<>();

    public Corso() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public Livello getLivello() { return livello; }
    public void setLivello(Livello livello) { this.livello = livello; }

    public Integer getDurataMinuti() { return durataMinuti; }
    public void setDurataMinuti(Integer durataMinuti) { this.durataMinuti = durataMinuti; }

    public String getUrlImmagine() { return urlImmagine; }
    public void setUrlImmagine(String urlImmagine) { this.urlImmagine = urlImmagine; }

    public Istruttore getIstruttore() { return istruttore; }
    public void setIstruttore(Istruttore istruttore) { this.istruttore = istruttore; }

    public List<Iscrizione> getIscrizioni() { return iscrizioni; }
    public void setIscrizioni(List<Iscrizione> iscrizioni) { this.iscrizioni = iscrizioni; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Corso)) return false;
        Corso corso = (Corso) o;
        return Objects.equals(id, corso.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Corso{id=" + id + ", nome='" + nome + "', livello=" + livello + "}";
    }
}
