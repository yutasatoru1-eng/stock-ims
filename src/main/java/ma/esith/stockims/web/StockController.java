package ma.esith.stockims.web;

import ma.esith.stockims.service.StockService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller @RequestMapping("/stock") @RequiredArgsConstructor
public class StockController {
    private final StockService service;

    @GetMapping("/{id}")
    public String fiche(@PathVariable("id") Long id, Model model) {
        model.addAttribute("mouvement", new MouvementForm());
        return preparer(id, model);
    }

    /** POST /stock/{id}/entrees ou /stock/{id}/sorties */
    @PostMapping("/{id}/{type:entrees|sorties}")
    public String mouvement(@PathVariable("id") Long id, @PathVariable("type") String type,
                            @Valid @ModelAttribute("mouvement") MouvementForm form,
                            BindingResult erreurs, Model model) {
        if (erreurs.hasErrors()) return preparer(id, model);
        try {
            if (type.equals("entrees")) service.entrer(id, form.getQuantite());
            else service.sortir(id, form.getQuantite());
        } catch (IllegalArgumentException e) {
            erreurs.reject("metier", e.getMessage());
            return preparer(id, model);
        }
        return "redirect:/stock/" + id;
    }

    private String preparer(Long id, Model model) {
        model.addAttribute("article", service.chercher(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        model.addAttribute("stock", service.calculerStock(id));
        return "stock/fiche";
    }
}
