# Installation portable

## Principe

L'application est distribuée avec trois conteneurs :

- PostgreSQL pour la base de données ;
- Spring Boot pour l'API backend ;
- Nginx pour servir le frontend React et transmettre `/api` au backend.

Java, Maven, Node.js et PostgreSQL ne sont donc pas nécessaires sur la machine hôte.

## Prérequis unique

Installer Docker Desktop (Windows/macOS) ou Docker Engine avec Compose (Linux).

## Installation

Depuis la racine du projet :

```bash
./install.sh
```

Ou directement :

```bash
docker compose up --build -d
```

Ouvrir ensuite `http://localhost:5173`.

## Comptes de démonstration

Pour charger les comptes et les exemples de demandes du rapport :

```bash
sh seed-demo.sh
```

Le script est idempotent : il peut être relancé sans recréer les mêmes données.
Les comptes utilisent le mot de passe `password`, sauf `admin@test.com` qui
utilise `test`.

## Arrêt

```bash
./stop.sh
```

Les données PostgreSQL sont conservées dans le volume Docker `postgres_data`.
Pour supprimer volontairement les données :

```bash
docker compose down -v
```

## Dépannage

- Voir les logs : `docker compose logs -f`.
- Reconstruire après une modification : `docker compose up --build -d`.
- Vérifier les services : `docker compose ps`.
- Si les ports 5173 ou 8080 sont occupés, les modifier dans `docker-compose.yml`.
- Si Docker renvoie `permission denied`, exécuter les commandes avec `sudo` ou
  ajouter l'utilisateur courant au groupe Docker, puis ouvrir une nouvelle session.

Cette solution est portable entre les systèmes supportant Docker. Elle ne promet pas une indépendance absolue : Docker reste requis sur la machine d'accueil.
