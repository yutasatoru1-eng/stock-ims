# StockIMS — Gestion de stock

Application web de gestion de stock réalisée avec **Spring Boot**, **MySQL** et **Thymeleaf**.

> ESITH · IMS 2e année · Java et programmation orientée objet · Séances 9 et 10
> Réalisé par : **Ahmed**

---

## Fonctionnalités

- Ajouter un article (référence unique + désignation), avec validation des saisies.
- Afficher la liste des articles et leur stock (badge vert / orange / rouge).
- Ouvrir la fiche d'un article et enregistrer une **entrée** ou une **sortie**.
- **Stock = somme des entrées − somme des sorties.**
- Refus d'une sortie supérieure au stock disponible, avec un message clair.
- Verrou sur l'article (`PESSIMISTIC_WRITE`) pour éviter deux sorties simultanées sur le même stock.

## Technologies

| Outil | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.1 (Web MVC, Data JPA, Validation, Thymeleaf) |
| MySQL | 8.4 |
| Lombok | — |
| Maven | Wrapper inclus (`mvnw`) |

## Architecture

```
src/main/java/ma/esith/stockims/
├── model/        Article, MouvementStock (abstraite), Entree, Sortie
├── repository/   ArticleRepository, EntreeRepository, SortieRepository
├── service/      ArticleService, StockService
└── web/          ArticleController, StockController, ArticleForm, MouvementForm
src/main/resources/
├── templates/    layout.html, articles/liste.html, articles/formulaire.html, stock/fiche.html
├── static/css/   style.css
└── application.properties
```

Trajet d'une requête : **Navigateur → Controller → Service → Repository (JPA) → MySQL**, puis retour via **Thymeleaf**.

### Base de données (3 tables)

| Table | Colonnes |
|---|---|
| `article` | id, reference (unique), designation |
| `entree` | id, article_id, quantite, date_mouvement |
| `sortie` | id, article_id, quantite, date_mouvement |

## Notions de POO utilisées

| Notion | Où ? |
|---|---|
| Encapsulation | Champs `private`, constructeur métier contrôlé dans `Article`, pas de setters |
| Abstraction / héritage | `MouvementStock` (abstraite, `@MappedSuperclass`) → `Entree`, `Sortie` |
| Polymorphisme | `quantiteSignee()` : `+quantite` pour une entrée, `−quantite` pour une sortie |
| Interfaces et génériques | `JpaRepository<Article, Long>`, etc. |
| Collections et exceptions | `List<MouvementStock>`, `Optional`, `IllegalArgumentException` |

## Routes

| Méthode | URL | Rôle |
|---|---|---|
| GET | `/articles` | Liste des articles |
| GET | `/articles/nouveau` | Formulaire d'ajout |
| POST | `/articles` | Enregistrer un article |
| GET | `/stock/{id}` | Fiche et stock d'un article |
| POST | `/stock/{id}/entrees` | Ajouter une entrée |
| POST | `/stock/{id}/sorties` | Ajouter une sortie (refusée si stock insuffisant) |

## Installation et lancement

**1. Créer la base MySQL**

```sql
CREATE DATABASE stock_ims CHARACTER SET utf8mb4;
CREATE USER 'stock_app'@'localhost' IDENTIFIED BY 'VOTRE_MOT_DE_PASSE';
GRANT ALL PRIVILEGES ON stock_ims.* TO 'stock_app'@'localhost';
```

**2. Créer le fichier `secret.properties`** à la racine du projet (à côté de `pom.xml`) :

```
spring.datasource.password=VOTRE_MOT_DE_PASSE
```

> Ce fichier n'est pas versionné (`.gitignore`) afin de ne pas publier le mot de passe.

**3. Lancer l'application**

```
.\mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run          # macOS / Linux
```

Puis ouvrir **http://localhost:8080**. Les tables sont créées automatiquement (`ddl-auto=update`).

## Scénario de test

| Action | Résultat attendu |
|---|---|
| Créer l'article `TP-10` / Bobine de fil | Stock 0 |
| Entrée 40, puis entrée 15 | Stock 55 |
| Sortie 12 | Stock 43 |
| Sortie 44 | **Refusée** : « Stock insuffisant : 43 disponible(s). » |
| Entrée 7, puis sortie 50 | Stock 0 |
| Quantité vide ou 0 | Message de saisie, stock inchangé |
| Réutiliser la référence `TP-10` | « Cette référence existe déjà. » |

Vérification dans MySQL :

```sql
SELECT quantite, date_mouvement FROM entree WHERE article_id = <id>;
SELECT quantite, date_mouvement FROM sortie WHERE article_id = <id>;
```
