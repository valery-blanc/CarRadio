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

### Déploiement
- [x] Build réussi (Kotlin 2.0.21 + Hilt 2.51 + AGP 8.7.3)
- [x] Installé sur appareil (2201116PG - Android 13)
- [x] Testé et confirmé fonctionnel par l'utilisateur
