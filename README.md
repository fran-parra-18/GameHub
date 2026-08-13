# 🎮 GameHub

A full-stack game platform originally developed as a university group project and later extended with a backend, persistent data storage, and AI-powered game analysis.

The application provides a game-platform experience where users can browse games, interact with game pages, play Connect Four, track their matches, and receive AI-generated insights about their gameplay.

---

## 🚀 Project Overview

GameHub was originally created as a university project focused on frontend development and user interfaces.

The original version was built using HTML, CSS, and JavaScript and included a complete game-platform interface, game catalog, user interface, shopping cart, comments, and a functional Connect Four game implemented with JavaScript and HTML Canvas.

For this version, I am extending the original project into a full-stack application by adding:

* A REST API backend
* Persistent data storage
* User and game management
* Match history
* Connect Four game persistence
* Player rankings
* AI-powered game analysis

The goal of this extension is to transform the original frontend project into a complete web application while preserving its original interface and game experience.

---

## ✨ Features

### 🎮 Game Platform

* Browse available games
* Search games
* Filter games by category
* View detailed game information
* Game ratings and interactions
* Responsive interface

### 🕹️ Connect Four

* Functional Connect Four game
* HTML Canvas-based game board
* Player turns
* Win detection
* Draw detection
* Match history
* Persistent game results
* Player statistics

### 👤 Users

* User registration
* User login
* User profiles
* Player statistics
* Match history

### 🛒 Shopping Cart

* Add games to cart
* Remove games from cart
* View cart contents
* Calculate total price
* Simulated checkout
* Persist cart information

> No real payments are processed. The checkout is simulated for demonstration purposes.

### 💬 Comments

* Add comments to games
* View existing comments
* Persist comments in the database
* Associate comments with users and games

### 🏆 Rankings

Players can be ranked according to their Connect Four performance.

Example statistics:

* Matches played
* Matches won
* Matches lost
* Win rate

### 🤖 AI Game Analysis

After completing a Connect Four match, users can request an AI-generated analysis of their gameplay.

The system sends relevant match information and player moves to an AI model, which can provide:

* Gameplay summary
* Strategic observations
* Mistakes or missed opportunities
* Strengths
* Suggestions for improvement

The AI feature is integrated into the application rather than being a standalone chatbot.

---

## 🏗️ Architecture

The application follows a client-server architecture.

```text
┌──────────────────────────┐
│                          │
│       GameHub Frontend   │
│                          │
│   HTML / CSS / JavaScript│
│                          │
└────────────┬─────────────┘
             │
             │ REST API
             │
┌────────────▼─────────────┐
│                          │
│      Spring Boot API     │
│                          │
│       Java Backend       │
│                          │
└─────────┬─────────┬──────┘
          │         │
          │         │
          ▼         ▼
┌──────────────┐  ┌─────────────────┐
│              │  │                 │
│  PostgreSQL  │  │    AI Service   │
│              │  │                 │
│ Persistent   │  │  Gemini API     │
│    Data      │  │                 │
│              │  │                 │
└──────────────┘  └─────────────────┘
```

---

## 🛠️ Technologies

### Frontend

* HTML5
* CSS3
* JavaScript
* HTML Canvas
* Responsive Web Design

### Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* REST API

### Database

* PostgreSQL

### Artificial Intelligence

* Google Gemini API

### Development Tools

* Git
* GitHub
* Maven
* IntelliJ IDEA / Visual Studio Code

---

## 🗄️ Database

The application uses PostgreSQL to persist information that was previously handled only on the client side.

The database includes entities such as:

```text
User
 ├── Comments
 ├── Matches
 └── Cart

Game
 ├── Comments
 └── Matches

Match
 └── Moves
```

### Main entities

#### User

Stores registered users and their basic information.

#### Game

Stores available games and their metadata.

#### Match

Stores completed Connect Four matches and their results.

#### Move

Stores the sequence of moves performed during a match.

#### Comment

Stores comments associated with games and users.

#### Cart

Stores the games currently associated with a user's shopping cart.

---

## 🔌 REST API

The backend exposes REST endpoints used by the frontend.

Example endpoint structure:

```text
/api/users
/api/games
/api/comments
/api/matches
/api/matches/{id}/moves
/api/rankings
/api/cart
/api/ai
```

Example requests:

```http
GET /api/games
GET /api/games/{id}
POST /api/matches
GET /api/matches/{id}
GET /api/rankings
POST /api/ai/analyze
```

The frontend communicates with the backend through HTTP requests and JSON responses.

---

## 🤖 AI Integration

One of the main goals of this project is to demonstrate practical AI integration inside a traditional web application.

The AI is used to analyze actual Connect Four matches generated by the application.

### Example flow

```text
Player finishes a match
        ↓
Frontend sends match information
        ↓
Spring Boot receives the request
        ↓
Backend retrieves match data
        ↓
Backend builds an AI prompt
        ↓
Gemini API analyzes the match
        ↓
AI response returned to backend
        ↓
Frontend displays the analysis
```

Example request:

```json
{
  "matchId": 42
}
```

The backend retrieves the corresponding moves and generates an analysis request.

Example response:

```json
{
  "summary": "The player controlled the center effectively during the opening.",
  "strengths": [
    "Good defensive positioning",
    "Effective center control"
  ],
  "suggestions": [
    "Look for double-threat opportunities earlier in the game."
  ]
}
```

---

## 🎯 Why AI?

The AI feature was designed around information already generated by the application.

Instead of adding a generic chatbot, the application provides the model with contextual information about an actual game and uses the model to generate useful feedback for the player.

This demonstrates how an AI model can be integrated as a service within a larger application.

---

## 🕹️ Connect Four Implementation

The Connect Four game was originally implemented using JavaScript and HTML Canvas.

The game contains logic for:

* Board rendering
* Player turns
* Piece placement
* Valid moves
* Horizontal win detection
* Vertical win detection
* Diagonal win detection
* Draw detection
* Game state management

The new version extends this functionality by connecting completed matches to the backend.

Instead of keeping the match only in browser memory:

```text
Game
 ↓
Match data
 ↓
REST API
 ↓
PostgreSQL
```

This allows matches to be retrieved later for statistics, rankings, and AI analysis.

---

## 📊 Player Statistics

The backend can calculate statistics from persisted matches.

Examples include:

```text
Matches played
Matches won
Matches lost
Win rate
```

These statistics can be used to build a player ranking.

Example:

| Player   | Matches | Wins | Win Rate |
| -------- | ------: | ---: | -------: |
| Player 1 |      20 |   15 |      75% |
| Player 2 |      18 |   11 |      61% |
| Player 3 |      25 |   13 |      52% |

---

## 📸 Screenshots

Screenshots of the application will be added here as the new full-stack version is completed.

Recommended screenshots:

1. Home page
2. Game catalog
3. Game details
4. Connect Four
5. Player profile
6. Ranking
7. AI game analysis
8. Shopping cart

---

## 🧪 Testing

Testing is performed at different levels depending on the component.

Planned tests include:

### Backend

* REST controller tests
* Service layer tests
* Repository tests
* Business logic tests

### Frontend

* Game logic testing
* API integration testing
* User interaction testing

### Integration

* Frontend ↔ Backend
* Backend ↔ PostgreSQL
* Backend ↔ AI service

---

## 🔐 Configuration

API keys and sensitive configuration values are not stored in the repository.

Environment variables are used for configuration.

Example:

```env
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
GEMINI_API_KEY=
```

A `.env.example` file will be provided with the required configuration structure.

---

## ▶️ Running the Project

### Prerequisites

Make sure you have installed:

* Java 17+
* Maven
* Node.js
* PostgreSQL
* Git

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/gamehub-fullstack.git
cd gamehub-fullstack
```

### 2. Configure PostgreSQL

Create a database:

```sql
CREATE DATABASE gamehub;
```

Configure the database credentials in the backend environment variables.

### 3. Configure the AI API

Create a Gemini API key and configure:

```env
GEMINI_API_KEY=your_api_key
```

### 4. Start the backend

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

### 5. Start the frontend

Open the frontend project and start it using the appropriate local development server.

The frontend will communicate with the Spring Boot API.

---

## 📁 Project Structure

The repository is organized approximately as follows:

```text
gamehub/
│
├── frontend/
│   ├── assets/
│   ├── css/
│   ├── js/
│   ├── pages/
│   └── index.html
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── ...
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
├── database/
│   └── ...
│
├── .env.example
├── .gitignore
└── README.md
```

---

## 📚 Project Background

The original GameHub application was developed as part of a university group project.

The original repository can be found here:

**Original project:**
https://github.com/TomasTourn/interfaces-grupo-47/tree/main/TP4/EntregaFinal

The current repository is an independent extension of that work.

The original project focused primarily on frontend development and user interface implementation.

This version focuses on expanding the application into a full-stack system with:

* Backend development
* Database persistence
* REST APIs
* Data modeling
* AI integration
* Game analytics

---

## 👨‍💻 My Contributions

The original application was developed collaboratively as a university project.

Because the original project was developed as a group, this repository does not claim individual ownership of every part of the original implementation.

My work in the current version focuses on extending the application with new functionality, including:

* Backend architecture
* REST API development
* PostgreSQL database integration
* Data persistence
* Match persistence
* Player statistics
* Ranking functionality
* AI-powered game analysis
* Frontend integration with the backend
* Deployment and production configuration

Specific features and contributions from the original university project are described as part of the project's historical context rather than being attributed individually.

---

## 🔮 Future Improvements

Possible future improvements include:

* Real-time multiplayer
* WebSocket communication
* OAuth authentication
* Advanced player profiles
* Improved AI game analysis
* AI-generated training exercises
* More games
* Game recommendations based on user activity
* Automated CI/CD pipeline
* Docker-based deployment
* Automated integration tests

---

## 🎓 Academic Context

This project originated as a university assignment focused on web interfaces and interactive applications.

The current version is being developed as a personal technical extension to explore:

* Full-stack development
* RESTful API design
* Relational database modeling
* AI integration
* Software architecture
* Application deployment

---

## 📄 License

This project is intended primarily as a portfolio and educational project.

The original project was developed as part of a university group assignment. Please refer to the original repository for additional information regarding its academic context.

---

## 👋 About

GameHub is a project focused on combining traditional web development with modern AI capabilities.

It demonstrates the evolution of a frontend-focused university project into a full-stack application with persistent data, backend services, and AI-powered functionality.
