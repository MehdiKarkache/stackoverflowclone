# StackOverflow Clone

Clone de Stack Overflow : questions, réponses, commentaires, votes, tags et réputation.

## Stack

| Partie    | Technologies                                                     |
|-----------|------------------------------------------------------------------|
| Backend   | Spring Boot 4, Java 21, Spring Data JPA, Spring Security (JWT)   |
| Frontend  | React 19, TypeScript, Vite, react-router, react-markdown         |
| Base      | PostgreSQL 17 (Docker) / H2 en mémoire (dev local)               |
| Déploiement | Docker Compose, nginx (fichiers statiques + reverse proxy API) |

## Démarrage rapide (Docker)

```bash
docker compose up -d --build
```

| Service   | URL                          |
|-----------|------------------------------|
| Frontend  | http://localhost:3000        |
| API       | http://localhost:8080/api    |

Au premier démarrage, des données de démo sont créées. Comptes disponibles (mot de passe `password123`) :

| Utilisateur | Rôle  | Réputation |
|-------------|-------|------------|
| `admin`     | ADMIN | 2000       |
| `alice`     | USER  | 320        |
| `bob`       | USER  | 150        |

Variables d'environnement utiles (fichier `.env` à la racine) :

```env
DB_PASSWORD=un-mot-de-passe-solide
JWT_SECRET=une-cle-secrete-d-au-moins-32-caracteres
```

## Développement local (sans Docker)

Backend (port 8080, base H2 en mémoire) :

```bash
cd backend
./mvnw spring-boot:run
```

Frontend (port 5173, `/api` redirigé vers le backend) :

```bash
cd frontend
npm install
npm run dev
```

Tests backend :

```bash
cd backend
./mvnw test
```

## Structure

```
backend/
  model/        Entités JPA : User, Post (abstrait) -> Question / Answer, Comment, Vote, Tag
  repository/   Repositories Spring Data
  service/      Logique métier (questions, réponses, votes, réputation…)
  web/          Contrôleurs REST + gestion des erreurs
  security/     JWT et utilisateur courant
  config/       Sécurité, données de démo
frontend/
  src/api/        Client HTTP et types
  src/auth/       Contexte d'authentification
  src/components/ Composants réutilisables (votes, commentaires, éditeur Markdown…)
  src/pages/      Pages de l'application
```

## API

Lecture publique (`GET`), écriture avec l'en-tête `Authorization: Bearer <token>`.

| Méthode | Route                              | Description                                      |
|---------|------------------------------------|--------------------------------------------------|
| POST    | `/api/auth/register`               | Inscription                                      |
| POST    | `/api/auth/login`                  | Connexion (nom d'utilisateur ou email)           |
| GET     | `/api/auth/me`                     | Utilisateur connecté                             |
| GET     | `/api/questions`                   | Liste : `sort` (newest, votes, unanswered, views), `tag`, `q`, `author`, `page`, `size` |
| GET     | `/api/questions/{id}`              | Détail avec réponses et commentaires             |
| POST    | `/api/questions`                   | Poser une question                               |
| PUT / DELETE | `/api/questions/{id}`         | Modifier / supprimer (auteur ou modérateur)      |
| POST    | `/api/questions/{id}/answers`      | Répondre                                         |
| PUT / DELETE | `/api/answers/{id}`           | Modifier / supprimer une réponse                 |
| POST    | `/api/answers/{id}/accept`         | Accepter / retirer l'acceptation                 |
| POST    | `/api/posts/{id}/comments`         | Commenter une question ou une réponse            |
| DELETE  | `/api/comments/{id}`               | Supprimer un commentaire                         |
| POST    | `/api/posts/{id}/vote`             | Voter : `{"value": 1 \| -1 \| 0}`                |
| GET     | `/api/tags`                        | Tags par popularité (`q` pour filtrer)           |
| GET     | `/api/users`, `/api/users/{id}`    | Utilisateurs, profil                             |
| PUT     | `/api/users/me`                    | Modifier sa bio                                  |

## Règles de réputation

| Événement                       | Réputation          |
|---------------------------------|---------------------|
| Upvote reçu                     | +10                 |
| Downvote reçu                   | −2                  |
| Downvoter une réponse           | −1 pour le votant   |
| Réponse acceptée                | +15 (auteur), +2 (celui qui accepte) |

Privilèges : 15 points pour voter pour, 125 pour voter contre
(configurables avec `app.privileges.upvote` et `app.privileges.downvote`).
