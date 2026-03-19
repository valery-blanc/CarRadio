# FEAT-007 — Réorganisation des favoris par appui long

**Statut :** DONE
**Date :** 2026-03-19

## Contexte
L'ordre des favoris ne peut pas être modifié une fois défini. L'utilisateur veut pouvoir les réorganiser librement, y compris entre pages.

## Comportement
Dans `FavoritesPickerScreen` :
1. **Appui long** sur une tuile remplie → tuile "sélectionnée" (bordure couleur primaire, légère mise en avant)
2. **Tap** sur n'importe quelle autre tuile → échange les deux positions (swap)
   - Remplie → remplie : les deux stations échangent leurs positions
   - Remplie → vide : la station se déplace, l'ancienne position devient vide
3. **Tap** sur la tuile sélectionnée elle-même → désélection
4. Fonctionne entre pages (toutes les pages sont visibles dans le scroll vertical)

## Spec technique
- Interaction : `Modifier.combinedClickable(onLongClick, onClick)`
- État local : `selectedPosition: Int?` dans le composable
- `FavoriteDao` : ajout `updatePosition(uuid, newPosition)`
- `RadioRepository` + `RadioRepositoryImpl` : ajout `swapFavorites(from, to)`
- `FavoritesViewModel` : ajout `swapFavorites(from, to)`
- `MiniSlot` : paramètre `isSelected: Boolean` pour l'affichage de la sélection

## Impact
- `FavoriteDao.kt`
- `RadioRepository.kt` + `RadioRepositoryImpl.kt`
- `FavoritesViewModel.kt`
- `FavoritesPickerScreen.kt`
