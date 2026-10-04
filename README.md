# 🎮 GameHub

Full-stack mini-games platform: a catalog of free-to-play games, JWT authentication, persistent favorites and comments, an AI-powered game finder (Google Gemini), and an original playable game, **Connect Four (Deadpool vs Wolverine)**.

It started as a university frontend project (HTML, CSS and JavaScript) and grew into a complete application with a Spring Boot backend, a database, external APIs and AI.

## ✨ Features

- 🎮 Dynamic catalog with carousels by genre (genres are built from the real data).
- 🔄 Automatic catalog synchronization with the FreeToGame API.
- 🔐 User registration and login with JWT.
- ❤️ Persistent favorites.
- 💬 Comments on every game.
- 🤖 AI game finder (Gemini) with a local keyword search as fallback.
- 🕹️ Playable Connect Four, stored in the database as GameHub's original game.
- 🧭 Side menu with search and genres, identical on every page.
- 🎨 Two switchable designs: **classic** and **new (Sunset Arcade)**.
- 📱 Responsive layout.

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, vanilla JavaScript, Fetch API, LocalStorage |
| Backend | Java 21, Spring Boot 3.5, Spring Data JPA, JJWT, BCrypt (spring-security-crypto), Maven |
| Database | H2 file database (development) · PostgreSQL (optional profile) |
| Integrations | FreeToGame API, Google Gemini API |
| Testing | JUnit 5, Mockito, Spring Boot Test |

## 🏗️ Architecture

A single server: Spring Boot exposes the REST API **and serves the frontend** (Maven copies the project root into `target/classes/static` on every build).

```text
Browser ──HTTP/JSON──▶ Spring Boot (localhost:8080)
                        ├── Static frontend (HTML + CSS + JS)
                        ├── REST API: users · games · favorites · comments · AI
                        └── Database (H2 / PostgreSQL)

External integrations: FreeToGame (catalog) · Google Gemini (AI)
```

## 📁 Project Structure

```text
GamesHub/
├── backend/                 # Spring Boot (source, tests, pom.xml)
├── css/
│   ├── style.css            # Classic design (base)
│   └── rebrand.css          # New design, loaded on top of the classic one
├── js/
│   ├── layout.js            # Shared header, side menu and user menu
│   ├── sidebar.js           # Genres and search of the side menu
│   ├── theme.js             # Design switcher (classic / new)
│   ├── api.js               # API client (JWT, error handling)
│   ├── frontend.js          # Home: catalog and carousels
│   ├── game-detail.js       # Detail page for catalog games
│   ├── game-social.js       # Favorite and comments for Connect Four
│   ├── favorites.js · login.js · register.js · site.js · ...
│   └── juego/               # Connect Four game logic
├── Images/ · Iconos/ · fonts/ · NumberBlocks/
├── index.html               # Home
├── game.html                # Connect Four
├── game-detail.html         # Detail page for catalog games
├── favorites.html · login.html · register.html
└── iniciar.bat              # Windows shortcut to start everything
```

## 🚀 Running Locally

### Requirements

- JDK 21 or higher.
- Maven 3.9 or higher.
- Internet access to import the FreeToGame catalog and to use Gemini (the app works without Gemini).
- PostgreSQL only if you use the `postgres` profile.

### Start the application

On Windows, double-click `iniciar.bat`. Or from a terminal:

```bash
cd backend
mvn spring-boot:run
```

Then open `http://localhost:8080`.

- The frontend is copied into the application on every start. If you edit HTML, CSS or JS, **restart the backend** and hard-refresh with `Ctrl+F5` to bypass the browser cache.
- On startup, if the catalog is empty it is imported automatically from FreeToGame. If that fails or there is no connection, the home page shows an **Importar catálogo** button; you can also trigger it manually:

```bash
curl -X POST http://localhost:8080/api/games/sync
```

  The sync performs an *upsert* by FreeToGame's external id, so running it again does not create duplicates.
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/gamehub`, user `sa`, empty password). Stop the backend before opening the file from another tool.
- Alternative: serve the frontend with another static server (for example `python -m http.server 5500`); the API is still consumed from `http://localhost:8080`. Opening the HTML files with `file://` is not recommended.

## ⚙️ Configuration

Data (users, favorites, comments and the catalog) is stored in `backend/data/` and survives restarts. To start from scratch, delete that folder while the backend is stopped.

Environment variables:

| Variable | Purpose | Default |
|---|---|---|
| `GEMINI_API_KEY` | Gemini key for the AI game finder | Empty (local search is used) |
| `GEMINI_MODEL` | Gemini model | `gemini-flash-latest` |
| `JWT_SECRET` | Key used to sign JWTs | Local development key |
| `JWT_EXPIRATION_MS` | JWT lifetime in milliseconds | `86400000` (24 hours) |
| `CATALOG_SYNC_ON_STARTUP` | Import FreeToGame on startup if the catalog is empty | `true` |
| `H2_URL` | H2 JDBC URL (e.g. `jdbc:h2:mem:gamehub` for an in-memory database) | `jdbc:h2:file:./data/gamehub` |
| `CORS_ALLOWED_ORIGINS` | Origins allowed to call the API | Local ports 8080 and 5500 |
| `DATABASE_URL` · `DATABASE_USERNAME` · `DATABASE_PASSWORD` | PostgreSQL connection | `jdbc:postgresql://localhost:5432/gamehub` · `postgres` · `postgres` |

### Gemini API key

The key is read from the `GEMINI_API_KEY` environment variable. Never write it in repository files.

```powershell
setx GEMINI_API_KEY "YOUR_KEY"
```

Then close all terminals and open a new one. In IntelliJ, add it under *Run → Edit Configurations → Environment variables*. Without a key, the finder falls back to a local keyword search (with Spanish → English synonyms).

### PostgreSQL

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Define `DATABASE_URL`, `DATABASE_USERNAME` and `DATABASE_PASSWORD` beforehand.

For real environments, set a long, private `JWT_SECRET`. Keys and passwords must not be committed to the repository.

## 🎨 Designs: Classic and New

The new design (*Sunset Arcade*) lives in `css/rebrand.css` and is loaded **on top of** `style.css` without modifying it. `js/theme.js` decides which one to use.

- Floating button at the bottom right: **Ver diseño clásico** / **Probar nuevo diseño**. The choice is remembered in the browser.
- Via URL: `?theme=classic` or `?theme=rebrand`.
- Change the default design: `DEFAULT_THEME` in `js/theme.js`.
- The new design uses its own logo (`Iconos/tituloLogoNuevo.svg`) and can be tuned almost entirely from the variables at the top of `rebrand.css`.

The header, side menu and user menu are generated once by `js/layout.js`; each page only contains `<div id="site-layout"></div>`.

## 🔌 REST API

Protected endpoints expect the header `Authorization: Bearer <token>`.

### Users

| Method | Route | Access | Description |
|---|---|---|---|
| `POST` | `/api/users/register` | Public | Registers a user and returns a JWT + the user |
| `POST` | `/api/users/login` | Public | Logs in with username or email and password |
| `GET` | `/api/users/me` | JWT | Returns the authenticated user |

Registration:

```json
{ "username": "player1", "email": "player1@example.com", "password": "secret123" }
```

Login uses the `email` field for both the email and the username:

```json
{ "email": "player1", "password": "secret123" }
```

### Games, favorites, comments and AI

| Method | Route | Access | Description |
|---|---|---|---|
| `GET` | `/api/games` | Public | Full catalog |
| `GET` | `/api/games/{id}` | Public | Game detail |
| `GET` | `/api/games/original` | Public | Connect Four (GameHub's original game) |
| `POST` | `/api/games/sync` | Public | Imports/updates the catalog from FreeToGame |
| `GET` | `/api/users/me/favorites` | JWT | The user's favorites |
| `POST` / `DELETE` | `/api/games/{id}/favorite` | JWT | Adds / removes a favorite |
| `GET` | `/api/games/{id}/comments` | Public | Comments of a game |
| `POST` | `/api/games/{id}/comments` | JWT | Creates a comment |
| `POST` | `/api/ai/find` | Public | AI game finder: takes `{ "query": "..." }` |

Example AI query:

```text
"I want a relaxing game to play with friends"
```

The backend picks up to 60 candidates from the catalog, asks Gemini, and returns the recommended games with the reason for each. If Gemini fails or no key is set, it answers with the local search.

## 🧪 Tests

```bash
cd backend
mvn clean test
```

34 tests cover authentication, persistence, controllers, FreeToGame and the AI finder. They use an in-memory database and do not sync the catalog. Without network access, use a local repository: `mvn -o -Dmaven.repo.local=.m2/repository test`.

## 🎯 What I Practiced

- Designing REST APIs and connecting a frontend to a backend.
- JWT authentication.
- Relational data modeling and persistence with JPA.
- Integrating external and AI APIs (prompts and structured responses).
- Error handling and testing with JUnit and Mockito.
- Frontend refactoring: shared layout, switchable themes and CSS variables.

## 📌 Project Context

GameHub began as a university frontend project and was redesigned and extended into a full-stack application with a backend, persistence, authentication, external APIs and AI.

## 👨‍💻 Author

**Francisco Parra** — Software Development student focused on full-stack development, frontend, QA and AI automation.
