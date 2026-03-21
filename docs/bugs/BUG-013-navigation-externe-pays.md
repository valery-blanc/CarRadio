# BUG-013 — Navigation externe lors du clic sur un pays

**Statut :** FIXED
**Découvert lors du test de :** FEAT-012

---

## Symptôme

Lorsqu'on cliquait sur un pays dans la page de recherche, l'app naviguait vers `StationListScreen` comme une destination externe (push sur la pile NavGraph). L'utilisateur perdait la TopAppBar commune et la navigation par swipe gauche/droite du HorizontalPager.

## Reproduction

1. Ouvrir la page recherche (dernière page du pager)
2. Scroller jusqu'à "Recherche par pays"
3. Taper sur un pays
4. → L'écran entier change, plus de barre de titre commune

## Cause racine

`CountryPickerScreen` appelait `onCountrySelected(iso, name)` → NavGraph naviguait via `navController.navigate(Routes.stationList(...))` → remplaçait l'écran principal.

## Fix appliqué

La liste des stations par pays est maintenant affichée **inline** dans `SearchPageContent` via un état local `selectedCountry: Pair<String, String>?`. Quand ce state est non-null, `InlineStationList` s'affiche à la place du contenu de recherche, avec un bouton ← retour. Aucune navigation NavGraph n'est impliquée.

La route `STATION_LIST` et `StationListScreen.kt` ont été supprimés.

## Section spec impactée

§6.5 SearchPage, §6.6 StationListScreen (supprimé)
