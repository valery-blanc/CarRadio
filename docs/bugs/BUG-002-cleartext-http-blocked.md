# BUG-002 — Streams radio silencieux (HTTP bloqué)

**Statut :** FIXED
**Date :** 2026-03-19

## Symptôme
Les favoris s'affichaient correctement, les tuiles réagissaient au tap, mais aucun son ne sortait.

## Reproduction
Configurer un favori depuis Radio Browser, taper la tuile.

## Cause racine
Android 9+ bloque le trafic HTTP en clair par défaut (`usesCleartextTraffic=false`). La majorité des streams Radio Browser utilisent des URLs `http://` (non chiffrées). ExoPlayer tentait de charger le stream mais la connexion était refusée silencieusement par la politique réseau Android.

## Logcat
```
NetworkSecurityConfig: No Network Security Config specified, using platform default
```

## Fix appliqué
- Ajout de `android:usesCleartextTraffic="true"` dans `<application>` du manifeste
- Création de `res/xml/network_security_config.xml` avec `cleartextTrafficPermitted="true"` sur tous les domaines
- Référencé via `android:networkSecurityConfig="@xml/network_security_config"` dans le manifeste

## Règle technique à retenir
Les streams radio utilisent fréquemment HTTP. Toujours autoriser le trafic HTTP en clair dans les apps de streaming radio, via `network_security_config.xml`.
