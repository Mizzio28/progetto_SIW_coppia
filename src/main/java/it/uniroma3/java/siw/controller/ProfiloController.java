package it.uniroma3.java.siw.controller;

import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.service.IscrizioneService;
import it.uniroma3.java.siw.service.PrenotazioneService;
import it.uniroma3.java.siw.service.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profilo")
public class ProfiloController {

    @Autowired
    private UtenteService utenteService;

    @Autowired
    private IscrizioneService iscrizioneService;

    @Autowired
    private PrenotazioneService prenotazioneService;

    @GetMapping
public String mostraProfilo(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    Utente utente = utenteService.findByUsernameWithAbbonamento(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("Utente non trovato"));

    model.addAttribute("utente", utente);
    model.addAttribute("iscrizioni", iscrizioneService.findByUtenteWithCorso(utente));
    model.addAttribute("prenotazione", prenotazioneService.findPrenotazioneAttiva(utente).orElse(null));

    return "profilo/show";
}
}

