package it.uniroma3.java.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
@Table(name = "abbonamenti")
public class Abbonamento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il nome è obbligatorio")
    @Column(nullable = false)
    private String nome;

    @Size(max = 500, message = "La descrizione non può superare i 500 caratteri")
    @Column(length = 500)
    private String descrizione;

    public Abbonamento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Abbonamento)) return false;
        Abbonamento that = (Abbonamento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Abbonamento{id=" + id + ", nome='" + nome + "'}";
    }
}
