package it.uniroma3.java.siw.controller;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.service.CorsoService;
import it.uniroma3.java.siw.service.IscrizioneService;
import it.uniroma3.java.siw.service.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/corsi")
public class IscrizioneController {

    @Autowired
    private IscrizioneService iscrizioneService;

    @Autowired
    private CorsoService corsoService;

    @Autowired
    private UtenteService utenteService;

    @PostMapping("/{id}/iscriviti")
    public String iscriviti(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {

        Corso corso = corsoService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Corso non trovato con id: " + id));

        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));

        try {
            iscrizioneService.iscriviUtente(utente, corso);
            redirectAttributes.addFlashAttribute("successo",
                    "Iscrizione al corso \"" + corso.getNome() + "\" completata!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
        }

        return "redirect:/corsi/" + id;
    }
}

