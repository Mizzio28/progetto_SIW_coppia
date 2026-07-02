package it.uniroma3.java.siw.controller;

import it.uniroma3.java.siw.model.Utente;
import it.uniroma3.java.siw.service.UtenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UtenteService utenteService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registraForm(Model model) {
        model.addAttribute("utente", new Utente());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registra(@Valid @ModelAttribute Utente utente,
                           BindingResult bindingResult) {

        // Controlli di unicità — aggiunge errori campo per campo
        if (utenteService.existsByUsername(utente.getUsername())) {
            bindingResult.rejectValue("username", "duplicate.username",
                    "Username già in uso, sceglierne un altro");
        }
        if (utente.getEmail() != null && !utente.getEmail().isBlank()
                && utenteService.existsByEmail(utente.getEmail())) {
            bindingResult.rejectValue("email", "duplicate.email",
                    "Email già registrata");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        utenteService.registra(utente);
        return "redirect:/login?registrato=true";
    }
}
