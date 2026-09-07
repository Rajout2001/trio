# Trio API - Documentation Backend

API backend du jeu Trio, construite avec Spring Boot, Spring Security (JWT), Spring Data JPA et H2.

## 1) Objectif du backend

Le module `trio-api` fournit:
- authentification des utilisateurs
- gestion complete du cycle de vie des parties
- application des regles metier Trio
- statistiques du joueur connecte
- endpoints documentes via OpenAPI/Swagger

## 2) Stack technique

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- H2 Database
- JWT (io.jsonwebtoken)
- springdoc-openapi

## 3) Lancement et configuration

### Configuration principale
Fichier: `trio-api/src/main/resources/application.properties`

Parametres importants:
- `server.port=8080`
- `spring.datasource.url` (H2 fichier local)
- `spring.jpa.hibernate.ddl-auto=update`
- `jwt.secret`
- `jwt.expiration-ms` (access token)
- `jwt.refresh-expiration-ms` (refresh token, valeur par defaut 7 jours)

### Demarrage local
Depuis `trio-api`:
```bash
./mvnw spring-boot:run
```
Sous Windows PowerShell:
```powershell
.\mvnw.cmd spring-boot:run
```

### Tests
```bash
./mvnw test
```
Sous Windows PowerShell:
```powershell
.\mvnw.cmd test
```

### Swagger
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/api-docs`

## 4) Architecture du code

- `controller/`: expose les endpoints HTTP
- `service/`: logique metier et securite applicative
- `security/`: JWT + filtre + registre de tokens
- `repository/`: acces base de donnees
- `model/`: entites JPA
- `dto/`: contrats entree/sortie API
- `config/`: config Spring Security, OpenAPI, gestion erreurs

## 5) Securite et authentification

### Principe
- API stateless
- authentification via header `Authorization: Bearer <accessToken>`
- routes `/api/auth/**`, swagger, h2-console autorisees sans token
- toutes les autres routes sont protegees

### JWT
Deux types de tokens:
- access token: court, utilise pour appeler les endpoints proteges
- refresh token: sert a regenerer un access token

Le claim `token_type` distingue `access` et `refresh`.

### Logout et revocation
`TokenRegistryService` maintient en memoire:
- les access tokens revoques (jusqu a expiration)
- les refresh tokens actifs

Au logout:
- access token est marque revoque
- refresh token est retire du registre

## 6) Endpoints exposes

## Health
- `GET /`

## Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `PATCH /api/auth/me`
- `POST /api/auth/logout`

### Exemple register
```json
{
  "email": "joueur1@test.com",
  "pseudo": "joueur1",
  "password": "Password123"
}
```

### Exemple refresh
```json
{
  "refreshToken": "<refresh-token>"
}
```

### Exemple logout
```json
{
  "refreshToken": "<refresh-token-a-revoquer>"
}
```

## Parties
- `GET /api/parties`
- `GET /api/parties/lobby`
- `GET /api/parties/terminees`
- `GET /api/parties/{partieId}`
- `POST /api/parties`
- `POST /api/parties/{partieId}/join`
- `POST /api/parties/{partieId}/start`
- `GET /api/parties/{partieId}/distribution/me`
- `POST /api/parties/{partieId}/moves`

### Exemple creation de partie
```json
{
  "variante": "A",
  "nombreJoueurs": 4,
  "partiePrivee": true,
  "motDePasse": "secret123"
}
```

### Exemple rejoindre une partie privee
```json
{
  "motDePasse": "secret123"
}
```

### Exemple coup de jeu
```json
{
  "type": "REVEAL_PLAYER_MIN",
  "targetPlayerPseudo": "joueur2"
}
```

Types de coup:
- `REVEAL_CENTER`
- `REVEAL_PLAYER_MIN`
- `REVEAL_PLAYER_MAX`

## Stats
- `GET /api/stats/me`

Retourne:
- infos joueur
- parties jouees/victoires
- detail par variante

## 7) Regles metier implementees

- deck de 36 cartes (valeurs 1..12, 3 exemplaires)
- 3 a 6 joueurs
- distribution:
  - 3 joueurs: 9 en main, 9 au centre
  - 4 joueurs: 7 en main, 8 au centre
  - 5 joueurs: 6 en main, 6 au centre
  - 6 joueurs: 5 en main, 6 au centre

Resolution d un tour:
- valeurs differentes: echec, cartes re-cachees, passage au joueur suivant
- 3 memes valeurs: trio gagne

Conditions de victoire:
- `TRIO_DE_7`
- variante A: `TROIS_TRIOS`
- variante B: `DEUX_TRIOS_LIES` selon les liaisons imprimees:
  - 1 -> 6 et 8
  - 2 -> 5 et 9
  - 3 -> 4 et 10
  - 4 -> 3 et 11
  - 5 -> 2 et 12
  - 6 -> 1
  - 7 -> aucune liaison (trio de 7 = victoire immediate)
  - 8 -> 1
  - 9 -> 2
  - 10 -> 3
  - 11 -> 4
  - 12 -> 5
- fin sans cartes jouables: `AUCUNE_CARTE_JOUABLE`

## 8) Entites principales

- `AppUser`: compte utilisateur (email, pseudo, passwordHash)
- `Partie`: etat de la partie (statut, createur, joueurs, joueur courant, gagnant, flags de resolution, partie privee)
- `CartePartie`: carte rattachee a une partie avec position (`MAIN_JOUEUR`, `CENTRE_CACHE`, `MAIN_REVELEE`, `CENTRE_REVELEE`, `TRIO_GAGNE`)

## 9) Gestion des erreurs

- erreurs metier via `ResponseStatusException` (403/404/409...)
- mauvais credentials via `BadCredentialsException` mappe en `401`
- validations DTO via annotations Jakarta (`@NotBlank`, `@Email`, `@Min`, `@Max`)

## 10) Limitations actuelles

- registre de tokens en memoire (non partage entre instances)
- H2 + `ddl-auto=update` adapte dev/test, pas production
- pas de migration schema (Flyway/Liquibase)
- pas de websocket natif pour synchro temps reel

## 11) Fichiers clefs pour soutenance

- `config/SecurityConfig.java`
- `security/JwtService.java`
- `security/JwtAuthenticationFilter.java`
- `security/TokenRegistryService.java`
- `service/AuthService.java`
- `service/PartieService.java`
- `controller/AuthController.java`
- `controller/PartieController.java`
- `controller/StatsController.java`
- `test/ApiEndpointsIntegrationTest.java`
