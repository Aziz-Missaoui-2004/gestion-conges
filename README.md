# Gestion des congés

Application Web de gestion des congés pour la fonction publique.

## Lancer l'application

Docker Desktop ou Docker Engine avec Compose est requis.

### Linux

```bash
sudo sh install.sh
sudo sh seed-demo.sh
```

### Windows PowerShell

```powershell
docker compose up --build -d
Get-Content .\backend\demo-data.sql -Raw | docker compose exec -T database psql -U gestion_conges -d gestion_conges
```

### macOS

```bash
sh install.sh
sh seed-demo.sh
```

Ouvrir ensuite <http://localhost:5173>.

Les comptes de démonstration utilisent le mot de passe `password`, sauf
`admin@test.com`, `responsable1@test.com`, `responsable2@test.com` et `agent@test.com` qui utilisent `test`.

Pour arrêter l'application :

Linux : `sudo sh stop.sh` — Windows/macOS : `docker compose down`

Les données sont conservées dans le volume Docker `postgres_data`. Ne pas
utiliser `docker compose down -v` sauf si la suppression de la base est voulue.
