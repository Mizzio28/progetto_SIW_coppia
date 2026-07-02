package it.uniroma3.java.siw.controller.admin;

import it.uniroma3.java.siw.model.Istruttore;
import it.uniroma3.java.siw.service.IstruttoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/istruttori")
public class AdminIstruttoreController {

    @Autowired
    private IstruttoreService istruttoreService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("istruttori", istruttoreService.findAll());
        return "admin/istruttori/list";
    }

    @GetMapping("/new")
    public String nuovoForm(Model model) {
        model.addAttribute("istruttore", new Istruttore());
        return "admin/istruttori/form";
    }

    @PostMapping
    public String salva(@Valid @ModelAttribute Istruttore istruttore,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (!result.hasFieldErrors("nome") && !result.hasFieldErrors("cognome")
                && istruttoreService.existsByNomeAndCognome(istruttore.getNome(), istruttore.getCognome())) {
            result.rejectValue("nome", "duplicate", "Esiste già un istruttore con questo nome e cognome");
        }

        if (result.hasErrors()) {
            return "admin/istruttori/form";
        }

        Istruttore saved = istruttoreService.save(istruttore);
        redirectAttributes.addFlashAttribute("successo",
                "Istruttore \"" + saved.getNome() + " " + saved.getCognome() + "\" creato con successo.");
        return "redirect:/admin/istruttori";
    }

    @GetMapping("/{id}/edit")
    public String modificaForm(@PathVariable Long id, Model model) {
        Istruttore istruttore = istruttoreService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Istruttore non trovato con id: " + id));
        model.addAttribute("istruttore", istruttore);
        return "admin/istruttori/form";
    }

    @PostMapping("/{id}/edit")
    public String salvaModifiche(@PathVariable Long id,
                                 @Valid @ModelAttribute Istruttore istruttore,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        if (!result.hasFieldErrors("nome") && !result.hasFieldErrors("cognome")
                && istruttoreService.existsByNomeAndCognomeExcluding(istruttore.getNome(), istruttore.getCognome(), id)) {
            result.rejectValue("nome", "duplicate", "Esiste già un istruttore con questo nome e cognome");
        }

        if (result.hasErrors()) {
            return "admin/istruttori/form";
        }

        istruttoreService.update(id, istruttore);
        redirectAttributes.addFlashAttribute("successo",
                "Istruttore \"" + istruttore.getNome() + " " + istruttore.getCognome() + "\" aggiornato con successo.");
        return "redirect:/admin/istruttori";
    }

    @PostMapping("/{id}/delete")
    public String elimina(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            istruttoreService.delete(id);
            redirectAttributes.addFlashAttribute("successo", "Istruttore eliminato con successo.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
        }
        return "redirect:/admin/istruttori";
    }
}

