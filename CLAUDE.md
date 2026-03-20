# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CarRadio is an Android internet radio streaming app designed for in-car use. The UI consists of large touch tiles organized in a 2-column × 4-row grid, 2 swipeable pages, supporting up to 16 favorite stations. The specification is the source of truth: `docs/specs/carradio-spec.md`.

## Build & Deploy

```bash
./gradlew build              # Build
./gradlew installDebug       # Deploy to connected device
./gradlew test               # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests
```

## Workflow Rules

### Task Tracking
For any task that involves more than 3 files or more than 3 steps:
1. BEFORE starting, create/update a checklist in `docs/tasks/TASKS.md`
2. Mark each sub-step with `[ ]` (todo), `[x]` (done), or `[!]` (blocked)
3. Update the checklist AFTER completing each sub-step
4. If the session is interrupted, the checklist is the source of truth for resuming work

### Resuming Work
When starting a new session or after /clear, ALWAYS:
1. Read `docs/tasks/TASKS.md` to check current progress
2. Identify the first unchecked item
3. Resume from there — do NOT restart completed work

### Documentation Synchronization (OBLIGATOIRE)

**À chaque demande de modification, bug fix ou nouvelle feature — quelle que soit
la façon dont elle est formulée (message direct, fichier temp_*.txt, description
orale) — TOUJOURS :**

1. **Créer ou mettre à jour le fichier de bug** (`docs/bugs/BUG-XXX-*.md`)
   ou de feature (`docs/specs/FEAT-XXX-*.md`) correspondant.

2. **Mettre à jour `docs/specs/carradio-spec.md`** — OBLIGATOIRE, SANS EXCEPTION.
   Ce fichier est la source de vérité de l'application. Il doit refléter à tout
   moment le comportement réel du code. Mettre à jour :
   - La section concernée (UI, navigation, persistance, architecture, etc.)
   - Le numéro de version en en-tête (FEAT-XXX / BUG-XXX)
   - La structure du projet §12 si des fichiers sont ajoutés/supprimés
   - Les cas limites si un nouveau cas est géré
   Ne pas attendre qu'on le demande. Si la feature est trop petite pour un §
   dédié, intégrer l'info dans la section la plus proche.

3. **Mettre à jour `docs/tasks/TASKS.md`** — toujours, sans condition :
   ajouter l'entrée si elle n'existe pas, cocher `[x]` les étapes terminées.

Cette règle s'applique MÊME pour les petites modifications demandées directement
dans le chat (ex : "désactive la mise en veille", "change la couleur", etc.).
Si c'est trop petit pour un fichier BUG/FEAT dédié, au minimum mettre à jour
`carradio-spec.md` si le comportement change.

### Règle de déploiement et confirmation (OBLIGATOIRE)

**Aucun commit ne doit être créé avant que l'utilisateur ait testé et confirmé.**

Ordre impératif pour tout bug fix ou feature :

```
[code] → [docs] → [./gradlew installDebug] → [demander test] → [attendre OK] → [commit]
```

- Le commit regroupe TOUJOURS : code source + fichiers de doc + TASKS.md
- Si l'utilisateur signale un problème après test → corriger, re-déployer,
  re-demander confirmation AVANT de committer
- **Si un crash est découvert lors du test** → créer `docs/bugs/BUG-XXX-*.md`
  (même si le crash a déjà été corrigé), mettre à jour `carradio-spec.md`
  avec la règle à retenir, et référencer dans TASKS.md
- Aucune exception : même pour une modification d'une seule ligne

### Bug Fix Workflow
1. Documenter le bug dans `docs/bugs/BUG-XXX-short-name.md` (symptôme,
   reproduction, logcat, section spec impactée)
2. Analyser la cause racine AVANT d'écrire le fix (Plan Mode)
3. Implémenter le fix
4. Mettre à jour toute la documentation :
   - `docs/bugs/BUG-XXX-*.md` → statut `FIXED`, fix appliqué décrit
   - **`docs/specs/carradio-spec.md` → OBLIGATOIRE** : mettre à jour la section
     du comportement corrigé
   - `docs/tasks/TASKS.md` → cocher `[x]` toutes les étapes terminées
5. **Déployer sur le téléphone** : `./gradlew installDebug`
6. **Demander à l'utilisateur de tester et attendre sa confirmation explicite**
   — NE PAS committer avant que l'utilisateur confirme que c'est OK
7. Une fois confirmé : committer TOUS les fichiers modifiés en un seul commit
   (code + docs + TASKS.md) : `"FIX BUG-XXX: description courte"`

### Feature Evolution Workflow
1. Écrire la spec dans `docs/specs/FEAT-XXX-short-name.md` (contexte,
   comportement, spec technique, impact sur l'existant)
2. Analyser l'impact sur le code existant (Plan Mode) : risques, conflits,
   lacunes de la spec
3. Décomposer en tâches dans `docs/tasks/TASKS.md`
4. Implémenter
5. Mettre à jour toute la documentation :
   - `docs/specs/FEAT-XXX-*.md` → statut `DONE`, implémentation décrite
   - **`docs/specs/carradio-spec.md` → OBLIGATOIRE** : intégrer le nouveau
     comportement dans la/les section(s) concernée(s), incrémenter la version
   - `docs/tasks/TASKS.md` → cocher `[x]` toutes les étapes terminées
6. **Déployer sur le téléphone** : `./gradlew installDebug`
7. **Demander à l'utilisateur de tester et attendre sa confirmation explicite**
   — NE PAS committer avant que l'utilisateur confirme que c'est OK
8. Une fois confirmé : committer TOUS les fichiers modifiés en un seul commit
   (code + docs + TASKS.md) : `"FEAT-XXX: description courte"`
9. Mettre à jour CLAUDE.md si des règles d'architecture ont changé

### Règle de build release (OBLIGATOIRE)

**À chaque build release (`./gradlew bundleRelease`) :**

1. **Incrémenter `versionCode`** dans `app/build.gradle.kts` AVANT de builder.
   Le Play Store rejette tout AAB dont le `versionCode` a déjà été uploadé.
   Règle : `versionCode` = numéro séquentiel strictement croissant, sans exception.
   Mettre à jour `versionName` si la version utilisateur change (ex: "1.1", "2.0").

2. **Vérifier `proguard-rules.pro`** avant d'activer ou de modifier la minification.
   La minification R8 (`isMinifyEnabled = true`) casse silencieusement :
   - **Retrofit + Gson** : les DTOs (`data/api/dto/`) doivent être dans les règles `-keep`
   - **Hilt** : les classes `@HiltViewModel` et `@Inject` doivent être conservées
   - **Room** : les entités `@Entity` et DAOs `@Dao` doivent être conservées
   - **Media3** : les classes ExoPlayer doivent être conservées
   Le fichier `app/proguard-rules.pro` contient toutes ces règles.
   Si un nouveau DTO, ViewModel ou entité Room est ajouté, vérifier qu'il est couvert.

## Architecture

**Pattern:** MVVM + Repository, Jetpack Compose UI

```
UI (Compose screens)
  HomeScreen — 2-page swipeable tile grid, now-playing bar
  SettingsScreen
  FavoritesPickerScreen → CountryPickerScreen → StationListScreen

ViewModels (Hilt-injected, StateFlow)
  HomeViewModel / SettingsViewModel / FavoritesViewModel

Repository layer
  RadioRepository — abstracts local DB and remote API

Data sources
  RadioBrowserApi (Retrofit) — radio-browser.info, no API key, server resolved dynamically via DNS
  Room Database — stores FavoriteStation entities (position field encodes page + slot index)

Player
  RadioPlayerService (MediaSessionService) — ExoPlayer, background playback
  PlayerController — ExoPlayer wrapper used by ViewModels
```

**DI:** Hilt modules — AppModule, NetworkModule, DatabaseModule
**Package root:** `com.carradio`
**Entry point:** `CarRadioApplication` (@HiltAndroidApp), `MainActivity`

## Technology Stack

| Component | Library & Version |
|---|---|
| UI | Jetpack Compose BOM 2024.04.00 |
| Navigation | Navigation Compose 2.7.7 |
| Audio | AndroidX Media3 / ExoPlayer 1.3.1 |
| Networking | Retrofit 2.11.0 + OkHttp logging 4.12.0 |
| Database | Room 2.6.1 |
| DI | Hilt 2.51 |
| Images | Coil 2.6.0 |
| Min SDK | API 26 (Android 8.0) |

## Key Behaviors

- **Position encoding:** Each `FavoriteStation` has a `position` int that encodes both page (0 or 1) and slot (0–7).
- **Radio Browser server:** The API base URL must be resolved dynamically via DNS lookup on `all.api.radio-browser.info` before making requests.
- **Screen-on during playback:** `FLAG_KEEP_SCREEN_ON` must be set while audio is playing.
- **Audio focus:** The player must handle focus loss (pause on calls, duck on notifications).
- **Background playback:** `RadioPlayerService` runs as a foreground service with `foregroundServiceType="mediaPlayback"`.
