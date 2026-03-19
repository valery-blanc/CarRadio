# CarRadio — Specification Technique

**Version :** 1.7 (FEAT-002/003/004/005/006/007)
**Date :** 2026-03-20
**Plateforme cible :** Android (API 26+, Android 8.0 Oreo minimum)
**Langage :** Kotlin
**Architecture :** MVVM + Repository pattern

---

## Table des matières

1. [Vue d'ensemble](#1-vue-densemble)
2. [Stack technique](#2-stack-technique)
3. [Source de données — Radio Browser API](#3-source-de-données--radio-browser-api)
4. [Modèles de données](#4-modèles-de-données)
5. [Architecture de l'application](#5-architecture-de-lapplication)
6. [Écrans et navigation](#6-écrans-et-navigation)
7. [Lecture audio](#7-lecture-audio)
8. [Persistance locale](#8-persistance-locale)
9. [Comportement en voiture](#9-comportement-en-voiture)
10. [Gestion des erreurs](#10-gestion-des-erreurs)
11. [Permissions Android](#11-permissions-android)
12. [Structure du projet](#12-structure-du-projet)
13. [Évolutions futures](#13-évolutions-futures)

---

## 1. Vue d'ensemble

CarRadio est une application Android de streaming radio internet conçue pour une utilisation en voiture. L'interface est volontairement simple, avec de grandes tuiles tactiles facilement actionnables en conduisant.

### Principe de fonctionnement

- L'utilisateur configure jusqu'à **32 stations favorites**, réparties sur **4 pages** de **8 tuiles** chacune (2 colonnes × 4 lignes).
- On passe d'une page à l'autre par un **swipe horizontal**.
- Un tap sur une tuile lance la lecture de la station. Un second tap sur la même tuile la met en pause.
- Les stations favorites sont choisies via un **écran de paramètres** qui exploite la **Radio Browser API** (base de données mondiale, gratuite et open source).

---

## 2. Stack technique

| Composant | Choix |
|---|---|
| Langage | Kotlin |
| UI | Jetpack Compose |
| Navigation | Navigation Compose |
| Lecteur audio | AndroidX Media3 (ExoPlayer) |
| Requêtes réseau | Retrofit 2 + OkHttp |
| Sérialisation JSON | Gson |
| Persistance | Room Database |
| Injection de dépendances | Hilt |
| Images (logos) | Coil |
| Coroutines | Kotlinx Coroutines + Flow |
| Architecture | MVVM + Repository |

### Dépendances `build.gradle` (app)

```kotlin
// Media3 / ExoPlayer
implementation("androidx.media3:media3-exoplayer:1.3.1")
implementation("androidx.media3:media3-ui:1.3.1")
implementation("androidx.media3:media3-session:1.3.1")

// Retrofit
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Hilt
implementation("com.google.dagger:hilt-android:2.51")
ksp("com.google.dagger:hilt-compiler:2.51")

// Coil
implementation("io.coil-kt:coil-compose:2.6.0")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2024.04.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")
implementation("androidx.compose.foundation:foundation")
implementation("androidx.navigation:navigation-compose:2.7.7")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
```

---

## 3. Source de données — Radio Browser API

L'application utilise **[radio-browser.info](https://www.radio-browser.info/)**, une base de données collaborative, gratuite, open source, sans clé API.

### Résolution du serveur

Radio Browser expose plusieurs serveurs miroirs. Il faut en résoudre un dynamiquement :

```kotlin
// Résoudre un serveur via DNS au démarrage
// DNS SRV : _api._tcp.radio-browser.info
// Ou simplement utiliser : https://de1.api.radio-browser.info
val BASE_URL = "https://de1.api.radio-browser.info"
```

En production, résoudre la liste via DNS et choisir un serveur aléatoirement. Basculer sur le suivant en cas d'échec.

### Endpoints utilisés

#### Lister les pays
```
GET /json/countries?order=name&reverse=false&hidebroken=true
```
Retourne : `[{ "name": "France", "iso_3166_1": "FR", "stationcount": 512 }, ...]`

#### Rechercher des stations par pays (endpoint principal)
```
GET /json/stations/search?countrycode=FR&order=clickcount&reverse=true&hidebroken=true&limit=200
```
Utiliser le code ISO (`countrycode`) plutôt que le nom du pays — plus fiable (pas de problèmes d'accents ou de casse).
Trier par `clickcount` (nombre de lectures) et non par `votes` — reflète mieux la popularité réelle et inclut davantage de stations connues dans le top 200.

#### Rechercher par nom de pays (alternative)
```
GET /json/stations/bycountryexact/{countryName}?order=clickcount&reverse=true&hidebroken=true&limit=200
```

#### Rechercher des stations par nom
```
GET /json/stations/search?name=XXXX&order=clickcount&reverse=true&hidebroken=true&limit=30
```
Limite à 30 résultats. Utilisé dans `CountryPickerScreen` section "Recherche par nom".

#### Suggestions de tags (auto-complétion)
```
GET /json/tags/{searchTerm}?order=stationcount&reverse=true&hidebroken=true&limit=10
```
Le filtre est un **path parameter** (pas un query param `name`). Déclenché après 3 caractères minimum, avec debounce 300ms.

#### Stations par tag
```
GET /json/stations/search?tag=TAG&order=clickcount&reverse=true&hidebroken=true&limit=50
```

#### Notifier un clic (bonne pratique API)
```
GET /json/url/{stationuuid}
```
À appeler à chaque lancement de stream. Retourne l'URL réelle du stream (suit les redirections).

### Champs utiles d'une station

```json
{
  "stationuuid": "960e57c5-0601-11e8-ae97-52543be04c81",
  "name": "France Inter",
  "url": "http://direct.franceinter.fr/live/franceinter-midfi.mp3",
  "url_resolved": "http://direct.franceinter.fr/live/franceinter-midfi.mp3",
  "favicon": "https://www.radiofrance.fr/favicon.ico",
  "tags": "news,talk,france",
  "country": "France",
  "countrycode": "FR",
  "state": "Île-de-France",
  "language": "french",
  "codec": "MP3",
  "bitrate": 128,
  "hls": 0,
  "lastcheckok": 1,
  "votes": 1500
}
```

Note : `state` (subdivision/région) et `language` sont parfois absents — traiter comme chaîne vide.

### Headers HTTP requis

```
User-Agent: CarRadio/1.0
```

---

## 4. Modèles de données

### `RadioStation` (modèle domaine)

```kotlin
data class RadioStation(
    val uuid: String,           // stationuuid depuis Radio Browser
    val name: String,
    val streamUrl: String,      // url_resolved ou url
    val faviconUrl: String?,
    val country: String,
    val countryCode: String,
    val subdivision: String = "",   // champ "state" de l'API (région/état)
    val languages: String = "",     // champ "language" de l'API
    val tags: String = "",          // tags séparés par virgules
    val codec: String,          // "MP3", "AAC", "HLS"
    val bitrate: Int,
    val isHls: Boolean,
    val votes: Int
)
```

### `FavoriteStation` (entité Room)

```kotlin
@Entity(tableName = "favorites")
data class FavoriteStation(
    @PrimaryKey val uuid: String,
    val name: String,
    val streamUrl: String,
    val faviconUrl: String?,
    val country: String,
    val countryCode: String,
    val codec: String,
    val bitrate: Int,
    val isHls: Boolean,
    val position: Int,          // 0-31 : position dans la grille (4 pages × 8 slots)
    val addedAt: Long = System.currentTimeMillis()
)
```

La **position** encode l'emplacement dans la grille :
- Positions 0–7 → Page 1
- Positions 8–15 → Page 2
- Positions 16–23 → Page 3
- Positions 24–31 → Page 4
- Maximum : **32 favoris** au total (4 pages × 8 tuiles)

### `Country` (modèle API)

```kotlin
data class Country(
    val name: String,
    val iso: String,            // code ISO 3166-1 alpha-2
    val stationCount: Int
)
```

### `TagDto` (DTO auto-complétion tags)

```kotlin
data class TagDto(
    @SerializedName("name") val name: String,
    @SerializedName("stationcount") val stationCount: Int
)
```

---

## 5. Architecture de l'application

```
app/
├── data/
│   ├── api/
│   │   ├── RadioBrowserApi.kt          // Interface Retrofit
│   │   ├── RadioBrowserService.kt      // Résolution serveur + client OkHttp
│   │   └── dto/                        // Data Transfer Objects (réponses JSON)
│   ├── db/
│   │   ├── AppDatabase.kt              // Room database
│   │   ├── FavoriteDao.kt              // CRUD favoris
│   │   └── FavoriteStation.kt          // Entité Room
│   └── repository/
│       ├── RadioRepository.kt          // Interface
│       └── RadioRepositoryImpl.kt      // Implémentation
├── domain/
│   └── model/
│       ├── RadioStation.kt
│       └── Country.kt
├── ui/
│   ├── MainActivity.kt
│   ├── navigation/
│   │   └── NavGraph.kt
│   ├── home/
│   │   ├── HomeScreen.kt               // Écran principal (4 pages swipables)
│   │   ├── HomeViewModel.kt
│   │   ├── RadioTile.kt                // Composant tuile
│   │   └── NowPlayingBar.kt            // Barre "en cours de lecture"
│   ├── settings/
│   │   ├── SettingsScreen.kt           // Menu paramètres
│   │   └── SettingsViewModel.kt
│   └── favorites/
│       ├── FavoritesPickerScreen.kt    // Gestion des favoris (réorganisation)
│       ├── CountryPickerScreen.kt      // Recherche par nom / tag / pays
│       ├── StationListScreen.kt        // Liste des stations d'un pays
│       └── FavoritesViewModel.kt
├── player/
│   ├── RadioPlayerService.kt           // MediaSessionService (background)
│   └── PlayerController.kt            // Abstraction ExoPlayer
└── di/
    ├── AppModule.kt                    // Hilt modules
    ├── NetworkModule.kt
    └── DatabaseModule.kt
```

---

## 6. Écrans et navigation

### 6.1 Graphe de navigation

```
HomeScreen
    └── (icône ⚙️) → SettingsScreen
                        └── "Gérer mes favoris" → FavoritesPickerScreen
                                                      ├── (slot vide) → CountryPickerScreen
                                                      │                     ├── (résultat nom/tag) → retour direct FavoritesPickerScreen
                                                      │                     └── (pays) → StationListScreen → retour FavoritesPickerScreen
                                                      └── (appui long slot rempli) → réorganisation in-place
```

---

### 6.2 HomeScreen — Écran principal

**Description :** Écran plein écran avec un `HorizontalPager` (4 pages). Chaque page affiche une grille 2 × 4 de tuiles radio.

**Layout :**

```
┌─────────────────────────────────┐
│  CarRadio          [⚙️]  [■]    │  ← TopAppBar
├─────────────────────────────────┤
│                                 │
│  ┌──────────┐  ┌──────────┐    │
│  │  [logo]  │  │  [logo]  │    │
│  │France    │  │RTL       │    │
│  │Inter     │  │          │    │
│  └──────────┘  └──────────┘    │
│         ...           ...       │
│                                 │
│       ● ● ○ ○  ← indicateur    │
├─────────────────────────────────┤
│  ▶ France Inter — En direct     │  ← NowPlayingBar
└─────────────────────────────────┘
```

**Comportement des tuiles :**

- Tuile **remplie** (favori configuré) :
  - Affiche le logo de la radio (chargé avec Coil, placeholder si absent)
  - Affiche le nom de la radio sous le logo
  - Fond coloré légèrement différencié quand la station est en cours de lecture (état `PLAYING`)
  - **Tap** → lance la lecture si autre station active, ou met en pause/reprend si c'est la station active
  - Indicateur visuel (icône ▶ ou animation de barres EQ) sur la tuile active
- Tuile **vide** (aucun favori à cette position) :
  - Affiche une icône `+` avec le texte "Ajouter"
  - **Tap** → navigue directement vers `FavoritesPickerScreen`

**TopAppBar :**
- Titre : "CarRadio"
- Icône `⚙️` → navigue vers `SettingsScreen`
- Icône ■ (stop) → arrête la lecture en cours (visible uniquement si une station est chargée)

**Indicateur de page :** Quatre points (● ● ○ ○) centrés en bas du pager.

**NowPlayingBar :** Barre fixe en bas de l'écran, visible uniquement quand une station est chargée. Affiche :
- Icône play/pause
- Nom de la station
- Indicateur de buffering si applicable

---

### 6.3 SettingsScreen — Paramètres

**Description :** Écran simple avec liste de paramètres.

**Contenu :**

- Section **"Mes favoris"**
  - Entrée : "Gérer mes favoris" → `FavoritesPickerScreen`
  - Sous-texte : "X/32 stations configurées"

- Section **"Lecture"**
  - Toggle : "Continuer la lecture quand l'app est en arrière-plan" (défaut : activé)
  - Sélecteur : "Qualité préférée" → `Haute (192k+)` / `Normale (128k)` / `Basse (<128k)`

- Section **"Affichage"**
  - Toggle : "Écran toujours allumé" (défaut : activé) — contrôle `FLAG_KEEP_SCREEN_ON`

- Section **"À propos"**
  - Version de l'app
  - Mention : "Données stations : radio-browser.info"

---

### 6.4 FavoritesPickerScreen — Gestion des favoris

**Description :** Écran de gestion permettant de visualiser les 32 slots (4 pages × 8), modifier chaque favori, et réorganiser leur ordre.

**Layout :**

```
┌─────────────────────────────────────┐
│  ← Mes favoris              [🗑️]    │  ← poubelle visible si tuile sélectionnée
├─────────────────────────────────────┤
│  Appui long pour déplacer/supprimer │  ← label d'aide
│  Page 1                             │
│  ┌──────────┐  ┌──────────┐        │
│  │ France   │  │  RTL     │        │
│  │ Inter    │  │          │        │
│  └──────────┘  └──────────┘        │
│  ┌──────────┐  ┌──────────┐        │
│  │  Europe 1│  │  [+]     │        │
│  └──────────┘  └──────────┘        │
│  ...                                │
│  Page 2                             │
│  ...                                │
│  Page 3 / Page 4                    │
└─────────────────────────────────────┘
```

**Interactions :**

- **Tap sur un slot vide** → navigue vers `CountryPickerScreen` avec le numéro de slot
- **Tap sur un slot rempli** (sans sélection en cours) → dialogue "Modifier" / "Supprimer"
- **Appui long sur un slot rempli** → slot "sélectionné" (bordure couleur primaire, légèrement agrandi, fond primaryContainer)
- **Tap sur un autre slot** (quand un slot est sélectionné) → les deux slots échangent leurs positions (`swapFavorites`). Fonctionne entre pages.
- **Tap sur le slot sélectionné** → désélection
- **Icône poubelle dans TopAppBar** (visible uniquement si slot sélectionné) → supprime le favori du slot sélectionné

---

### 6.5 CountryPickerScreen — Recherche de station

**Description :** Écran de recherche de station en trois sections, dans un `LazyColumn` scrollable.

#### Section 1 — Recherche par nom
- Champ texte avec `ImeAction.Search`
- Sur appui Retour clavier → requête `searchStationsByName(query)`, limite 30 résultats
- Résultats affichés inline : nom, codec · bitrate · région · pays · langue
- Tap sur une station → ajout au slot + retour direct à `FavoritesPickerScreen`
- Bouton "Effacer" pour réinitialiser

#### Section 2 — Recherche par tag
- Champ texte avec auto-complétion (`ExposedDropdownMenuBox`)
- Auto-complétion déclenchée après **3 caractères minimum**, avec debounce 300ms
- Requête : `GET /json/tags/{searchTerm}?order=stationcount&reverse=true&limit=10`
- Sélection d'un tag dans le dropdown → ou appui Retour clavier → requête `getStationsByTag(tag)`, limite 50 résultats
- Résultats affichés : nom, pays, tags (3 premiers)
- Tap sur une station → ajout au slot + retour direct à `FavoritesPickerScreen`

#### Section 3 — Recherche par pays
- Champ texte pour filtrer la liste des pays
- **Pays mis en avant** (hors filtre) : France, Suisse, Belgique, Canada
- Liste complète alphabétique ensuite
- Chaque entrée : nom du pays + nombre de stations
- Cache 24h (Room `countries_cache`)
- Tap sur un pays → navigue vers `StationListScreen`

---

### 6.6 StationListScreen — Choix d'une station par pays

**Description :** Liste des stations d'un pays, triée par popularité.

**Comportement :**
- Affiche le nom du pays dans la TopAppBar (bouton retour)
- Requête : `GET /json/stations/search?countrycode=XX&order=clickcount&reverse=true&hidebroken=true&limit=200`
- Barre de recherche pour filtrer par nom
- Chaque entrée affiche :
  - Logo de la station (Coil, 48dp)
  - Nom de la station
  - Codec · bitrate · région (`state`) · pays · langue (`language`) en supporting text (champs vides omis)
- **Tap sur une station** → ajoute au slot sélectionné, retourne à `FavoritesPickerScreen`

---

## 7. Lecture audio

### 7.1 RadioPlayerService

Utiliser `MediaSessionService` de Media3 pour la lecture en arrière-plan.

```kotlin
@AndroidEntryPoint
class RadioPlayerService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }
}
```

### 7.2 Lancement d'un stream

```kotlin
fun play(station: RadioStation) {
    // 1. Notifier Radio Browser (comptage de clics)
    repository.notifyClick(station.uuid)

    // 2. Construire le MediaItem
    val mediaItem = MediaItem.Builder()
        .setUri(station.streamUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(station.name)
                .setArtworkUri(station.faviconUrl?.toUri())
                .build()
        ).build()

    // 3. Lancer la lecture
    player.setMediaItem(mediaItem)
    player.prepare()
    player.play()
}
```

### 7.3 Gestion des formats

| Format | Détection | Traitement ExoPlayer |
|---|---|---|
| MP3 direct | URL `.mp3` ou codec `MP3` | Natif |
| AAC / AAC+ | codec `AAC` | Natif |
| HLS | `hls=1` ou URL `.m3u8` | Natif via `DefaultHlsExtractorFactory` |
| M3U / PLS playlist | URL `.m3u` ou `.pls` | Résoudre l'URL réelle avant lecture |

Pour les playlists M3U/PLS : parser simple intégré — télécharger le fichier texte, extraire la première URL de stream valide (ligne ne commençant pas par `#` pour M3U, valeur `File1=` pour PLS), puis passer cette URL à ExoPlayer.

### 7.4 Notification média

Media3 génère automatiquement une notification avec :
- Nom de la station
- Logo (favicon)
- Boutons play/pause et stop
- Contrôle depuis l'écran de verrouillage et Bluetooth voiture (AVRCP)

### 7.5 États du player

```kotlin
enum class PlayerState {
    IDLE,       // Aucune station chargée
    LOADING,    // Buffering en cours
    PLAYING,    // Lecture active
    PAUSED,     // En pause
    ERROR       // Erreur de connexion
}
```

---

## 8. Persistance locale

### Room Database — `AppDatabase`

```kotlin
@Database(
    entities = [FavoriteStation::class, CountryCache::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun countryCacheDao(): CountryCacheDao
}
```

### `FavoriteDao`

```kotlin
@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY position ASC")
    fun getAllFavorites(): Flow<List<FavoriteStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(station: FavoriteStation)

    @Query("DELETE FROM favorites WHERE uuid = :uuid")
    suspend fun deleteFavorite(uuid: String)

    @Query("DELETE FROM favorites WHERE position = :position")
    suspend fun deleteAtPosition(position: Int)

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun count(): Int

    @Query("SELECT * FROM favorites WHERE position = :position LIMIT 1")
    suspend fun getAtPosition(position: Int): FavoriteStation?

    @Query("UPDATE favorites SET position = :newPosition WHERE uuid = :uuid")
    suspend fun updatePosition(uuid: String, newPosition: Int)
}
```

### Cache pays

Stocker la liste des pays dans une table Room `countries_cache` avec un timestamp `lastFetchedAt`. Invalider après 24h.

```kotlin
@Entity(tableName = "countries_cache")
data class CountryCache(
    @PrimaryKey val iso: String,
    val name: String,
    val stationCount: Int,
    val lastFetchedAt: Long
)
```

---

## 9. Comportement en voiture

### Écran toujours allumé

```kotlin
// Dans MainActivity
window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
```

Ajouter un toggle dans les paramètres pour désactiver ce comportement.

### Taille des tuiles

Les tuiles doivent être suffisamment grandes pour être actionnables sans regarder précisément. Sur un écran 5" en portrait :
- Hauteur de tuile cible : **≥ 100dp**
- Le layout 2×4 doit occuper tout l'espace disponible entre la TopAppBar et la NowPlayingBar
- Utiliser `fillMaxSize()` et `weight(1f)` pour que les tuiles s'adaptent à la taille de l'écran

### Audio Focus

Gérer l'audio focus Android pour :
- Mettre en pause quand un appel téléphonique arrive
- Baisser le volume (ducking) quand une notification audio apparaît
- Reprendre après la fin de l'appel

```kotlin
ExoPlayer.Builder(context)
    .setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build(),
        /* handleAudioFocus = */ true
    )
    .build()
```

---

## 10. Gestion des erreurs

| Erreur | Comportement |
|---|---|
| Pas de connexion internet | Snackbar "Pas de connexion réseau". Tuile active affiche un état d'erreur. |
| Stream indisponible (timeout) | Retry automatique 2× avec délai. Puis snackbar "Station indisponible". |
| Logo non chargeable | Afficher un placeholder avec les initiales de la station |
| API Radio Browser indisponible | Essayer le prochain serveur miroir. Si tous échouent : message d'erreur. |
| Liste de pays vide | Afficher un message et un bouton "Réessayer" |

---

## 11. Permissions Android

### `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
```

Attributs application :
- `android:icon="@mipmap/ic_launcher"`
- `android:roundIcon="@mipmap/ic_launcher_round"`
- `android:networkSecurityConfig="@xml/network_security_config"` — permet HTTP cleartext (streams radio)
- `android:usesCleartextTraffic="true"`

### Déclaration du service

```xml
<service
    android:name=".player.RadioPlayerService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="true">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
    </intent-filter>
</service>
```

---

## 12. Structure du projet

```
CarRadio/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/carradio/
│   │   │   ├── CarRadioApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   │   ├── RadioBrowserApi.kt
│   │   │   │   │   ├── RadioBrowserService.kt
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── StationDto.kt
│   │   │   │   │       ├── CountryDto.kt
│   │   │   │   │       └── TagDto.kt
│   │   │   │   ├── db/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── FavoriteDao.kt
│   │   │   │   │   ├── FavoriteStation.kt
│   │   │   │   │   ├── CountryCacheDao.kt
│   │   │   │   │   └── CountryCache.kt
│   │   │   │   └── repository/
│   │   │   │       ├── RadioRepository.kt
│   │   │   │       └── RadioRepositoryImpl.kt
│   │   │   ├── domain/
│   │   │   │   └── model/
│   │   │   │       ├── RadioStation.kt
│   │   │   │       └── Country.kt
│   │   │   ├── ui/
│   │   │   │   ├── navigation/NavGraph.kt
│   │   │   │   ├── home/
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── HomeViewModel.kt
│   │   │   │   │   ├── RadioTile.kt
│   │   │   │   │   └── NowPlayingBar.kt
│   │   │   │   ├── settings/
│   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   └── SettingsViewModel.kt
│   │   │   │   └── favorites/
│   │   │   │       ├── FavoritesPickerScreen.kt
│   │   │   │       ├── CountryPickerScreen.kt
│   │   │   │       ├── StationListScreen.kt
│   │   │   │       └── FavoritesViewModel.kt
│   │   │   ├── player/
│   │   │   │   ├── RadioPlayerService.kt
│   │   │   │   └── PlayerController.kt
│   │   │   └── di/
│   │   │       ├── AppModule.kt
│   │   │       ├── NetworkModule.kt
│   │   │       └── DatabaseModule.kt
│   │   └── res/
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   ├── colors.xml
│   │       │   └── themes.xml
│   │       ├── drawable/
│   │       ├── xml/
│   │       │   └── network_security_config.xml
│   │       ├── mipmap-mdpi/ic_launcher.png + ic_launcher_round.png
│   │       ├── mipmap-hdpi/
│   │       ├── mipmap-xhdpi/
│   │       ├── mipmap-xxhdpi/
│   │       └── mipmap-xxxhdpi/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/libs.versions.toml
├── gradle.properties               (JAVA_HOME = Android Studio JBR)
└── docs/
    ├── specs/carradio-spec.md      (ce fichier)
    ├── specs/FEAT-*.md
    ├── bugs/BUG-*.md
    └── tasks/TASKS.md
```

---

## 13. Évolutions futures

Ces fonctionnalités sont hors scope v1 mais l'architecture doit les permettre sans refonte majeure.

- **Android Auto** : Le `MediaSessionService` est déjà compatible. Ajouter `androidx.car.app` et une `CarAppService`.
- **Widget écran d'accueil** : Widget 4×2 avec play/pause et nom de la station active.
- **Égaliseur** : Via `ExoPlayer` audio effects.
- **Thème sombre / clair** : Déjà prévu via Material3.
- **Export/import des favoris** : Sérialisation JSON des favoris pour backup.
- **Historique de lecture** : Table `history` dans Room.

---

## 14. Notes d'implémentation

### Règles techniques issues des bugs

**BUG-001 — Service foreground**
Ne jamais appeler `startForegroundService()` de façon préventive. Appeler uniquement au moment où la lecture est réellement déclenchée (dans `PlayerController.play()`). `RadioPlayerService.onStartCommand()` doit appeler `startForeground()` immédiatement.

**BUG-002 — HTTP cleartext**
Les streams Radio Browser utilisent fréquemment `http://`. Toujours déclarer `android:usesCleartextTraffic="true"` et un `network_security_config.xml` avec `cleartextTrafficPermitted="true"`.

**BUG-003 — Tri des stations**
Utiliser `order=clickcount` (pas `order=votes`). Le tri par votes exclut des stations populaires qui ont peu de votes mais beaucoup de lectures effectives.

**FEAT-003 — Champs API nullable**
Les champs `state`, `language`, `tags` de l'API Radio Browser sont souvent absents du JSON. Toujours les déclarer `String?` dans le DTO et utiliser `?: ""` dans `toDomain()`. Les champs `String = ""` dans un `data class` Kotlin ne sont PAS respectés par Gson lors de la désérialisation — Gson met `null` si le champ est absent.

**FEAT-004 — Tags API endpoint**
Le filtre de tags utilise un **path parameter** : `/json/tags/{searchTerm}` et non un query param `?name=`. Un query param `name` est ignoré par l'API et retourne le top global.

### Architecture PlayerController
`PlayerController` est un singleton Hilt qui crée son propre `ExoPlayer` en lazy init (sur le main thread). `RadioPlayerService` injecte `PlayerController` et wrape le même player dans une `MediaSession`. Pas de `init()` externe — le player est prêt dès le premier accès à `playerController.player`.

### Build
- JDK 21 requis (via `org.gradle.java.home` pointant sur Android Studio JBR)
- Kotlin **2.0.21** + Hilt **2.51** + KSP **2.0.21-1.0.28** (Kotlin 2.1.x incompatible avec Hilt 2.51/2.52)
- AGP 8.7.3, Gradle 8.14.3

---

## Annexe — Exemples d'appels API

### Récupérer les radios françaises populaires

```bash
curl -H "User-Agent: CarRadio/1.0" \
  "https://de1.api.radio-browser.info/json/stations/search?countrycode=FR&order=clickcount&reverse=true&hidebroken=true&limit=200"
```

### Rechercher une station par nom

```bash
curl -H "User-Agent: CarRadio/1.0" \
  "https://de1.api.radio-browser.info/json/stations/search?name=rire&order=clickcount&reverse=true&hidebroken=true&limit=30"
```

### Suggestions de tags

```bash
curl -H "User-Agent: CarRadio/1.0" \
  "https://de1.api.radio-browser.info/json/tags/rock?order=stationcount&reverse=true&limit=10"
```

### Stations par tag

```bash
curl -H "User-Agent: CarRadio/1.0" \
  "https://de1.api.radio-browser.info/json/stations/search?tag=rock&order=clickcount&reverse=true&hidebroken=true&limit=50"
```

### Notifier un clic avant lecture

```bash
curl -H "User-Agent: CarRadio/1.0" \
  "https://de1.api.radio-browser.info/json/url/960e57c5-0601-11e8-ae97-52543be04c81"
```

### Lister tous les pays

```bash
curl -H "User-Agent: CarRadio/1.0" \
  "https://de1.api.radio-browser.info/json/countries?order=name&hidebroken=true"
```

---

*Document maintenu pour Claude Code — CarRadio v1.7*
