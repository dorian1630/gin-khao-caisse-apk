# Gin Khao Caisse — APK

Cadre Android plein écran autour de la caisse web (`pos.html`). Le contenu mixte est autorisé
(la page https parle au Pi d'impression en http), l'écran reste allumé, le bouton Retour ne quitte jamais la caisse.

## Une URL par restaurant
`app/src/main/java/com/ginkhao/caisse/MainActivity.java` → ligne `URL_CAISSE` → l'adresse `pos.html` du restaurant → commit.
Le workflow **Build APK Caisse** (onglet Actions) construit `app-debug.apk` → Artifacts → renommer `caisse-<resto>.apk` → installer sur la tablette caisse.

## Réglage du relais
Dans la caisse : ☰ → Réglages → Relais d'impression → `http://IP-DU-PI:9100`.
