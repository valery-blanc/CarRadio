# FEAT-008 — Sleep Timer (Minuteur sommeil)

**Statut :** DONE
**Version spec :** v1.8

## Contexte

Permettre à l'utilisateur de programmer l'arrêt automatique de la radio après un délai défini, par exemple pour s'endormir en écoutant la musique.

## Comportement

### Accès
- Icône sablier (`HourglassEmpty`) dans la `TopAppBar` de l'écran principal, à gauche de l'icône Paramètres.
- Lorsque le minuteur est actif, l'icône change (`HourglassTop`) et devient visuellement distincte.

### Écran de réglage (`SleepTimerScreen`)
- Trois roues (drum picker) pour régler : **heures** (0–23), **minutes** (0–59), **secondes** (0–59).
- Design inspiré du minuteur Android stock : roues centrées avec la valeur sélectionnée en grand et en couleur primaire, valeurs adjacentes en petit et atténuées.
- Bouton **Démarrer** : lance le minuteur et revient à l'écran principal.
- Bouton **Annuler** : visible uniquement si le minuteur est en cours — l'annule et remet le volume à 1.

### Écran principal — TopAppBar quand minuteur actif
- Icône `Bedtime` (zzzz/lune) + temps restant affiché (`H:MM:SS` ou `MM:SS`), cliquable → écran minuteur.
- Icône sablier cliquable → écran minuteur.
- Icône Paramètres toujours présente.

### Comportement à la fin
- Durant les **30 dernières secondes** : le volume baisse progressivement de 1.0 à 0.0.
- À l'expiration : `playerController.stop()` puis `killProcess()` — l'application se ferme.

## Spec technique

### Nouveau fichier : `SleepTimerViewModel.kt`
- Scoped à l'activité (créé dans `NavGraph` avant le `NavHost`).
- Coroutine dans `viewModelScope` : décrémente chaque seconde, gère le fade volume.
- StateFlows : `hours`, `minutes`, `seconds` (réglages), `isRunning`, `remainingSeconds`.

### Nouveau fichier : `SleepTimerScreen.kt`
- Composable `WheelPicker` (private) : `LazyColumn` + `rememberSnapFlingBehavior`, items de padding haut/bas pour centrage.
- `SleepTimerScreen` composable public : 3 roues + boutons.

### Modifications : `NavGraph.kt`
- Nouvelle route `SLEEP_TIMER = "sleep_timer"`.
- `SleepTimerViewModel` instancié dans `NavGraph()` avant `NavHost` → partagé entre `HomeScreen` et `SleepTimerScreen`.

### Modifications : `HomeScreen.kt`
- Nouveaux paramètres : `onNavigateToTimer`, `isTimerRunning`, `remainingSeconds`.
- `TopAppBar` : affiche `Bedtime` + temps restant (cliquable) + hourglass + settings.

## Impact sur l'existant
- `PlayerController` : usage de `player.volume` pour le fade (API ExoPlayer standard).
- Aucun impact sur Room, Retrofit, ou les autres écrans.

## Cas limites
- Si `remainingSeconds <= 0` au moment de Démarrer → rien ne se passe.
- Si l'utilisateur annule le minuteur → volume remis à 1.0 immédiatement.
- Si le minuteur expire mais que la radio n'est pas en cours → stop sans crash.
