package it.uniroma3.java.siw.controller;

import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.service.AbbonamentoService;
import it.uniroma3.java.siw.service.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/abbonamenti")
public class AbbonamentoController {

    @Autowired
    private AbbonamentoService abbonamentoService;

    @Autowired
    private UtenteService utenteService;

    @GetMapping("/scegli")
    public String scegli(Model model) {
        model.addAttribute("abbonamenti", abbonamentoService.findAll());
        return "abbonamenti/scegli";
    }

    @PostMapping("/{id}/sottoscrivi")
    public String sottoscrivi(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        Utente utente = utenteService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));

        utenteService.sottoscriviAbbonamento(utente.getId(), id);
        redirectAttributes.addFlashAttribute("successo", "Abbonamento sottoscritto con successo.");
        return "redirect:/profilo";
    }
}
