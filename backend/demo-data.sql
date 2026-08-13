-- Donnees de demonstration idempotentes.
-- Mot de passe des comptes demo : password

DO $$
DECLARE
    service_demo_id BIGINT;
    resp1_id BIGINT;
    resp2_id BIGINT;
    resp3_id BIGINT;
    resp4_id BIGINT;
    agent_test_id BIGINT;
    leila_id BIGINT;
    sami_id BIGINT;
    amine_id BIGINT;
    leila_request_id BIGINT;
    sami_request_id BIGINT;
    refused_request_id BIGINT;
    amine_request_id BIGINT;
BEGIN
    INSERT INTO services (nom)
    VALUES ('Direction des ressources humaines')
    ON CONFLICT (nom) DO NOTHING;

    SELECT id INTO service_demo_id
    FROM services
    WHERE nom = 'Direction des ressources humaines';

    INSERT INTO users (email, password, role)
    VALUES (
        'demo.responsable3@cni.tn',
        '$2b$12$3MKwNUddbb7oCmgiRmsJOeZfICodRUsAoHerEPK.M9pWzEBgKwDMq',
        'RESPONSABLE'
    )
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO users (email, password, role)
    VALUES (
        'demo.leila@cni.tn',
        '$2b$12$3MKwNUddbb7oCmgiRmsJOeZfICodRUsAoHerEPK.M9pWzEBgKwDMq',
        'AGENT'
    )
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO users (email, password, role)
    VALUES (
        'demo.sami@cni.tn',
        '$2b$12$3MKwNUddbb7oCmgiRmsJOeZfICodRUsAoHerEPK.M9pWzEBgKwDMq',
        'AGENT'
    )
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO users (email, password, role)
    VALUES (
        'demo.responsable4@cni.tn',
        '$2b$12$3MKwNUddbb7oCmgiRmsJOeZfICodRUsAoHerEPK.M9pWzEBgKwDMq',
        'RESPONSABLE'
    )
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO users (email, password, role)
    VALUES (
        'demo.amine@cni.tn',
        '$2b$12$3MKwNUddbb7oCmgiRmsJOeZfICodRUsAoHerEPK.M9pWzEBgKwDMq',
        'AGENT'
    )
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO users (email, password, role)
    VALUES (
        'admin@test.com',
        '$2b$12$Ztd/DYPBk0UKAHUPsioIG.gPbWc2qEoF3rvS0xYG5DKWiVKl4uIP2',
        'ADMIN'
    )
    ON CONFLICT (email) DO NOTHING;

    -- Comptes historiques utilisés par les scénarios et les captures du rapport.
    INSERT INTO users (email, password, role)
    VALUES ('responsable2@test.com', '$2b$12$3MKwNUddbb7oCmgiRmsJOeZfICodRUsAoHerEPK.M9pWzEBgKwDMq', 'RESPONSABLE')
    ON CONFLICT (email) DO NOTHING;
    INSERT INTO users (email, password, role)
    VALUES ('responsable1@test.com', '$2b$12$3MKwNUddbb7oCmgiRmsJOeZfICodRUsAoHerEPK.M9pWzEBgKwDMq', 'RESPONSABLE')
    ON CONFLICT (email) DO NOTHING;
    INSERT INTO users (email, password, role)
    VALUES ('agent@test.com', '$2b$12$3MKwNUddbb7oCmgiRmsJOeZfICodRUsAoHerEPK.M9pWzEBgKwDMq', 'AGENT')
    ON CONFLICT (email) DO NOTHING;

    -- Création de la hiérarchie minimale nécessaire aux validations à deux niveaux.
    SELECT id INTO resp2_id FROM agents a JOIN users u ON u.id = a.user_id
    WHERE u.email = 'responsable2@test.com';
    IF resp2_id IS NULL THEN
        INSERT INTO agents (user_id, nom, prenom, date_embauche, service_id, responsable_direct_id)
        VALUES ((SELECT id FROM users WHERE email = 'responsable2@test.com'), 'Responsable', 'Deux', '2020-01-06', service_demo_id, NULL)
        RETURNING id INTO resp2_id;
    END IF;

    SELECT id INTO resp1_id FROM agents a JOIN users u ON u.id = a.user_id
    WHERE u.email = 'responsable1@test.com';
    IF resp1_id IS NULL THEN
        INSERT INTO agents (user_id, nom, prenom, date_embauche, service_id, responsable_direct_id)
        VALUES ((SELECT id FROM users WHERE email = 'responsable1@test.com'), 'Responsable', 'Un', '2021-01-11', service_demo_id, resp2_id)
        RETURNING id INTO resp1_id;
    END IF;

    SELECT id INTO agent_test_id FROM agents a JOIN users u ON u.id = a.user_id
    WHERE u.email = 'agent@test.com';
    IF agent_test_id IS NULL THEN
        INSERT INTO agents (user_id, nom, prenom, date_embauche, service_id, responsable_direct_id)
        VALUES ((SELECT id FROM users WHERE email = 'agent@test.com'), 'Missaoui', 'Aziz', '2023-01-02', service_demo_id, resp1_id)
        RETURNING id INTO agent_test_id;
    END IF;

    SELECT a.id INTO resp1_id FROM agents a JOIN users u ON u.id = a.user_id
    WHERE u.email = 'responsable1@test.com';
    SELECT a.id INTO resp2_id FROM agents a JOIN users u ON u.id = a.user_id
    WHERE u.email = 'responsable2@test.com';
    SELECT a.id INTO resp3_id FROM agents a JOIN users u ON u.id = a.user_id
    WHERE u.email = 'demo.responsable3@cni.tn';
    SELECT a.id INTO resp4_id FROM agents a JOIN users u ON u.id = a.user_id
    WHERE u.email = 'demo.responsable4@cni.tn';

    IF NOT EXISTS (SELECT 1 FROM agents WHERE user_id = (SELECT id FROM users WHERE email = 'demo.responsable3@cni.tn')) THEN
        INSERT INTO agents (user_id, nom, prenom, date_embauche, service_id, responsable_direct_id)
        VALUES ((SELECT id FROM users WHERE email = 'demo.responsable3@cni.tn'), 'Ben Salem', 'Nadia', '2022-01-10', service_demo_id, NULL)
        RETURNING id INTO resp3_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM agents WHERE user_id = (SELECT id FROM users WHERE email = 'demo.leila@cni.tn')) THEN
        INSERT INTO agents (user_id, nom, prenom, date_embauche, service_id, responsable_direct_id)
        VALUES ((SELECT id FROM users WHERE email = 'demo.leila@cni.tn'), 'Trabelsi', 'Leila', '2023-03-15', service_demo_id, resp1_id)
        RETURNING id INTO leila_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM agents WHERE user_id = (SELECT id FROM users WHERE email = 'demo.sami@cni.tn')) THEN
        INSERT INTO agents (user_id, nom, prenom, date_embauche, service_id, responsable_direct_id)
        VALUES ((SELECT id FROM users WHERE email = 'demo.sami@cni.tn'), 'Mansouri', 'Sami', '2024-06-03', service_demo_id, resp3_id)
        RETURNING id INTO sami_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM agents WHERE user_id = (SELECT id FROM users WHERE email = 'demo.responsable4@cni.tn')) THEN
        INSERT INTO agents (user_id, nom, prenom, date_embauche, service_id, responsable_direct_id)
        VALUES ((SELECT id FROM users WHERE email = 'demo.responsable4@cni.tn'), 'Jaziri', 'Hatem', '2021-09-01', service_demo_id, resp3_id)
        RETURNING id INTO resp4_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM agents WHERE user_id = (SELECT id FROM users WHERE email = 'demo.amine@cni.tn')) THEN
        INSERT INTO agents (user_id, nom, prenom, date_embauche, service_id, responsable_direct_id)
        VALUES ((SELECT id FROM users WHERE email = 'demo.amine@cni.tn'), 'Gharbi', 'Amine', '2024-02-12', service_demo_id, resp4_id)
        RETURNING id INTO amine_id;
    END IF;

    SELECT a.id INTO leila_id FROM agents a JOIN users u ON u.id = a.user_id WHERE u.email = 'demo.leila@cni.tn';
    SELECT a.id INTO sami_id FROM agents a JOIN users u ON u.id = a.user_id WHERE u.email = 'demo.sami@cni.tn';
    SELECT a.id INTO resp4_id FROM agents a JOIN users u ON u.id = a.user_id WHERE u.email = 'demo.responsable4@cni.tn';
    SELECT a.id INTO amine_id FROM agents a JOIN users u ON u.id = a.user_id WHERE u.email = 'demo.amine@cni.tn';

    INSERT INTO leave_balances (agent_id, annee, jours_acquis, jours_consommes, jours_ajustement)
    VALUES (leila_id, 2026, 30, 0, 0)
    ON CONFLICT (agent_id, annee) DO NOTHING;
    INSERT INTO leave_balances (agent_id, annee, jours_acquis, jours_consommes, jours_ajustement)
    VALUES (sami_id, 2026, 30, 3, 0)
    ON CONFLICT (agent_id, annee) DO NOTHING;
    INSERT INTO leave_balances (agent_id, annee, jours_acquis, jours_consommes, jours_ajustement)
    VALUES (resp3_id, 2026, 30, 0, 0)
    ON CONFLICT (agent_id, annee) DO NOTHING;
    INSERT INTO leave_balances (agent_id, annee, jours_acquis, jours_consommes, jours_ajustement)
    VALUES (resp4_id, 2026, 30, 0, 0)
    ON CONFLICT (agent_id, annee) DO NOTHING;
    INSERT INTO leave_balances (agent_id, annee, jours_acquis, jours_consommes, jours_ajustement)
    VALUES (amine_id, 2026, 30, 0, 0)
    ON CONFLICT (agent_id, annee) DO NOTHING;

    IF NOT EXISTS (SELECT 1 FROM leave_requests WHERE agent_id = leila_id AND date_debut = '2026-09-07') THEN
        INSERT INTO leave_requests (agent_id, date_debut, date_fin, nombre_jours, statut, motif, created_at, prochain_validateur_id, niveau_validation)
        VALUES (leila_id, '2026-09-07', '2026-09-11', 5, 'EN_ATTENTE', 'Congé annuel', NOW(), resp1_id, 1)
        RETURNING id INTO leila_request_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM leave_requests WHERE agent_id = sami_id AND date_debut = '2026-10-12') THEN
        INSERT INTO leave_requests (agent_id, date_debut, date_fin, nombre_jours, statut, motif, created_at, prochain_validateur_id, niveau_validation)
        VALUES (sami_id, '2026-10-12', '2026-10-14', 3, 'VALIDEE', 'Participation familiale', NOW(), NULL, 2)
        RETURNING id INTO sami_request_id;
    END IF;

    SELECT id INTO sami_request_id FROM leave_requests WHERE agent_id = sami_id AND date_debut = '2026-10-12';
    IF NOT EXISTS (SELECT 1 FROM validations WHERE leave_request_id = sami_request_id) THEN
        INSERT INTO validations (leave_request_id, validateur_id, niveau, decision, commentaire, date_decision)
        VALUES (sami_request_id, resp3_id, 1, 'APPROUVEE', 'Validation du responsable direct', NOW());
        INSERT INTO validations (leave_request_id, validateur_id, niveau, decision, commentaire, date_decision)
        VALUES (sami_request_id, resp2_id, 2, 'APPROUVEE', 'Validation finale', NOW());
    END IF;

    IF NOT EXISTS (SELECT 1 FROM leave_requests WHERE agent_id = leila_id AND date_debut = '2026-11-02') THEN
        INSERT INTO leave_requests (agent_id, date_debut, date_fin, nombre_jours, statut, motif, created_at, prochain_validateur_id, niveau_validation)
        VALUES (leila_id, '2026-11-02', '2026-11-04', 3, 'REFUSEE', 'Convenance personnelle', NOW(), NULL, 1)
        RETURNING id INTO refused_request_id;
    END IF;

    SELECT id INTO refused_request_id FROM leave_requests WHERE agent_id = leila_id AND date_debut = '2026-11-02';
    IF NOT EXISTS (SELECT 1 FROM validations WHERE leave_request_id = refused_request_id) THEN
        INSERT INTO validations (leave_request_id, validateur_id, niveau, decision, commentaire, date_decision)
        VALUES (refused_request_id, resp1_id, 1, 'REFUSEE', 'Période non compatible avec le service', NOW());
    END IF;

    IF NOT EXISTS (SELECT 1 FROM leave_requests WHERE agent_id = amine_id AND date_debut = '2026-12-07') THEN
        INSERT INTO leave_requests (agent_id, date_debut, date_fin, nombre_jours, statut, motif, created_at, prochain_validateur_id, niveau_validation)
        VALUES (amine_id, '2026-12-07', '2026-12-09', 3, 'EN_ATTENTE', 'Congé annuel', NOW(), resp4_id, 1)
        RETURNING id INTO amine_request_id;
    END IF;
END $$;
