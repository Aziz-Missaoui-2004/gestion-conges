# Tests fonctionnels

## Préparation

1. Démarrer PostgreSQL et le backend sur `http://localhost:8080`.
2. Importer `docs/gestion-conges.postman_collection.json` dans Postman.
3. Utiliser les comptes de démonstration avec le mot de passe `password`.

## Scénarios principaux

| Scénario | Résultat attendu |
|---|---|
| Appel protégé sans JWT | `401 Unauthorized` |
| Agent consulte ses demandes | `200 OK`, uniquement ses demandes |
| Agent tente d’approuver | `403 Forbidden` |
| Mauvais responsable tente d’approuver | `403 Forbidden` |
| Demande inexistante | `404 Not Found` |
| Demande avec dates invalides | `400 Bad Request` |
| Période déjà demandée | `409 Conflict` |
| Solde insuffisant | `400 Bad Request` |
| Responsable niveau 1 approuve | `200 OK`, demande encore `EN_ATTENTE` |
| Responsable niveau final approuve | `200 OK`, demande `VALIDEE` |
| Responsable refuse | `200 OK`, demande `REFUSEE`, aucun jour consommé |
| Agent annule sa demande en attente | `200 OK`, demande `ANNULEE` |

## Règles métier vérifiées

- Les jours sont calendaires : `dateFin - dateDebut + 1`.
- L’acquisition est de `2,5 jours par mois commencé`, plafonnée à 30 jours par année.
- Les jours sont consommés une seule fois lors de la validation finale.
- Une demande en attente réserve les jours correspondants.
- Une demande refusée ou annulée ne consomme aucun jour.
- Une demande ne peut être traitée que par le validateur indiqué dans la chaîne hiérarchique.
- Une demande ne peut pas être créée dans le passé ou sur une année future.
