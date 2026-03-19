# BUG-001 — ForegroundServiceDidNotStartInTimeException

**Statut :** FIXED
**Date :** 2026-03-19

## Symptôme
L'application crashait systématiquement au démarrage.

## Reproduction
Lancer l'application depuis le launcher.

## Logcat
```
android.app.RemoteServiceException$ForegroundServiceDidNotStartInTimeException:
Context.startForegroundService() did not then call Service.startForeground()
ServiceRecord com.carradio/.player.RadioPlayerService
```

## Cause racine
`HomeScreen` appelait `context.startForegroundService()` dans un `LaunchedEffect` au chargement de l'écran. Android impose que `startForeground()` soit appelé dans les 5 secondes. `MediaSessionService` de Media3 ne le fait qu'au démarrage de la lecture — donc si aucune station n'est lancée, le délai expire et le système tue le process.

## Fix appliqué
- Suppression du `startForegroundService()` dans `HomeScreen`
- `PlayerController` crée son propre `ExoPlayer` (lazy, sur le main thread)
- `startForegroundService()` est appelé uniquement dans `PlayerController.play()`, juste avant de lancer le stream
- `RadioPlayerService.onStartCommand()` appelle immédiatement `startForeground()` avec une notification minimale

## Règle technique à retenir
Ne jamais appeler `startForegroundService()` de façon préventive. Le service doit être démarré uniquement au moment où la lecture est réellement lancée.
