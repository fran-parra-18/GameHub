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

GameHub uses JWT-based authentication.

After logging in, the backend returns a token that the frontend stores locally and sends with protected requests:

```text
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
