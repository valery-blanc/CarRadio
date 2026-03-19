# FEAT-005 — Icône de l'application

**Statut :** DONE
**Date :** 2026-03-19

## Contexte
L'application n'a pas d'icône personnalisée. Le fichier source `carradio_icon.png` est disponible à la racine du projet.

## Comportement ajouté
L'icône `carradio_icon.png` est utilisée comme icône de lancement de l'application, redimensionnée pour toutes les densités Android requises.

## Spec technique
Densités à générer (ic_launcher.png et ic_launcher_round.png) :
| Density | Taille |
|---------|--------|
| mdpi    | 48×48  |
| hdpi    | 72×72  |
| xhdpi   | 96×96  |
| xxhdpi  | 144×144 |
| xxxhdpi | 192×192 |

- Répertoires : `app/src/main/res/mipmap-{density}/`
- `AndroidManifest.xml` : ajout `android:icon="@mipmap/ic_launcher"` et `android:roundIcon="@mipmap/ic_launcher_round"`

## Outil utilisé
ImageMagick (`magick`) pour le redimensionnement.

## Impact
- `carradio_icon.png` (source, inchangé)
- `app/src/main/res/mipmap-*/ic_launcher.png` (nouveaux)
- `app/src/main/res/mipmap-*/ic_launcher_round.png` (nouveaux)
- `AndroidManifest.xml`
