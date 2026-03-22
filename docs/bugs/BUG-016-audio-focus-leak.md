# BUG-016 — Disparition complète du son après plusieurs utilisations

**Statut :** FIXED
**Signalé par :** testeur externe
**Symptôme :** Après plusieurs sessions d'écoute, tout le son du téléphone disparaît. Seul un redémarrage du téléphone restaure le son.

---

## Symptôme

Après plusieurs play/stop de stations, le son disparaît entièrement sur le téléphone (pas seulement dans l'app). Redémarrage nécessaire pour récupérer le son.

## Cause racine

Deux bugs combinés causaient un **audio focus leak** :

### Bug 1 — `RadioPlayerService.onDestroy()` libérait le player singleton

```kotlin
// AVANT (incorrect)
override fun onDestroy() {
    mediaSession?.run {
        player.release()  // ← détruisait le player singleton de PlayerController
        release()
    }
}
```

`PlayerController.player` est un `@Singleton` dont la durée de vie est celle du processus. Quand Android détruisait le service (mémoire basse, etc.), `player.release()` était appelé. Lors de la prochaine lecture, `PlayerController` appelait des méthodes sur un player déjà libéré. ExoPlayer pouvait alors acquérir le focus audio sans jamais le relâcher → perte totale du son système.

### Bug 2 — Le service ne s'arrêtait jamais quand la lecture était stoppée

`PlayerController.stop()` arrêtait le player mais laissait le `RadioPlayerService` tourner avec `START_STICKY`. Si Android tuait ce service inactif ultérieurement, `onDestroy()` était appelé → déclenchait le Bug 1.

## Fix appliqué

**`RadioPlayerService.onDestroy()`** : suppression de `player.release()`. Le player appartient à `PlayerController`, pas au service. Il sera libéré naturellement par l'OS à la fin du processus.

**`PlayerController.stop()`** : ajout de `context.stopService(Intent(context, RadioPlayerService::class.java))` après l'arrêt de la lecture. Le service est stoppé proprement par le code plutôt qu'être tué par Android à l'improviste.

## Règle à retenir

Dans une architecture où le player ExoPlayer est un singleton injecté dans un service via Hilt, **le service ne doit pas appeler `player.release()`** — seul le propriétaire du player (ici `PlayerController`) peut le libérer, et uniquement en fin de processus.
