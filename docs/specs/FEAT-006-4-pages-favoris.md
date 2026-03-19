# FEAT-006 — 4 pages de favoris

**Statut :** DONE
**Date :** 2026-03-19

## Contexte
L'application affiche 2 pages de 8 slots = 16 favoris. L'utilisateur souhaite 4 pages = 32 favoris.

## Comportement ajouté
- HomeScreen : pager passe de 2 à 4 pages, grille de slots 0–31
- FavoritesPickerScreen : affichage de 4 pages dans le scroll vertical

## Spec technique
Aucun changement de schéma Room (position est déjà un Int sans contrainte de max).

## Impact
- `HomeScreen.kt` : pageCount 2→4, slots (0 until 32), indicateur 4 points
- `FavoritesPickerScreen.kt` : repeat(2)→repeat(4)
