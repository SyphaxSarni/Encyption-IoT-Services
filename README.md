# L3 USTHB PROJET DE FIN D'ETUDES
2ème semestre - 3ème année Licence académique(ACAD) - Université des sciences et de la technologie Houari Boumediène - ALGER

## Chiffrement pour service basé IoT
Le projet consiste à récuperer a travers un capteur des données de l'utilisateur (Battements de coeur dans notre cas). Les traité a travers un FoG, et les archiver dans un Cloud.

## Etapes:
- Récupération des données:
  Les données sont simulé a travers une application android. Les battements de coeurs sont générés.
- Création d'un serveur "FOG" en JAVA.
- Sécurisation de la communication entre le FoG et l'application android, grâce a la gestion de certificats.
  - Génération des certificats.
  - Signatures des certificats.
  - Implémentation dans l'application android.
- Implémentation de l'API firebase sur le serveur FOG.
- Archivage des données récupérées toutes les 2h.
- Utilisation de firebase pour l'archivage

## Outils et environnements:
- Java
- Android
- API Firebase
