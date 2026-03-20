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

## FEAT-009 : Bannière publicitaire AdMob + suppression NowPlayingBar
- [x] `libs.versions.toml` : ajout play-services-ads
- [x] `build.gradle.kts` : ajout dépendance AdMob
- [x] `AndroidManifest.xml` : meta-data App ID AdMob (test ID)
- [x] `CarRadioApplication.kt` : initialisation MobileAds
- [x] `HomeScreen.kt` : suppression NowPlayingBar, ajout AdBanner en bas
- [x] `NowPlayingBar.kt` : suppression du fichier
- [x] `carradio-spec.md` : v1.9, mise à jour §6.2 layout, §12 structure
- [x] Déployé et testé sur appareil
