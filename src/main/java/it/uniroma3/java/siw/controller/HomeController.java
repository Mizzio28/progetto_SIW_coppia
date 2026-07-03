package it.uniroma3.java.siw.controller;

import it.uniroma3.java.siw.service.AbbonamentoService;
import it.uniroma3.java.siw.service.CorsoService;
import it.uniroma3.java.siw.service.UtenteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    //@Autowired 
    //private CorsoService corsoService;

    @Autowired
    private UtenteService utenteService;

     @GetMapping("/")
     public String home(Model model) {
        //model.addAttribute("numeroCorsi", corsoService.count());
        model.addAttribute("numeroUtenti", utenteService.count());
        return "index"; 
    }    
}
