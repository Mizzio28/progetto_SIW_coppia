package it.uniroma3.java.siw.controller.rest;

import it.uniroma3.java.siw.controller.dto.RecensioneDTO;
import it.uniroma3.java.siw.controller.dto.RecensioneRequest;
import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Recensione;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.service.CorsoService;
import it.uniroma3.java.siw.service.RecensioneService;
import it.uniroma3.java.siw.service.UtenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/corsi/{corsoId}/recensioni")
public class RecensioneRestController {

    @Autowired private RecensioneService recensioneService;
    @Autowired private CorsoService corsoService;
    @Autowired private UtenteService utenteService;

    @GetMapping
    public Map<String, Object> lista(@PathVariable Long corsoId) {
        Corso corso = corsoService.findById(corsoId)
                .orElseThrow(() -> new IllegalArgumentException("Corso non trovato con id: " + corsoId));
        List<RecensioneDTO> recensioni = recensioneService.findByCorso(corso).stream()
                .map(RecensioneDTO::from)
                .collect(Collectors.toList());
        Double media = recensioneService.mediaVoto(corsoId);
        return Map.of("recensioni", recensioni, "mediaVoto", media == null ? 0 : media);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecensioneDTO crea(@PathVariable Long corsoId,
                              @Valid @RequestBody RecensioneRequest request,
                              @AuthenticationPrincipal UserDetails userDetails) {
        Corso corso = corsoService.findById(corsoId)
                .orElseThrow(() -> new IllegalArgumentException("Corso non trovato con id: " + corsoId));
        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));
        Recensione recensione = recensioneService.crea(utente, corso, request.voto(), request.testo());
        return RecensioneDTO.from(recensione);
    }

    @PutMapping("/{id}")
    public RecensioneDTO modifica(@PathVariable Long corsoId,
                                  @PathVariable Long id,
                                  @Valid @RequestBody RecensioneRequest request,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));
        Recensione recensione = recensioneService.modifica(id, utente, request.voto(), request.testo());
        return RecensioneDTO.from(recensione);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long corsoId,
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserDetails userDetails) {
        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));
        recensioneService.elimina(id, utente);
    }
}