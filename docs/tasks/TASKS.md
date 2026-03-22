# TASKS.md — CarRadio

## FEAT-001 : Implémentation initiale v1.0

### Infrastructure
- [x] Fichiers Gradle (settings, root, app, libs.versions.toml)
- [x] Gradle wrapper (8.14.3, JDK 21 via Android Studio JBR)
- [x] gradle.properties (JAVA_HOME = Android Studio JBR)

### Android base
- [x] AndroidManifest.xml (permissions, service déclaré)
- [x] CarRadioApplication (@HiltAndroidApp)
- [x] MainActivity
- [x] Theme Compose (dark)
- [x] strings.xml, colors.xml, themes.xml

### Couche Data
- [x] DTOs (StationDto, CountryDto)
- [x] RadioBrowserApi (Retrofit)
- [x] RadioBrowserService (résolution DNS + fallback)
- [x] FavoriteStation (Room entity)
- [x] CountryCache (Room entity, TTL 24h)
- [x] FavoriteDao, CountryCacheDao
- [x] AppDatabase
- [x] RadioRepository interface + RadioRepositoryImpl

### Player
- [x] PlayerState enum
- [x] PlayerController (ExoPlayer singleton, lazy init, audio focus)
- [x] RadioPlayerService (MediaSessionService, startForeground immédiat)

### DI (Hilt)
- [x] NetworkModule
- [x] DatabaseModule
- [x] AppModule (binding Repository)

### UI (Compose)
- [x] NavGraph (5 destinations)
- [x] HomeScreen (HorizontalPager 2 pages, grille 2×4)
- [x] RadioTile (remplie / vide, indicateur lecture animé)
- [x] NowPlayingBar
- [x] HomeViewModel
- [x] SettingsScreen (toggles, qualité, à propos)
- [x] SettingsViewModel
- [x] FavoritesPickerScreen (16 slots, dialogue modifier/supprimer)
- [x] CountryPickerScreen (pays recommandés + liste complète, recherche)
- [x] StationListScreen (liste par pays, recherche)
- [x] FavoritesViewModel

### Bugs corrigés
- [x] BUG-001 : ForegroundServiceDidNotStartInTimeException (crash au démarrage)
- [x] BUG-002 : Streams silencieux — HTTP bloqué par Android (network_security_config)
- [x] BUG-003 : Stations manquantes — tri par `votes` remplacé par `clickcount`

### Déploiement
- [x] Build réussi (Kotlin 2.0.21 + Hilt 2.51 + AGP 8.7.3)
- [x] Installé sur appareil (2201116PG - Android 13)
- [x] Testé et confirmé fonctionnel par l'utilisateur

## FEAT-002 : Recherche de stations par nom
- [x] Endpoint `searchStationsByName` dans RadioBrowserApi
- [x] Méthode `searchStationsByName` dans RadioRepository + Impl
- [x] États `nameSearchQuery`, `nameSearchState` dans FavoritesViewModel
- [x] CountryPickerScreen : section "Recherche par nom" avec ImeAction.Search
- [x] NavGraph : callback `onStationDirectlySelected`
- [x] Testé et confirmé par l'utilisateur

## FEAT-003 : Enrichissement résultats recherche par nom
- [x] `StationDto` : ajout `state`, `language`, `tags` (champs nullable)
- [x] `RadioStation` : ajout `subdivision`, `languages`, `tags`
- [x] `StationItem` : affichage enrichi (codec · bitrate · ville · pays · langue)
- [x] Limite recherche par nom : 100 → 30
- [x] Testé et confirmé par l'utilisateur

## FEAT-004 : Recherche par tag avec auto-complétion
- [x] `TagDto` : nouveau DTO
- [x] `RadioBrowserApi` : `getTagSuggestions` (path `/json/tags/{searchTerm}`), `getStationsByTag`
- [x] `RadioRepository` + `RadioRepositoryImpl` : deux nouvelles méthodes
- [x] `FavoritesViewModel` : `tagSearchQuery`, `tagSuggestions`, `tagSearchState` + actions + debounce 300ms + min 3 chars
- [x] `CountryPickerScreen` : section "Recherche par tag" avec ExposedDropdownMenuBox
- [x] Testé et confirmé par l'utilisateur

## FEAT-005 : Icône de l'application
- [x] Icônes générées pour toutes densités (mdpi→xxxhdpi) via ImageMagick
- [x] `AndroidManifest.xml` : `android:icon` et `android:roundIcon` ajoutés
- [x] Testé et confirmé par l'utilisateur

## FEAT-006 : 4 pages de favoris
- [x] `HomeScreen.kt` : pageCount 2→4, slots 16→32, indicateur 4 points
- [x] `FavoritesPickerScreen.kt` : repeat(2)→repeat(4)
- [x] Testé et confirmé par l'utilisateur

## FEAT-007 : Réorganisation des favoris par appui long
- [x] `FavoriteDao.kt` : ajout `updatePosition(uuid, newPosition)`
- [x] `RadioRepository` + `RadioRepositoryImpl` : ajout `swapFavorites(from, to)`
- [x] `FavoritesViewModel` : ajout `swapFavorites(from, to)`
- [x] `FavoritesPickerScreen` : appui long → sélection, tap → swap, poubelle dans TopAppBar, label info
- [x] Testé et confirmé par l'utilisateur

## FEAT-008 : Minuteur sommeil (Sleep Timer)
- [x] `docs/specs/FEAT-008-sleep-timer.md` : spec créée
- [x] `SleepTimerViewModel.kt` : countdown, fade volume 30s, killProcess à l'expiration
- [x] `SleepTimerScreen.kt` : 3 roues drum picker (heures/min/sec) + boutons Démarrer/Annuler
- [x] `NavGraph.kt` : route `SLEEP_TIMER`, SleepTimerViewModel partagé (activity-scoped)
- [x] `HomeScreen.kt` : icône sablier, affichage Bedtime + compte à rebours quand actif
- [x] `carradio-spec.md` : v1.8, §6.1, §6.2, §6.7, §12 mis à jour
- [x] Déployé et testé sur appareil

## FEAT-010 : Mise en veille de la luminosité après 30s d'inactivité
- [x] `MainActivity.kt` : Handler 30s + dimScreen/restoreBrightness + onUserInteraction
- [x] `carradio-spec.md` : v1.10, §9 comportement en voiture mis à jour
- [x] Déployé et testé sur appareil

## FEAT-009 : Bannière publicitaire AdMob + suppression NowPlayingBar
- [x] `libs.versions.toml` : ajout play-services-ads
- [x] `build.gradle.kts` : ajout dépendance AdMob
- [x] `AndroidManifest.xml` : meta-data App ID AdMob (test ID)
- [x] `CarRadioApplication.kt` : initialisation MobileAds
- [x] `HomeScreen.kt` : suppression NowPlayingBar, ajout AdBanner en bas
- [x] `NowPlayingBar.kt` : suppression du fichier
- [x] `carradio-spec.md` : v1.9, mise à jour §6.2 layout, §12 structure
- [x] Déployé et testé sur appareil

## FEAT-012 : Refonte interface principale

### Documentation
- [x] `docs/specs/FEAT-012-refonte-interface.md` créé
- [x] `docs/specs/carradio-spec.md` mis à jour (v1.12)

### Strings (i18n)
- [x] `values/strings.xml` — nouvelles chaînes
- [x] `values-fr/strings.xml`
- [x] `values-es/strings.xml`
- [x] `values-pt/strings.xml`
- [x] `values-de/strings.xml`

### Code
- [x] `HomeViewModel.kt` — favoritePageCount, addBlankPage, addFavoriteToNextAvailableSlot, swapFavorites, removeFavoriteAtPosition, playStation
- [x] `RadioTile.kt` — appui long + isSelectedForMove
- [x] `CountryPickerScreen.kt` → SearchPageContent (sans Scaffold, boutons play+cœur)
- [x] `StationListScreen.kt` — boutons play+cœur, suppression slotPosition
- [x] `HomeScreen.kt` — pager dynamique, menu, mode déplacement, SearchPage intégrée
- [x] `NavGraph.kt` — HomeViewModel partagé, routes simplifiées
- [x] `SettingsScreen.kt` — suppression section "Gérer mes favoris"
- [x] `FavoritesPickerScreen.kt` — supprimé

### Bugs découverts lors des tests
- [x] BUG-013 : navigation externe pays → liste inline dans SearchPage
- [x] BUG-014 : favoritePageCount = 0 après reinstall (Android Auto Backup)

### Features additionnelles (confirmées OK)
- [x] FEAT-2 : bouton play → stop quand radio en lecture
- [x] FEAT-3 : cœur plein cliquable → supprime le favori
- [x] FEAT-1 : paramètre luminosité configurable (toggle + slider)

### Déploiement
- [x] Build et déploiement sur appareil (APK debug)
- [x] Test et confirmation utilisateur
- [x] Commit + push GitHub
- [x] Build release bundle (Play Store) — versionCode 3, versionName 1.1

## BUG-015 : Clavier virtuel intempestif sur pages favoris
- [x] Fix : LocalFocusManager.clearFocus() dans LaunchedEffect(pagerState.currentPage)
- [x] Déployé et confirmé par l'utilisateur

## BUG-016 : Disparition complète du son après plusieurs utilisations
- [x] `RadioPlayerService.onDestroy()` : suppression de `player.release()` (player appartient à PlayerController)
- [x] `PlayerController.stop()` : ajout de `stopService()` pour arrêter le service proprement
- [x] `docs/bugs/BUG-016-audio-focus-leak.md` créé
- [ ] Déployé et confirmé par l'utilisateur

## FEAT-013 : Navigation rapide entre recherche et favoris
- [x] `docs/specs/FEAT-013-navigation-rapide.md` créé
- [x] `HomeScreen.kt` : page recherche déplacée en index 0, favoris aux indices 1..N
- [x] `HomeScreen.kt` : icône Home sur page recherche, icône Search sur pages favoris
- [x] `HomeScreen.kt` : initialPage = 1 si favoris existent, LaunchedEffect pour BUG-014
- [x] `HomeScreen.kt` : BUG-015 condition mise à jour (currentPage > 0)
- [x] Strings × 5 langues : go_to_home, go_to_search
- [ ] Déployé et confirmé par l'utilisateur

## REVIEW-2026-03-22 : Correctifs revue de code (37 issues)

### CRITICAL (3 fixes)
- [x] 1.1 `NetworkModule.kt` : suppression `runBlocking` (risque ANR au démarrage)
- [x] 1.1 `RadioBrowserService.kt` : résolution DNS dans thread daemon background + intercepteur hôte dynamique
- [x] 1.2 `DatabaseModule.kt` : ajout `fallbackToDestructiveMigration()` (évite crash sur migration manquante)
- [x] 1.3 `SleepTimerViewModel.kt` : `Process.killProcess` → `_shouldFinishApp` StateFlow observé par MainActivity → `finishAffinity()`

### HIGH (8 fixes)
- [x] 2.1 `FavoriteDao.kt` : méthode `swapFavorites` avec `@Transaction` (atomicité du swap)
- [x] 2.1 `RadioRepositoryImpl.kt` : délégation au DAO pour `swapFavorites`
- [x] 2.2 `RadioBrowserService.kt` : timeouts OkHttp (connect 10s, read 15s, write 10s)
- [x] 2.4 `PlayerController.kt` : `onEvents()` à la place de deux callbacks séparés (élimine race condition)
- [x] 2.5 `PlayerController.kt` : try-catch sur les opérations player (play, pause, resume, stop)
- [x] 2.6 `HomeScreen.kt` : `onRelease = { it.destroy() }` sur AdView (évite memory leak)
- [x] 2.8/2.9 `RadioPlayerService.kt` : suppression `startForeground` manuel dans `onStartCommand`, passage à `START_NOT_STICKY`
  - [!] RÉVERT PARTIEL : `startForeground()` restauré (cf. BUG-017 — ForegroundServiceDidNotStartInTimeException)
- [x] 2.10 `HomeViewModel.kt` : try-catch sur `notifyClick`, `playerController` privé
- [x] 2.11 `CountryPickerScreen.kt` : suppression du paramètre par défaut `hiltViewModel()` dans `SearchPageContent`

### MEDIUM (8 fixes)
- [x] 3.1 `MainActivity.kt` : `FLAG_KEEP_SCREEN_ON` lié à l'état du player (via collectAsState)
- [x] 3.2 `MainActivity.kt` : instance SharedPreferences mise en cache (by lazy)
- [x] 3.3 `HomeViewModel.kt` : `withTimeoutOrNull(5_000)` sur `getFavorites().first()` dans init
- [x] 3.4 `SleepTimerViewModel.kt` : drift corrigé via `SystemClock.elapsedRealtime()`
- [x] 3.5 `SleepTimerViewModel.kt` : `onCleared()` appelle `cancelTimer()` (reset volume + annulation job)
- [x] 3.6 `CountryCacheDao.kt` : méthode `replaceAll` avec `@Transaction`
- [x] 3.6 `RadioRepositoryImpl.kt` : utilisation de `countryCacheDao.replaceAll()`
- [x] 3.7 `RadioBrowserService.kt` : logging conditionnel `BuildConfig.DEBUG`
- [x] 3.8 `CarRadioApplication.kt` : `MobileAds.initialize` dans thread background
- [x] 3.9 `proguard-rules.pro` : règles Coil ajoutées
- [x] 3.10 `HomeScreen.kt` : guard sur `slots.subList()` (coerceAtMost)
- [x] 3.11 `HomeScreen.kt` : multiplicateur pager 10 000 → 100
- [x] 3.12 `AndroidManifest.xml` : suppression `usesCleartextTraffic` redondant

### LOW (5 fixes)
- [x] 4.1 `RadioRepositoryImpl.kt` : `CancellationException` re-thrown dans `notifyClick`
- [x] 4.2 `HomeViewModel.kt` : `playerController` rendu privé
- [x] 4.3 `PlayerController.kt` : champ nullable `playerInstance` + check `isPlayerInitialized` dans `stop()`
- [x] 4.5 `HomeScreen.kt` : `adSize` dans `remember {}` (évite recalcul à chaque recomposition)
- [x] 4.6 `MainActivity.kt` : constante `MIN_BRIGHTNESS` utilisée dans `dimScreen()`
- [x] 4.7 `CarRadioApplication.kt` : completion listener sur `MobileAds.initialize`
- [x] 4.8 `CountryPickerScreen.kt` : filtrage pays dans `remember {}` (hissé avant LazyColumn)

### Déploiement
- [x] Déployé sur appareil (REVIEW fixes)
- [ ] Testé et confirmé par l'utilisateur
- [ ] Commit

## BUG-017 : ForegroundServiceDidNotStartInTimeException (BUG-001 réintroduit par REVIEW)

- [x] Diagnostic via logcat : `ForegroundServiceDidNotStartInTimeException` confirmé
- [x] `RadioPlayerService.kt` : restauration `startForeground(NOTIFICATION_ID, buildInitialNotification())` dans `onStartCommand()`
- [ ] Déployé et confirmé par l'utilisateur
- [ ] Commit

## BUG-018 : Navigation quitte la page recherche lors de la création d'une nouvelle page favoris

- Cause racine : quand `totalPages` change (N→N+1), `currentRealPage = pagerState.currentPage % totalPages` change car le modulo change — même sans scroll de l'utilisateur
- [x] `HomeScreen.kt` : `LaunchedEffect(favoritePageCount)` ré-ancre le pager sur la même page visuelle via `oldRealPage = currentPage % oldTotalPages`, clampé à `favoritePageCount` (gère aussi la suppression de page)
- [ ] Déployé et confirmé par l'utilisateur
- [ ] Commit

## FEAT-014 : Suppression d'une page de favoris

- [x] `FavoriteDao.kt` : `deleteInRange`, `shiftPositionsDown`, `removePage` @Transaction
- [x] `RadioRepository.kt` + `RadioRepositoryImpl.kt` : `removeFavoritePage(pageStart, slotsPerPage)`
- [x] `HomeViewModel.kt` : `removeFavoritePage(realPageIndex)` (supprime favoris + compacte positions + décrémente count)
- [x] `HomeScreen.kt` : menu item "Remove this favorites page" (visible sur pages favoris) + dialog de confirmation
- [x] Strings × 5 langues : `remove_favorite_page`, `remove_page_confirm`, `confirm`
- [ ] Déployé et confirmé par l'utilisateur
- [ ] Commit
