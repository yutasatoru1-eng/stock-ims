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

**Prérequis :** Java 21+, MySQL 8, Git (ou téléchargement ZIP). Maven n'est pas nécessaire : le wrapper `mvnw` est inclus.

**1. Récupérer le projet**

```
git clone https://github.com/VOTRE_USERNAME/stock-ims.git
cd stock-ims
```

Ou : bouton vert **Code → Download ZIP** sur GitHub, puis extraire.

**2. Créer la base MySQL** (dans MySQL Workbench ou le client `mysql`, connecté en root)

```sql
CREATE DATABASE stock_ims CHARACTER SET utf8mb4;
CREATE USER 'stock_app'@'localhost' IDENTIFIED BY 'stock123';
GRANT ALL PRIVILEGES ON stock_ims.* TO 'stock_app'@'localhost';
FLUSH PRIVILEGES;
```

**3. Créer le fichier `secret.properties`** à la racine du projet (à côté de `pom.xml`), avec cette seule ligne :

```
spring.datasource.password=stock123
```

> Ce fichier n'est pas versionné (`.gitignore`) afin de ne pas publier de mot de passe. Il est lu grâce à `spring.config.import` dans `application.properties`.

**4. Lancer l'application** depuis un terminal ouvert dans le dossier du projet

```
.\mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run          # macOS / Linux
```

**5. Ouvrir http://localhost:8080** après le message `Started StockImsApplication`. Les tables `article`, `entree` et `sortie` sont créées automatiquement (`ddl-auto=update`).

### Dépannage

| Message | Solution |
|---|---|
| `Access denied for user 'stock_app'` | Vérifier le mot de passe dans `secret.properties` et l'emplacement du fichier (à côté de `pom.xml`). |
| `Communications link failure` | Démarrer le service MySQL (`services.msc` sous Windows). |
| `Unknown database 'stock_ims'` | Exécuter l'étape 2. |
| `Port 8080 was already in use` | Arrêter l'autre instance de l'application. |

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
