# Leave Management Platform

Full-stack web application designed during a software engineering internship at the Centre National de l’Informatique (CNI), Tunisia, to model and manage employee leave requests for a public-sector context.

The project covers the full workflow from leave-balance management to hierarchical approval, with separate interfaces and permissions for employees, managers and administrators.

## Why this project matters

The main engineering challenge was not simple CRUD. The application had to represent a real organizational workflow with different roles, leave balances, approval states and authorization rules while remaining easy to deploy locally as a complete stack.

## Engineering highlights

- Built a Java 21 / Spring Boot backend with REST endpoints and PostgreSQL persistence.
- Modeled employees, leave balances, leave requests, services and validation history with JPA entities.
- Implemented a hierarchical leave-approval workflow for employees and managers.
- Added stateless JWT authentication with Spring Security.
- Enforced role-based access control for `AGENT`, `RESPONSABLE` and `ADMIN` endpoints.
- Used BCrypt password hashing and validation at the API layer.
- Built a React + TypeScript frontend with role-specific dashboards.
- Containerized the frontend, backend and PostgreSQL database with Docker Compose.
- Added reproducible demo data and local setup scripts for Linux, Windows and macOS.

## Technology stack

| Area | Technologies |
| --- | --- |
| Backend | Java 21, Spring Boot 4, Spring MVC |
| Security | Spring Security, JWT, BCrypt, RBAC |
| Persistence | PostgreSQL 16, Spring Data JPA / Hibernate |
| Frontend | React 19, TypeScript, Vite, Axios |
| Infrastructure | Docker, Docker Compose, Nginx |
| API / Testing tools | REST, Postman, Maven |

## Functional scope

### Employee / Agent

- Authenticate and access a personal dashboard.
- View the current leave balance.
- Create a leave request.
- Consult personal requests and their status.
- Cancel eligible requests.

### Manager / Responsable

- Access employee and validation views.
- Review pending leave requests.
- Approve or reject requests.
- Consult validation history.
- Submit personal leave requests while retaining manager permissions.

### Administrator

- Access administrative endpoints.
- Manage agents and account status.
- Update leave balances and manager assignments.
- Maintain the organizational structure used by the workflow.

## Security model

The backend uses stateless JWT-based authentication. The JWT contains the authenticated user role, which is mapped by Spring Security to role-based authorities.

Examples of authorization rules:

- `/api/admin/**` → `ADMIN`
- approval and rejection endpoints → `RESPONSABLE`
- personal leave-request and balance endpoints → `AGENT` or `RESPONSABLE`

Passwords are hashed with BCrypt before storage.

## Architecture

```text
                         ┌──────────────────────┐
                         │ React / TypeScript   │
                         │ Vite + Nginx         │
                         └──────────┬───────────┘
                                    │ REST / JWT
                         ┌──────────▼───────────┐
                         │ Spring Boot API      │
                         │ Security · MVC · JPA │
                         └──────────┬───────────┘
                                    │
                         ┌──────────▼───────────┐
                         │ PostgreSQL 16        │
                         │ Persistent volume    │
                         └──────────────────────┘
```

Docker Compose starts the three application layers together and waits for the PostgreSQL health check before starting the backend.

## Domain model

The backend models the main business concepts required by the leave workflow:

- `User`
- `Agent`
- `Service`
- `LeaveBalance`
- `LeaveRequest`
- `Validation`
- role and request-status enumerations

The original internship specification includes a leave entitlement model based on 30 days per year / 2.5 days per month and a hierarchical validation workflow.

## Repository structure

```text
gestion-conges/
├── backend/               # Spring Boot REST API
│   ├── src/               # Controllers, entities, repositories, services, DTOs
│   ├── postman/           # API test collections
│   ├── demo-data.sql      # Demo dataset
│   └── Dockerfile
├── frontend/              # React + TypeScript + Vite
│   ├── src/
│   ├── nginx.conf
│   └── Dockerfile
├── docs/                  # Project documentation
├── rapport-stage/         # Internship report material
├── docker-compose.yml
├── install.sh
├── seed-demo.sh
└── stop.sh
```

## Run locally

Docker Desktop or Docker Engine with Docker Compose is required.

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

Then open:

```text
http://localhost:5173
```

To stop the stack:

```bash
docker compose down
```

The PostgreSQL data is persisted in the `postgres_data` Docker volume.

## Demo data

The repository includes a demo SQL dataset for local evaluation. Demo accounts cover the main roles and workflow states so the application can be explored without manually creating a complete organization.

## Internship context

The project was developed as part of a software engineering internship at the Centre National de l’Informatique (CNI), Tunisia.

The internship subject focused on:

- relational data modeling for employees and leave rights;
- implementation of a hierarchical approval workflow;
- leave-balance consultation;
- online leave-request creation;
- role-specific dashboards.

## What this project demonstrates

Backend development with Java and Spring Boot, relational database modeling, REST API design, authentication and authorization, role-based workflow implementation, full-stack integration, containerization and end-to-end delivery of a business application.

## Notes

This repository is an internship / portfolio project and should not be treated as a production deployment without environment-specific security hardening and configuration review.
