# FEAT-002 — Recherche de stations par nom

**Statut :** DONE
**Date :** 2026-03-19

## Contexte
L'écran de sélection de station ne permettait que la navigation par pays. Impossible de trouver directement une station dont on connaît le nom sans savoir quel pays choisir.

## Comportement ajouté
L'écran `CountryPickerScreen` est scindé en deux sections :

### Section 1 — Recherche par nom (nouvelle)
- Champ texte en haut de l'écran
- Sur appui sur le bouton "Retour" du clavier (action Search/Done), déclenche une requête vers Radio Browser :
  `GET /json/stations/search?name=XXXX&order=clickcount&reverse=true&hidebroken=true&limit=100`
- Affiche les résultats en liste (même composant que StationListScreen)
- Tap sur une station → l'ajoute directement au slot et retourne à FavoritesPickerScreen

### Section 2 — Recherche par pays (existante)
- Inchangée : liste des pays, tap → StationListScreen

## Spec technique
- Nouvel endpoint dans `RadioBrowserApi` : `searchStationsByName(name)`
- Nouvelle méthode dans `RadioRepository` : `searchStationsByName`
- Nouvel état dans `FavoritesViewModel` : `nameSearchState`
- `CountryPickerScreen` reçoit un nouveau callback `onStationDirectlySelected`
- NavGraph passe ce callback pour revenir directement à `FavoritesPickerScreen`

## Impact
- `RadioBrowserApi.kt` — ajout endpoint
- `RadioRepository.kt` + `RadioRepositoryImpl.kt` — ajout méthode
- `FavoritesViewModel.kt` — ajout état + action
- `CountryPickerScreen.kt` — refonte UI avec 2 sections
- `NavGraph.kt` — ajout callback `onStationDirectlySelected`
