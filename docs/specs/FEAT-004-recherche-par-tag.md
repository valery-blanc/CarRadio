# FEAT-004 — Recherche de stations par tag

**Statut :** DONE
**Date :** 2026-03-19

## Contexte
La recherche par nom nécessite de connaître le nom exact. La recherche par tag permet de découvrir des stations par genre musical ou style (rock, jazz, pop…).

## Comportement ajouté
Nouvelle section dans `CountryPickerScreen`, entre la recherche par nom et la recherche par pays.

### Saisie du tag
- Champ texte avec auto-complétion
- À chaque frappe, requête vers Radio Browser :
  `GET /json/tags?name=XXXX&order=stationcount&reverse=true&hidebroken=true&limit=10`
- Un menu déroulant affiche les 10 suggestions (nom du tag + nombre de stations)
- Sélection d'un tag → déclenche la recherche de stations

### Résultats
- Requête : `GET /json/stations/search?tag=TAG&order=clickcount&reverse=true&hidebroken=true&limit=50`
- Affichage : nom de la station, pays (`country`), tags (`tags`) en supporting text
- Tap sur une station → ajout au slot + retour à FavoritesPickerScreen

## Spec technique
- `TagDto.kt` : nouveau DTO `{ name: String, stationcount: Int }`
- `RadioBrowserApi` : ajout `getTagSuggestions(name)` et `getStationsByTag(tag)`
- `RadioRepository` + `RadioRepositoryImpl` : ajout des deux méthodes
- `FavoritesViewModel` : ajout `tagSearchQuery`, `tagSuggestions`, `tagSearchState` + actions
- `CountryPickerScreen` : nouvelle section avec `ExposedDropdownMenuBox`
- `StationDto` : ajout champ `tags`

## Impact
- `TagDto.kt` (nouveau)
- `StationDto.kt`
- `RadioStation.kt`
- `RadioBrowserApi.kt`
- `RadioRepository.kt` + `RadioRepositoryImpl.kt`
- `FavoritesViewModel.kt`
- `CountryPickerScreen.kt`
