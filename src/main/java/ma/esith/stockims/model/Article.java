package ma.esith.stockims.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "article")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 20)
    private String reference;
    @Column(nullable = false, length = 80)
    private String designation;

    public Article(String reference, String designation) {
        this.reference = valider(reference, 20, "Référence invalide.");
        this.designation = valider(designation, 80, "Désignation invalide.");
    }

    private static String valider(String v, int max, String msg) {
        if (v == null || v.isBlank() || v.strip().length() > max) throw new IllegalArgumentException(msg);
        return v.strip();
    }
}
