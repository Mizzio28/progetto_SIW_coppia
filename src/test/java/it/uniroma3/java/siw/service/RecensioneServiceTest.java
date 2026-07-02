package it.uniroma3.java.siw.service;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Recensione;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.model.enums.Ruolo;
import it.uniroma3.java.siw.repository.IscrizioneRepository;
import it.uniroma3.java.siw.repository.RecensioneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecensioneServiceTest {

    @Mock private RecensioneRepository recensioneRepository;
    @Mock private IscrizioneRepository iscrizioneRepository;
    @InjectMocks private RecensioneService recensioneService;

    private Utente utente;
    private Utente altroUtente;
    private Utente admin;
    private Corso corso;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setId(1L);
        utente.setRuolo(Ruolo.USER);

        altroUtente = new Utente();
        altroUtente.setId(2L);
        altroUtente.setRuolo(Ruolo.USER);

        admin = new Utente();
        admin.setId(3L);
        admin.setRuolo(Ruolo.ADMIN);

        corso = new Corso();
        corso.setId(10L);
    }

    @Test
    void crea_lanciaEccezione_seUtenteNonIscritto() {
        when(iscrizioneRepository.existsByUtenteAndCorso(utente, corso)).thenReturn(false);

        assertThatThrownBy(() -> recensioneService.crea(utente, corso, 5, "Ottimo corso"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("iscritto");

        verify(recensioneRepository, never()).save(any());
    }

    @Test
    void crea_lanciaEccezione_seRecensioneGiaEsistente() {
        when(iscrizioneRepository.existsByUtenteAndCorso(utente, corso)).thenReturn(true);
        when(recensioneRepository.existsByUtenteAndCorso(utente, corso)).thenReturn(true);

        assertThatThrownBy(() -> recensioneService.crea(utente, corso, 5, "Ottimo corso"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("già recensito");

        verify(recensioneRepository, never()).save(any());
    }

    @Test
    void crea_salvaRecensione_seIscrittoESenzaRecensionePrecedente() {
        when(iscrizioneRepository.existsByUtenteAndCorso(utente, corso)).thenReturn(true);
        when(recensioneRepository.existsByUtenteAndCorso(utente, corso)).thenReturn(false);
        when(recensioneRepository.save(any(Recensione.class))).thenAnswer(inv -> inv.getArgument(0));

        Recensione risultato = recensioneService.crea(utente, corso, 4, "Bello");

        assertThat(risultato.getVoto()).isEqualTo(4);
        assertThat(risultato.getTesto()).isEqualTo("Bello");
        assertThat(risultato.getUtente()).isEqualTo(utente);
        assertThat(risultato.getCorso()).isEqualTo(corso);
        assertThat(risultato.getDataCreazione()).isNotNull();
    }

    @Test
    void modifica_lanciaEccezione_seRecensioneNonTrovata() {
        when(recensioneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recensioneService.modifica(99L, utente, 3, "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void modifica_lanciaEccezione_seNonAutore() {
        Recensione esistente = new Recensione();
        esistente.setId(5L);
        esistente.setUtente(utente);
        when(recensioneRepository.findById(5L)).thenReturn(Optional.of(esistente));

        assertThatThrownBy(() -> recensioneService.modifica(5L, altroUtente, 3, "x"))
                .isInstanceOf(SecurityException.class);

        verify(recensioneRepository, never()).save(any());
    }

    @Test
    void modifica_aggiornaCampi_seAutore() {
        Recensione esistente = new Recensione();
        esistente.setId(5L);
        esistente.setUtente(utente);
        esistente.setVoto(2);
        esistente.setTesto("vecchio");
        when(recensioneRepository.findById(5L)).thenReturn(Optional.of(esistente));
        when(recensioneRepository.save(any(Recensione.class))).thenAnswer(inv -> inv.getArgument(0));

        Recensione risultato = recensioneService.modifica(5L, utente, 5, "nuovo");

        assertThat(risultato.getVoto()).isEqualTo(5);
        assertThat(risultato.getTesto()).isEqualTo("nuovo");
    }

    @Test
    void elimina_lanciaEccezione_seNonAutoreNeAdmin() {
        Recensione esistente = new Recensione();
        esistente.setId(5L);
        esistente.setUtente(utente);
        when(recensioneRepository.findById(5L)).thenReturn(Optional.of(esistente));

        assertThatThrownBy(() -> recensioneService.elimina(5L, altroUtente))
                .isInstanceOf(SecurityException.class);

        verify(recensioneRepository, never()).deleteById(any());
    }

    @Test
    void elimina_permetteEliminazione_seAutore() {
        Recensione esistente = new Recensione();
        esistente.setId(5L);
        esistente.setUtente(utente);
        when(recensioneRepository.findById(5L)).thenReturn(Optional.of(esistente));

        recensioneService.elimina(5L, utente);

        verify(recensioneRepository).deleteById(5L);
    }

    @Test
    void elimina_permetteEliminazione_seAdmin() {
        Recensione esistente = new Recensione();
        esistente.setId(5L);
        esistente.setUtente(utente);
        when(recensioneRepository.findById(5L)).thenReturn(Optional.of(esistente));

        recensioneService.elimina(5L, admin);

        verify(recensioneRepository).deleteById(5L);
    }
}