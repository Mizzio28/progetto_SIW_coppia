package it.uniroma3.java.siw.controller.admin;

import it.uniroma3.java.siw.model.Corso;
import it.uniroma3.java.siw.model.Istruttore;
import it.uniroma3.java.siw.model.enums.Livello;
import it.uniroma3.java.siw.service.CorsoService;
import it.uniroma3.java.siw.service.IstruttoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/corsi")
public class AdminCorsoController {

    @Autowired
    private CorsoService corsoService;

    @Autowired
    private IstruttoreService istruttoreService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("corsi", corsoService.findAll());
        return "admin/corsi/list";
    }

    @GetMapping("/new")
    public String nuovoForm(Model model) {
        model.addAttribute("corso", new Corso());
        model.addAttribute("istruttori", istruttoreService.findAll());
        model.addAttribute("livelli", Livello.values());
        return "admin/corsi/form";
    }

    @PostMapping
    public String salva(@Valid @ModelAttribute("corso") Corso corso,
                        BindingResult result,
                        @RequestParam(required = false) Long istruttoreId,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (corsoService.existsByNome(corso.getNome())) {
            result.rejectValue("nome", "duplicate", "Esiste già un corso con questo nome");
        }

        if (result.hasErrors()) {
            model.addAttribute("istruttori", istruttoreService.findAll());
            model.addAttribute("livelli", Livello.values());
            return "admin/corsi/form";
        }

        if (istruttoreId != null) {
            istruttoreService.findById(istruttoreId).ifPresent(corso::setIstruttore);
        }

        Corso saved = corsoService.save(corso);
        redirectAttributes.addFlashAttribute("successo", "Corso \"" + saved.getNome() + "\" creato con successo.");
        return "redirect:/admin/corsi";
    }

    @GetMapping("/{id}/edit")
    public String modificaForm(@PathVariable Long id, Model model) {
        Corso corso = corsoService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Corso non trovato con id: " + id));
        model.addAttribute("corso", corso);
        model.addAttribute("istruttori", istruttoreService.findAll());
        model.addAttribute("livelli", Livello.values());
        return "admin/corsi/form";
    }

    @PostMapping("/{id}/edit")
    public String salvaModifiche(@PathVariable Long id,
                                 @Valid @ModelAttribute("corso") Corso corso,
                                 BindingResult result,
                                 @RequestParam(required = false) Long istruttoreId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        if (corsoService.existsByNomeExcluding(corso.getNome(), id)) {
            result.rejectValue("nome", "duplicate", "Esiste già un corso con questo nome");
        }

        if (result.hasErrors()) {
            model.addAttribute("istruttori", istruttoreService.findAll());
            model.addAttribute("livelli", Livello.values());
            return "admin/corsi/form";
        }

        if (istruttoreId != null) {
            istruttoreService.findById(istruttoreId).ifPresent(corso::setIstruttore);
        } else {
            corso.setIstruttore(null);
        }

        corsoService.update(id, corso);
        redirectAttributes.addFlashAttribute("successo", "Corso \"" + corso.getNome() + "\" aggiornato con successo.");
        return "redirect:/admin/corsi";
    }

    @PostMapping("/{id}/delete")
    public String elimina(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            corsoService.delete(id);
            redirectAttributes.addFlashAttribute("successo", "Corso eliminato con successo.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
        }
        return "redirect:/admin/corsi";
    }
}
