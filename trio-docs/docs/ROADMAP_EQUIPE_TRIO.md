# ROADMAP_EQUIPE_TRIO

## 1. But du document

Ce document sert à :

- garder une trace claire de l'état actuel du projet
- conserver les idées produit utiles sans les confondre avec le MVP
- séparer ce qui est déjà branché de ce qui relève d'une discussion future



## 2. État actuel 

### Socle technique

- `trio-api` : backend Spring Boot REST
- `trio-web` : frontend Spring Boot MVC + Thymeleaf
- Docker Compose utilisé pour lancer la stack locale
- JWT côté API
- session HTTP côté web
- Bearer injecté automatiquement sur les appels protégés
- aucune logique métier Trio portée par le frontend

### Fonctionnel branché aujourd'hui

- inscription / connexion / déconnexion frontend branchées
- lobby branché sur des données réelles
- création de partie branchée
- jointure de partie branchée
- lancement de partie branché
- écran de partie branché sur l'API
- consultation de la distribution du joueur connecté
- actions de jeu branchées : révéler une carte du centre, révéler le minimum ou le maximum d'un joueur
- affichage des trios gagnés, du tour courant et de la fin de partie

### Nettoyage déjà acté

- la mention de compte test est uniquement une aide locale et ne doit pas être présentée comme donnée garantie
- `demo()` est une trace technique désactivée

## 3. Idées produit à conserver

Les idées ci-dessous sont utiles, leurs implémentation doit etre discuté au prochain meet.

### Lobby / accès aux parties

- recherche de lobby par nom unique ou identifiant
- parcours d'une liste de lobbies ouverts
- possibilité pour le propriétaire d'accepter ou refuser des entrées
- possibilité future de compléter un lobby avec des bots

### Expérience de jeu

- timer d'affichage sur les révélations
- meilleure vue du tour courant
- suggestions de coups
- aide de lecture de l'état de partie

### Profil joueur

- statistiques joueur avancées
- historique de parties
- suivi par variante
- suivi de style de jeu

## 4. Décisions déjà prises

### Architecture

- pas de logique métier côté frontend
- backend = source de vérité
- Docker obligatoire
- JWT côté API
- session HTTP côté web pour porter le token
- seul `trio-web` est exposé sur `localhost:8080` en Docker Compose
- `trio-api` est appelée par `trio-web` via `http://trio-api:8080`

### MVP

- lobby manuel pour le MVP
- bots non prioritaires pour le MVP
- documentation du MVP centrée sur ce qui est réellement branché
- `game.html` documenté comme écran métier actif
- `/api/parties/demo` non documenté comme feature réelle

## 5. Décisions à prendre plus tard

- faut-il conserver, supprimer définitivement ou réintroduire proprement un mode `/demo` ?
- quel niveau de détail  pour le vrai système de lobby ?
- timer = affichage-only ?
- quel niveau de détail  pour les statistiques ?
- quelle stratégie adopter pour des bots ?
-  historique détaillé des parties et des coups ?
- comment gérer le remplacement d'un joueur déconnecté ?

## 6. Problèmes potentiels / points de vigilance

- ne pas glisser de logique métier côté frontend
- attention au volume d'historique si l'on garde chaque coup
- attention aux données joueur si l'on construit des profils
- ne pas mélanger trop tôt statistiques, suggestions et bots
- ne pas transformer une idée d'UX en contrainte métier
- ne pas documenter une maquette comme une fonctionnalité

## 7. Focus spécial statistiques + bots

### Statistiques envisagées

Pistes possibles :

- parties jouées
- parties gagnées / perdues
- statistiques par variante
- bons / mauvais coups
- intuition correcte / incorrecte sur les révélations
- qualité estimée des décisions
- tendances de style de jeu
- historique exploitable

### Vision long terme

À long terme, chaque joueur pourrait disposer d'un identifiant relié à ses parties et à ses statistiques.

À partir de cet historique, il serait possible plus tard de :

- construire des profils de jeu
- détecter des habitudes
- paramétrer des bots
- entraîner des heuristiques
- proposer des statistiques plus riches, à la manière de plateformes de jeux de stratégie

### Important

Cette piste est présentée comme :

- une **évolution possible**
- **pas** comme le périmètre du push actuel
- un sujet qui mérite d'avoir une discussion autour d'une pause café ou autour d'un meet

## 8. Lecture recommandée pour l'équipe

### Ce qui est prêt à documenter comme réel

- inscription / connexion / déconnexion
- login
- session JWT
- Bearer automatique
- lobby réel
- création
- jointure
- lancement
- écran de partie
- distribution visible côté joueur
- actions de jeu
- architecture Docker actuelle conforme à la spécification

### Ce qui doit rester dans le backlog

- bots
- recherche avancée de lobby
- modération des entrées
- statistiques joueur demandées par la spécification
- stats avancées
- aide à la décision
- historique détaillé des coups

## 9. Résumé court

### MVP actuel
- stack Docker locale
- backend métier + JWT
- frontend MVC
- inscription / login
- lobby réel
- create / join / start
- écran de partie et coups jouables

### Post-MVP possible
- recherche / modération de lobby
- bots
- statistiques enrichies
- suggestions de coups
- historique détaillé
