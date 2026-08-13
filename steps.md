
- [x] ~~Finaliser l’espace administrateur : résumé, agents, responsables et services.~~
- [x] ~~Permettre à l’administrateur de modifier le solde d’un agent de manière compatible avec l’acquisition de 2,5 jours/mois.~~
- [x] ~~Afficher l’historique des demandes côté agent.~~
- [x] ~~Nettoyer et relier tous les éléments de la sidebar.~~
- [x] ~~Améliorer le design et le responsive.~~
- [x] ~~Ajouter les indicateurs utiles dans les dashboards.~~
- [x] ~~Ajouter les données de démonstration et des demandes cohérentes.~~
- [x] ~~Vérifier le scénario complet avec les données de démonstration.~~
- [x] ~~Documenter les tests Postman et les règles métier.~~
- [x] ~~Vérifier les règles de solde, chevauchement et validation finale.~~
- [x] ~~Finaliser la gestion des années précédentes et futures.~~
- [x] ~~Ajouter le statut de l’agent : ACTIF, SUSPENDU, etc.~~
- [ ] Préparer la documentation et la démonstration du stage.
- [x] ~~Ajouter une distribution portable Docker avec PostgreSQL, backend et frontend.~~

## Vérifications effectuées

- [x] ~~Accès sans JWT : `401 Unauthorized`.~~
- [x] ~~Agent consultant ses demandes et son tableau de bord : `200 OK`.~~
- [x] ~~Agent tentant une approbation : `403 Forbidden`.~~
- [x] ~~Mauvais responsable tentant une approbation : `403 Forbidden`.~~
- [x] ~~Demande inexistante : `404 Not Found`.~~
- [x] ~~Validation niveau 1 puis niveau 2 : `200 OK`.~~
- [x] ~~Validation finale : statut `VALIDEE` et consommation unique du solde.~~
- [x] ~~Solde insuffisant : `400 Bad Request`.~~
- [x] ~~Chevauchement : `409 Conflict`.~~
- [x] ~~Date passée et année future : `400 Bad Request`.~~
- [x] ~~Annulation d’une demande en attente : `200 OK`, statut `ANNULEE`.~~
