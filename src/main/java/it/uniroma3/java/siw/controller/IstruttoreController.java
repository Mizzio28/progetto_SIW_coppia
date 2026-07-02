package it.uniroma3.java.siw.controller;

import it.uniroma3.java.siw.model.Istruttore;
import it.uniroma3.java.siw.service.IstruttoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/istruttori")
public class IstruttoreController {

    @Autowired
    private IstruttoreService istruttoreService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("istruttori", istruttoreService.findAll());
        return "istruttori/list";
    }

    @GetMapping("/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        Istruttore istruttore = istruttoreService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Istruttore non trovato con id: " + id));
        model.addAttribute("istruttore", istruttore);
        return "istruttori/show";
    }
}

