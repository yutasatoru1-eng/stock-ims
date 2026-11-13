package ma.esith.stockims.service;

import ma.esith.stockims.model.*;
import ma.esith.stockims.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service @RequiredArgsConstructor
public class StockService {
    private final ArticleRepository articles;
    private final EntreeRepository entrees;
    private final SortieRepository sorties;

    public Optional<Article> chercher(Long id) { return articles.findById(id); }

    @Transactional(readOnly = true)
    public long calculerStock(Long articleId) {
        List<MouvementStock> mouvements = new ArrayList<>(entrees.findByArticle_Id(articleId));
        mouvements.addAll(sorties.findByArticle_Id(articleId));
        long stock = 0;
        for (MouvementStock m : mouvements) stock += m.quantiteSignee(); // polymorphisme
        return stock;
    }

    @Transactional
    public void entrer(Long id, int quantite) {
        entrees.save(new Entree(verrouiller(id), quantite, LocalDate.now()));
    }

    @Transactional
    public void sortir(Long id, int quantite) {
        Article article = verrouiller(id);
        long stock = calculerStock(id);
        if (quantite > stock) throw new IllegalArgumentException("Stock insuffisant : " + stock + " disponible(s).");
        sorties.save(new Sortie(article, quantite, LocalDate.now()));
    }

    private Article verrouiller(Long id) {
        return articles.verrouiller(id).orElseThrow(() -> new IllegalArgumentException("Article introuvable."));
    }
}
