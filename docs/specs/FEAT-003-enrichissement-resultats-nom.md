# FEAT-003 — Enrichissement des résultats de recherche par nom

**Statut :** DONE
**Date :** 2026-03-19

## Contexte
Les résultats de la recherche par nom n'affichent que le codec et le bitrate. L'utilisateur ne peut pas identifier la localisation ni la langue d'une station sans cliquer dessus.

## Comportement ajouté
- Les résultats de la recherche par nom affichent, en plus du format : la ville (`state`), le pays (`country`) et la langue (`language`)
- Format d'affichage : `MP3 · 128k · Île-de-France · France · French`
  (les champs vides sont omis)
- Limite de résultats réduite de 100 à 30

## Spec technique
- `StationDto` : ajout des champs `countrysubdivision`, `languages`
- `RadioStation` : ajout des champs `subdivision: String`, `languages: String`
- `RadioBrowserApi.searchStationsByName` : `limit` passe de 100 à 30
- `StationItem` (composant partagé) : supporting text enrichi

## Impact
- `StationDto.kt`
- `RadioStation.kt`
- `RadioBrowserApi.kt`
- `StationListScreen.kt` (StationItem)
