package it.uniroma3.java.siw.controller;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.model.enums.Livello;
import it.uniroma3.java.siw.service.CorsoService;
import it.uniroma3.java.siw.service.IscrizioneService;
import it.uniroma3.java.siw.service.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/corsi")
public class CorsoController {

    @Autowired
    private CorsoService corsoService;

    @Autowired
    private IscrizioneService iscrizioneService;

    @Autowired
    private UtenteService utenteService;

   @GetMapping
public String lista(@RequestParam(required = false) Livello livello,
                    @RequestParam(required = false) Integer durataMin,
                    @RequestParam(required = false) Integer durataMax,
                    Model model) {
    List<Corso> corsi = corsoService.findByFiltri(livello, durataMin, durataMax);
    model.addAttribute("corsi", corsi);
    model.addAttribute("livelli", Livello.values());
    model.addAttribute("livelloSelezionato", livello);
    model.addAttribute("durataMinSelezionata", durataMin);
    model.addAttribute("durataMaxSelezionata", durataMax);
    return "corsi/list";
}


    @GetMapping("/{id}")
    public String dettaglio(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        Corso corso = corsoService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Corso non trovato con id: " + id));

        model.addAttribute("corso", corso);

        if (userDetails != null) {
            Utente utente = utenteService.findByUsername(userDetails.getUsername()).orElse(null);
            if (utente != null) {
                model.addAttribute("giaIscritto", iscrizioneService.esisteIscrizione(utente, corso));
            }
        }

        return "corsi/show";
    }

    
    long calcolaDurataTotale(List<Corso> corsi) {
        return corsi.stream()
                .mapToLong(Corso::getDurataMinuti)
                .sum();
    }
}

