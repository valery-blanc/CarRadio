# FEAT-013 — Navigation rapide entre recherche et favoris

**Statut :** DONE
**Version :** v1.13

---

## Contexte

Amélioration de la navigation entre la page de recherche et les pages de favoris.

## Comportements

### Réorganisation du pager

La page de recherche passe de la **dernière** position à la **première** (index 0).
Les pages de favoris sont aux indices 1..N.

Cela rend les swipes naturels sans code de gestion de gestes spécifique :

| Situation | Geste | Résultat |
|---|---|---|
| Page recherche | Swipe gauche | 1ère page favoris |
| 1ère page favoris | Swipe droit | Page recherche |
| Pages favoris intermédiaires | Swipe droite/gauche | Navigation normale |

### Icône Home dans la TopAppBar

- Visible uniquement sur la page recherche **et** s'il existe ≥ 1 page de favoris
- Clic → scroll animé vers la 1ère page de favoris (page 1)

### Icône Search dans la TopAppBar

- Visible sur **toutes** les pages de favoris
- Clic → scroll animé vers la page de recherche (page 0)

### Page de démarrage

- Si des favoris existent : l'app s'ouvre sur la 1ère page de favoris (page 1)
- Si aucun favori : l'app s'ouvre sur la page de recherche (page 0)

---

## Fichiers modifiés

- `HomeScreen.kt` — réorganisation du pager, ajout des icônes Home/Search
- `strings.xml` × 5 — chaînes `go_to_home`, `go_to_search`
