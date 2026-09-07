# GAME_RULES_TRIO

## 1. But du document

Ce document résume les règles fonctionnelles du jeu Trio pour le projet.

## 2. Il distingue clairement :

- ce qui relève de la règle générale du jeu
- ce qui est confirmé par la spec
- ce qui est confirmé par le code backend actuel
- ce qui reste à vérifier ou à garder comme connaissance métier



## 3. Vue simple du jeu

Trio est un jeu de cartes où les joueurs essaient de former des **trios**, c'est-à-dire trois cartes de même valeur.

Le paquet contient :

- `36 cartes`
- `12 valeurs`
- `3 cartes par valeur`

Le jeu se joue de `3` à `6` joueurs.

## 4. Variantes

### Variante A

**Spec :**
- gagner avec `3 trios`
- ou gagner immédiatement avec le `trio de 7`

**Code backend :**
- la variante `A` existe
- la victoire est détectée par :
  - `TROIS_TRIOS`
  - ou `TRIO_DE_7`

### Variante B

**Spec :**
- gagner avec `2 trios liés`
- ou gagner immédiatement avec le `trio de 7`

**Code backend :**
- la variante `B` existe
- la victoire est détectée par :
  - `DEUX_TRIOS_LIES`
  - ou `TRIO_DE_7`

**Précision métier utile :**
- dans la spec, l'exemple donné relie `1` avec `6` ou `8`
- dans le code backend actuel, la liaison est calculée par un écart de `5` avec rebouclage sur `12`

## 5. Distribution initiale

### Règle générale

La distribution dépend du nombre de joueurs.

### Confirmé par la spec

| Joueurs | Cartes par joueur | Cartes au centre |
|---|---:|---:|
| 3 | 9 | 9 |
| 4 | 7 | 8 |
| 5 | 6 | 6 |
| 6 | 5 | 6 |

### Confirmé par le code backend

Le backend applique exactement cette répartition.

## 6. Déroulement général d'un tour

### Règle générale

À son tour, un joueur révèle des cartes une par une.

Le tour s'arrête :

- dès que deux valeurs révélées sont différentes
- ou quand trois cartes identiques ont été révélées

### Confirmé par la spec

- le jeu est au tour par tour
- la révélation se fait progressivement
- le joueur continue tant qu'il ne révèle pas deux valeurs différentes
- au maximum, on va jusqu'à trois révélations

### Confirmé par le code backend

Le backend :

- conserve les cartes révélées du tour courant
- termine le tour si au moins `2 valeurs différentes` sont présentes
- termine le tour si `3 cartes` identiques ont été révélées
- sinon laisse le tour en attente d'une nouvelle révélation

## 7. Règles de révélation

### Première révélation

- elle démarre le tour
- aucune résolution immédiate n'a lieu tant qu'on n'a pas assez d'information

### Deuxième révélation

- si la valeur est différente de la première : échec immédiat du tour
- si la valeur est identique : le joueur peut continuer

### Troisième révélation

- si elle est identique aux deux premières : trio gagné
- si elle est différente : échec du tour

### Confirmé par la spec

Oui.

### Confirmé par le code backend

Oui.

Le backend renvoie une résolution de type :

- `EN_ATTENTE`
- `ECHEC_NUMEROS_DIFFERENTS`
- `TRIO_GAGNE`

## 8. Règles de sélection des cartes

### Depuis le centre

**Spec :**
- le joueur peut révéler une carte cachée du centre

**Code backend :**
- le type de coup `REVEAL_CENTER` existe
- l'API permet de cibler une carte du centre par identifiant
- si aucun identifiant n'est fourni, le backend prend la première carte disponible

### Depuis une main

**Spec :**
- on ne peut demander que :
  - la plus petite carte `MIN`
  - ou la plus grande carte `MAX`
- cela vaut pour sa propre main comme pour celle d'un autre joueur

**Code backend :**
- `REVEAL_PLAYER_MIN` existe
- `REVEAL_PLAYER_MAX` existe
- le backend choisit bien la plus petite ou la plus grande carte encore en main

### Ciblage

**Spec :**
- le joueur actif choisit la source ciblée
- il peut demander au même joueur la même requête
- il peut viser sa propre main

**Code backend :**
- aucun verrou spécifique n'interdit de cibler des joueurs différents au sein d'un même tour
- aucun verrou spécifique n'interdit de viser plusieurs fois le même joueur

## 9. Fin de tour

### En cas d'échec

**Spec :**
- les cartes révélées retournent à leur emplacement d'origine
- cartes du centre : redeviennent cachées au centre
- cartes de main : reviennent chez leur propriétaire
- puis on passe au joueur suivant

**Code backend :**
- le backend remet les cartes révélées dans leur position précédente
- le tour passe ensuite au joueur suivant

### En cas de succès

**Spec :**
- le trio est gagné définitivement
- les trois cartes sont prises devant le joueur
- puis on passe au joueur suivant

**Code backend :**
- les trois cartes sont attribuées au joueur dans la position `TRIO_GAGNE`
- le backend vérifie ensuite si une condition de victoire est atteinte
- sinon, on passe au joueur suivant

## 10. Joueur sans carte

### Spec

- un joueur sans carte reste dans la partie
- il ne peut simplement plus révéler depuis sa propre main

### Code backend

- un joueur reste inscrit dans la partie même s'il n'a plus de carte
- une tentative de révélation depuis la main d'un joueur vide provoque un refus métier

## 11. Ordre des joueurs

### Spec

La spec parle d'un ordre de jeu dans le sens des aiguilles d'une montre.

### Code backend actuel

Le backend implémente un ordre déterministe :

- le créateur commence la partie
- puis les autres joueurs suivent dans l'ordre de leurs identifiants

### Lecture correcte

- **cible fonctionnelle spec** : ordre de tour abstrait, type "autour de la table"
- **implémentation actuelle** : ordre déterministe compatible MVP en ligne

Ce point doit être considéré comme une **concrétisation technique actuelle**, pas comme une modélisation fidèle d'une table physique.

## 12. Conditions de victoire

### Confirmé par la spec

- Variante A :
  - `3 trios`
  - ou `trio de 7`
- Variante B :
  - `2 trios liés`
  - ou `trio de 7`

### Confirmé par le code backend

- `TROIS_TRIOS`
- `DEUX_TRIOS_LIES`
- `TRIO_DE_7`

### Ajout observé dans le code backend

Le backend peut aussi terminer une partie avec :

- `AUCUNE_CARTE_JOUABLE`

Cette condition est une capacité d'implémentation observée dans le code actuel.  
Elle n'est pas explicitée comme condition principale de victoire dans la spec initiale.

## 13. Timer d'affichage

### Connaissance métier / idée d'interface

Un timer d'environ `10 secondes` a été envisagé pour l'affichage des cartes révélées.

### Règle documentaire à retenir

- le timer doit être considéré comme une logique **d'affichage uniquement**
- il ne doit pas être présenté comme un blocage métier
- il ne remplace pas la validation backend d'un coup
- il n'est pas confirmé comme fonctionnalité branchée dans le MVP actuel

## 14. Ce qui est confirmé côté backend et branché côté `trio-web`

Le backend expose et le frontend consomme déjà :

- démarrage d'une partie
- consultation de la distribution du joueur connecté
- exécution d'un coup
- calcul de résolution de tour
- calcul de victoire


## 15. Résumé court

### Confirmé par la spec
- jeu de `3` à `6` joueurs
- deux variantes `A` et `B`
- distribution initiale définie
- tour basé sur des révélations successives
- `MIN` / `MAX` pour les révélations depuis une main
- victoire par trios selon la variante

### Confirmé par le code backend
- distribution exacte
- coups `REVEAL_CENTER`, `REVEAL_PLAYER_MIN`, `REVEAL_PLAYER_MAX`
- résolution du tour
- gestion des trios gagnés
- victoire `TROIS_TRIOS`, `DEUX_TRIOS_LIES`, `TRIO_DE_7`
- fin possible par `AUCUNE_CARTE_JOUABLE`

### hors MVP actuel
- timer d'affichage
- bots
- statistiques joueur demandées par la spécification
- statistiques avancées
