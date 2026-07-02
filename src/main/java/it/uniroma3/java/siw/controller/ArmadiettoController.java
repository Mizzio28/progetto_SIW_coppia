package it.uniroma3.java.siw.controller;

import it.uniroma3.java.siw.model.Prenotazione;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.model.enums.Dimensione;
import it.uniroma3.java.siw.service.PrenotazioneService;
import it.uniroma3.java.siw.service.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/armadietti")
public class ArmadiettoController {

    private static final String[] NOMI_MESI = {
            "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
            "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
    };

    @Autowired
    private PrenotazioneService prenotazioneService;

    @Autowired
    private UtenteService utenteService;

    @GetMapping
    public String form(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));

        LocalDate oggi = LocalDate.now();
        List<Integer> mesiDisponibili = new ArrayList<>();
        for (int m = oggi.getMonthValue(); m <= 12; m++) {
            mesiDisponibili.add(m);
        }

        model.addAttribute("dimensioni", Dimensione.values());
        model.addAttribute("mesiDisponibili", mesiDisponibili);
        model.addAttribute("nomiMesi", NOMI_MESI);
        model.addAttribute("anno", oggi.getYear());
        model.addAttribute("prenotazioneAttiva", prenotazioneService.findPrenotazioneAttiva(utente).orElse(null));

        return "armadietti/prenota";
    }

    @PostMapping("/prenota")
    public String prenota(@RequestParam Dimensione dimensione,
                          @RequestParam Integer mese,
                          @RequestParam Integer anno,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {

        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));

        try {
            Prenotazione prenotazione = prenotazioneService.prenota(utente, dimensione, mese, anno);
            redirectAttributes.addFlashAttribute("successo",
                    "Armadietto n. " + prenotazione.getArmadietto().getNumero() + " prenotato con successo!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
        }

        return "redirect:/armadietti";
    }

    @PostMapping("/annulla")
    public String annulla(@RequestParam Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {

        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));

        try {
            prenotazioneService.annulla(id, utente);
            redirectAttributes.addFlashAttribute("successo", "Prenotazione annullata.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
        }

        return "redirect:/armadietti";
    }
}
