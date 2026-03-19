# BUG-003 — Station "Rire et Chansons" absente de la liste française

**Statut :** FIXED
**Date :** 2026-03-19

## Symptôme
La station "Rire et Chansons" n'apparaît pas dans la liste des radios françaises, alors qu'elle est bien référencée sur radio-browser.info.

## Cause racine
L'app triait les stations par `order=votes` avec `limit=200`. "Rire et Chansons" a 170 votes — insuffisant pour figurer dans le top 200 votes français (plus de 200 stations françaises ont davantage de votes).

Le site radio-browser.info utilise `order=clickcount` (nombre de lectures), pas `votes`. Avec ce tri, la station apparaît en position ~70.

## Fix appliqué
Changement du paramètre de tri de `votes` → `clickcount` dans `RadioBrowserApi.getStationsByCountryCode()` et `getStationsByCountryName()`.

## Section spec impactée
§3 — endpoint stations, paramètre `order`
