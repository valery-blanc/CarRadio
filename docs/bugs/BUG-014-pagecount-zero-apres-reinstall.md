# BUG-014 — favoritePageCount = 0 malgré des favoris en DB après réinstallation

**Statut :** FIXED
**Découvert lors du test de :** FEAT-012

---

## Symptôme

Après désinstallation + réinstallation de l'app (avec vidage des données), les favoris apparaissaient comme "déjà favoris" (cœur plein) dans la page de recherche, mais aucune page de favoris n'était visible dans le HorizontalPager.

## Reproduction

1. Configurer des favoris dans l'app
2. Vider les données + désinstaller
3. Réinstaller
4. Ouvrir l'app → les cœurs sont pleins mais le pager n'a que la page recherche

## Cause racine

Android **Auto Backup** restaure la base de données Room (SQLite) depuis le cloud lors d'une réinstallation, mais pas forcément les SharedPreferences (selon la version de backup ou si les prefs ne correspondent pas). Résultat : `favorite_page_count = 0` dans les prefs alors que Room contient des favoris à des positions > 0.

## Fix appliqué

Dans `HomeViewModel.init`, après le démarrage, on lit les favoris existants en DB et on recalcule le `favoritePageCount` minimum nécessaire :

```kotlin
init {
    viewModelScope.launch {
        val existing = repository.getFavorites().first()
        if (existing.isNotEmpty()) {
            val neededPages = existing.maxOf { it.position / SLOTS_PER_PAGE } + 1
            if (_favoritePageCount.value < neededPages) {
                saveFavoritePageCount(neededPages)
            }
        }
    }
}
```

## Section spec impactée

§6.2 HomeScreen — Nombre de pages de favoris
