#!/usr/bin/env sh
set -eu

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker est requis. Installez Docker Desktop ou Docker Engine, puis relancez ce script."
  exit 1
fi

docker compose up --build -d
echo "Application disponible sur http://localhost:5173"
