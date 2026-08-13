#!/usr/bin/env sh
set -eu

docker compose exec -T database psql \
  -U gestion_conges \
  -d gestion_conges \
  < backend/demo-data.sql

echo "Donnees de demonstration chargees."
