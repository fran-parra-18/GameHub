# 🎮 GameHub

Full-stack gaming platform that combines a responsive game catalog, user authentication, persistent favorites and comments, external API integration, and AI-powered game recommendations.

The project was originally developed as a frontend gaming website and later expanded into a complete full-stack application with a Java Spring Boot backend.


## ✨ Features

* 🎮 Dynamic game catalog
* 🔄 Game synchronization with the FreeToGame API
* 🔐 User registration and authentication with JWT
* 👤 Protected user profile
* ❤️ Persistent favorites
* 💬 User comments
* 🤖 AI-powered Game Finder using Google Gemini
* 🎯 Personalized game recommendations
* 🕹️ Playable Connect Four game
* 📱 Responsive frontend
* 🗄️ Persistent data storage

## 🛠️ Tech Stack

### Frontend

* HTML5
* CSS3
* JavaScript
* Fetch API
* LocalStorage

### Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Maven
* JWT Authentication
* REST API

### Database

* H2 for local development
* PostgreSQL configuration available

### APIs & AI

* FreeToGame API
* Google Gemini API

### Testing

* JUnit 5
* Mockito
* Spring Boot Test

## 🏗️ Architecture

```text
Browser
   │
   │ HTTP / JSON
   ▼
Frontend
HTML + CSS + JavaScript
   │
   │ REST API
   ▼
Spring Boot Backend
   │
   ├── Authentication
   ├── Games
   ├── Comments
   ├── Favorites
   └── AI Game Finder
   │
   ▼
Database
H2 / PostgreSQL

External integrations:
   ├── FreeToGame API
   └── Google Gemini
```

## 📁 Project Structure

```text
GameHub/
│
├── backend/
│   ├── src/
│   └── pom.xml
│
├── css/
├── js/
├── Images/
├── Iconos/
├── NumberBlocks/
│
├── index.html
├── game.html
├── game-detail.html
├── favorites.html
├── login.html
└── register.html
```

## 🔐 Authentication

- JDK 21 o superior.
- Maven 3.9 o superior.
- PostgreSQL únicamente si se utiliza el perfil `postgres`.
- Acceso a Internet para sincronizar FreeToGame o utilizar Gemini.

## Configuración

El backend usa H2 en un archivo local (`backend/data/gamehub`), por lo que no requiere instalar una base de datos y los usuarios, favoritos, comentarios y el catálogo se conservan entre reinicios. Para empezar de cero basta con borrar la carpeta `backend/data`.

Variables de entorno disponibles:

| Variable | Uso | Valor predeterminado |
|---|---|---|
| `JWT_SECRET` | Clave utilizada para firmar los JWT | Clave local de desarrollo |
| `JWT_EXPIRATION_MS` | Duración del JWT en milisegundos | `86400000` (24 horas) |
| `GEMINI_API_KEY` | Clave de acceso a Gemini | Vacía (se usa la búsqueda local) |
| `GEMINI_MODEL` | Modelo utilizado por AI Game Finder | `gemini-flash-latest` |
| `CATALOG_SYNC_ON_STARTUP` | Importa FreeToGame al iniciar si el catálogo está vacío | `true` |
| `H2_URL` | URL JDBC de H2 (por ejemplo `jdbc:h2:mem:gamehub` para una base en memoria) | `jdbc:h2:file:./data/gamehub` |
| `CORS_ALLOWED_ORIGINS` | Orígenes autorizados para consumir la API | Puertos locales 8080 y 5500 |
| `DATABASE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/gamehub` |
| `DATABASE_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DATABASE_PASSWORD` | Contraseña de PostgreSQL | `postgres` |

Para ambientes reales se debe definir un `JWT_SECRET` largo y privado. Las claves y contraseñas no deben incorporarse al repositorio.

## Ejecución local

Un solo comando levanta todo: el backend también sirve el frontend.

```bash
cd backend
mvn spring-boot:run
```

Luego abrir `http://localhost:8080`.

- Maven copia el frontend (HTML, CSS, JS e imágenes de la raíz del proyecto) dentro de la aplicación en cada arranque. Si modificás archivos del frontend, reiniciá el backend para verlos.
- Al iniciar, si el catálogo está vacío, se importa automáticamente desde FreeToGame (requiere Internet). Si falló o no hay conexión, la home muestra el botón **Importar catálogo**; también se puede hacer a mano:

```bash
curl -X POST http://localhost:8080/api/games/sync
```

  La sincronización realiza un *upsert* por el identificador externo de FreeToGame, por lo que repetirla no crea duplicados. Si FreeToGame no está disponible, el backend sigue funcionando y conserva el catálogo existente.
- La consola de H2 está en `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/gamehub`, usuario `sa`, contraseña vacía; apagá el backend antes de abrir el archivo desde otra herramienta).
- Alternativa: servir el frontend con otro servidor estático (por ejemplo `python -m http.server 5500`) y abrir `http://localhost:5500`; la API se sigue consumiendo desde `http://localhost:8080`. No se recomienda abrir los HTML con `file://`.

## PostgreSQL

Para ejecutar el backend con PostgreSQL:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Antes de iniciarlo se deben configurar `DATABASE_URL`, `DATABASE_USERNAME` y `DATABASE_PASSWORD` según el entorno.

## API REST

### Usuarios y autenticación

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/users/register` | Público | Registra un usuario y devuelve JWT + usuario seguro |
| `POST` | `/api/users/login` | Público | Inicia sesión con username o email y contraseña |
| `GET` | `/api/users/me` | JWT | Devuelve el usuario autenticado |

Ejemplo de registro:

```json
{
  "username": "player1",
  "email": "player1@example.com",
  "password": "secret123"
}
```

El login utiliza el campo `email` para recibir tanto el email como el nombre de usuario:

```json
{
  "email": "player1",
  "password": "secret123"
}
```

Los endpoints protegidos esperan el encabezado:

```http
Authorization: Bearer <token>
```

Authenticated users can access features such as favorites, comments and profile information.

## 🎮 Game Catalog

The backend can synchronize game information from the FreeToGame API.

This allows the application to work with real game data instead of maintaining the entire catalog manually.

## 🤖 AI Game Finder

GameHub includes an AI recommendation feature powered by Google Gemini.

Users can describe the kind of game they want to play and the backend uses AI to select appropriate games from the available catalog.

Example:

```text
"I want a relaxing multiplayer game that I can play with friends."
```

The AI returns matching games together with a recommendation reason.

## ❤️ Favorites

Authenticated users can:

* Add games to their favorites
* Remove games from their favorites
* Retrieve their saved games
* View favorites from their profile

Favorites are stored persistently in the backend.

## 💬 Comments

Users can read comments associated with games.

Authenticated users can also create comments, allowing interaction around the game catalog.

## 🚀 Running the Project

### Requirements

* Java 21+
* Maven
* A modern web browser

### Backend

Navigate to the backend directory:

```bash
cd backend
```

Run the application:

```bash
mvn spring-boot:run
```

The backend runs locally on:

```text
http://localhost:8080
```

### Frontend

Serve the root directory using a local web server such as VS Code Live Server.

Example:

```text
http://127.0.0.1:5500
```

## 🧪 Tests

Run the backend test suite with:

```bash
cd backend
mvn clean test
```

The project includes tests for authentication, persistence, controllers and application behavior.

## 🎯 What I Practiced

This project gave me hands-on experience with:

* Building REST APIs
* Connecting frontend and backend applications
* JWT authentication
* Relational data modeling
* API integration
* AI API integration
* Prompt design and structured AI responses
* Error handling
* HTTP requests with JavaScript
* Backend testing with JUnit and Mockito
* Full-stack application architecture

## 📌 Project Context

GameHub began as a university frontend project and was later redesigned and extended into a full-stack application.

The goal of the extension was to transform a static interface into a functional application with a backend, persistence, authentication, external APIs and artificial intelligence.

## 👨‍💻 Author

**Francisco Parra**

Software Development student focused on full-stack development, frontend development, QA and AI automation.
