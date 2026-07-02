package it.uniroma3.java.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

import it.uniroma3.java.siw.model.enums.Dimensione;

@Entity
@Table(name = "armadietti")
public class Armadietto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private Integer numero;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Dimensione dimensione;

    public Armadietto() {}

    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Integer getNumero() { 
        return numero; 
    }

    public void setNumero(Integer numero) { 
        this.numero = numero; 
    }

    public Dimensione getDimensione() { 
        return dimensione; 
    }
    public void setDimensione(Dimensione dimensione) { 
        this.dimensione = dimensione; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Armadietto)) return false;
        Armadietto that = (Armadietto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Armadietto{id=" + id + ", numero=" + numero + ", dimensione=" + dimensione + "}";
    }
    
}
