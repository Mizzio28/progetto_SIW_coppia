package it.uniroma3.java.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.uniroma3.java.siw.model.enums.Ruolo;

@Entity
@Table(name = "utenti")
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Lo username è obbligatorio")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 6, message = "La password deve contenere almeno 6 caratteri")
    @Column(nullable = false)
    private String password;

    private String nome;

    private String cognome;

    @Email(message = "Inserire un indirizzo email valido")
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Ruolo ruolo;

    @Column(nullable = false)
    private LocalDateTime dataRegistrazione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abbonamento_id")
    private Abbonamento abbonamento;

    @OneToMany(mappedBy = "utente", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Iscrizione> iscrizioni = new ArrayList<>();

    public Utente() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Ruolo getRuolo() { return ruolo; }
    public void setRuolo(Ruolo ruolo) { this.ruolo = ruolo; }

    public LocalDateTime getDataRegistrazione() { return dataRegistrazione; }
    public void setDataRegistrazione(LocalDateTime dataRegistrazione) { this.dataRegistrazione = dataRegistrazione; }

    public Abbonamento getAbbonamento() { return abbonamento; }
    public void setAbbonamento(Abbonamento abbonamento) { this.abbonamento = abbonamento; }

    public List<Iscrizione> getIscrizioni() { return iscrizioni; }
    public void setIscrizioni(List<Iscrizione> iscrizioni) { this.iscrizioni = iscrizioni; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utente)) return false;
        Utente that = (Utente) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Utente{id=" + id + ", username='" + username + "', ruolo=" + ruolo + "}";
    }
}
