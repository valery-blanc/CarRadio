# FEAT-012 — Refonte interface principale

**Statut :** DONE
**Version :** v1.12

---

## Contexte

Refonte complète de la navigation et de l'interface principale :
- La page recherche est intégrée dans le HorizontalPager
- La gestion des favoris se fait directement depuis les tuiles (appui long)
- La page FavoritesPickerScreen est supprimée
- Le menu principal regroupe Settings, Timer, et ajout de page

---

## Comportements

### HorizontalPager dynamique
- Pages = N pages de favoris + 1 page de recherche (toujours en dernier)
- À la première ouverture (0 pages de favoris) : seule la page recherche est affichée
- Le nombre de pages de favoris est persisté dans SharedPreferences (`favorite_page_count`)
- Quand on ajoute un favori et que toutes les pages sont pleines (ou inexistantes) : une nouvelle page est créée automatiquement

### Barre de titre (TopAppBar)
- Icône CarRadio + titre "Auto Radio"
- Si timer actif : 🌙 mm:ss (cliquable → naviguer vers timer)
- Si radio en lecture : bouton stop ■
- Bouton menu (⋮) avec :
  - Paramètres ⚙️
  - Timer ⏳
  - Ajouter une page de favoris

### Comportement des tuiles
- **Appui court sur tuile remplie** → play / stop (idem avant)
- **Appui court sur tuile vide** → scroll vers la page de recherche (dernière page du pager)
- **Appui long sur tuile remplie** → AlertDialog avec options :
  - Déplacer : active le mode déplacement (tap sur tuile cible pour swapper)
  - Supprimer : supprime le favori de ce slot

### Mode déplacement
- Quand actif : la tuile source est mise en évidence (bordure colorée)
- Tapper n'importe quelle tuile (remplie ou vide) effectue le swap/déplacement
- Le mode est annulé après le tap

### Page recherche (intégrée au pager)
- Contenu identique à l'ancien CountryPickerScreen (recherche par nom, tag, pays)
- **Chaque résultat de station** a :
  - Bouton play ▶ (joue immédiatement)
  - Bouton cœur ♡/♥ (ajouter aux favoris / déjà favori)
- Cœur plein → station déjà en favori, bouton désactivé
- Cœur vide → clic → ajoute à la première tuile vide disponible
- Si toutes les pages sont pleines ou inexistantes : crée une nouvelle page

### Ajout d'un favori (depuis StationListScreen également)
- Même comportement : boutons play + cœur sur chaque ligne

### Première ouverture
- 0 pages de favoris → pager affiche uniquement la page recherche (index 0)
- Après ajout du premier favori : 1 page de favoris + page recherche

---

## Corrections de bugs incluses

### BUG-013 — Navigation externe lors du clic sur un pays
Liste des stations affichée inline dans SearchPage (plus de navigation NavGraph). `StationListScreen.kt` supprimé.

### BUG-014 — favoritePageCount = 0 après réinstallation (Android Auto Backup)
HomeViewModel.init recalcule automatiquement le nombre de pages nécessaires depuis la DB.

## Features additionnelles (confirmées après test)

### Bouton play → stop (FEAT-2)
Le bouton ▶ devient ■ quand la station est en cours de lecture. Même comportement dans SearchPage et InlineStationList.

### Cœur plein = supprimer favori (FEAT-3)
Cliquer sur un ♥ plein supprime le favori. La tuile correspondante se vide. `HomeViewModel.removeFavorite()` ajouté.

### Paramètre luminosité configurable (FEAT-1)
Settings → Affichage : toggle `dim_enabled` + slider `dim_brightness` (1–50%). MainActivity lit les prefs au moment du déclenchement.

## Fichiers supprimés
- `FavoritesPickerScreen.kt`
- `StationListScreen.kt` (remplacé par InlineStationList dans CountryPickerScreen.kt)

## Fichiers modifiés
- `HomeViewModel.kt` — favoritePageCount, addBlankPage, addFavoriteToNextAvailableSlot, swapFavorites, removeFavoriteAtPosition, playStation
- `HomeScreen.kt` — pager dynamique, menu, mode déplacement, page recherche intégrée
- `RadioTile.kt` — appui long, isSelectedForMove
- `CountryPickerScreen.kt` — renommé SearchPageContent, sans Scaffold, boutons play+cœur
- `StationListScreen.kt` — boutons play+cœur, plus de slotPosition
- `NavGraph.kt` — suppression routes FavoritesPicker et CountryPicker, HomeViewModel partagé
- `SettingsScreen.kt` — suppression section "Gérer mes favoris"
- `strings.xml` × 5 — nouvelles chaînes

---

## Impact sur carradio-spec.md
- §6.1 Navigation principale
- §6.2 HomeScreen layout
- §6.3 Gestion des favoris
- §12 Structure des fichiers
