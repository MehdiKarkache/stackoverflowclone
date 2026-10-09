# StackOverflow Clone

Clone de Stack Overflow : questions, réponses, commentaires, votes, tags et réputation.

## Stack

| Partie    | Technologies                                             |
|-----------|----------------------------------------------------------|
| Backend   | Spring Boot 4, Spring Data JPA, Spring Security (JWT)    |
| Frontend  | React + Vite                                             |
| Base      | PostgreSQL (Docker) / H2 en mémoire (dev local)          |

## Structure

```
backend/    API REST Spring Boot
frontend/   Application React
```

## Lancer en local (sans Docker)

Backend (port 8080, base H2 en mémoire) :

```bash
cd backend
./mvnw spring-boot:run
```
