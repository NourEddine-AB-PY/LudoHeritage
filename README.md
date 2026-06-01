# LudoHeritage

> An interactive web platform dedicated to preserving and exploring traditional board games from around the world — in French.

---

## What is LudoHeritage?

LudoHeritage is a full-stack web application built as a final-year project at **ENSIAS (Rabat)**. It brings together a curated catalog of 20 traditional board games with rich cultural context, interactive exploration tools, an AI assistant, and a community space — all in French.

---

## Features

| Feature | Description |
|---|---|
| **Game Catalog** | 20 traditional games with history, rules, mechanics and cultural value |
| **Interactive Map** | Explore games geographically on a world map |
| **Timeline** | Browse games on a historical timeline from Antiquity to modern times |
| **LudoBot** | AI assistant that answers questions and recommends games based on your profile |
| **Quiz** | 10-question quizzes with a live leaderboard |
| **Comparator** | Compare two games side by side |
| **Playable Awale** | Play the West African Awale directly in the browser |
| **Community Forum** | Post, like, and comment with other enthusiasts |
| **User Profile** | Favorites, history, and unlockable achievements |

---

## Tech Stack

**Backend**
- Java 21 + Spring Boot 3.2
- MongoDB (data persistence)
- Ollama (local AI engine powering LudoBot)
- Maven

**Frontend**
- React 18 + Vite
- Plain CSS

---

## Prerequisites

Make sure you have the following installed before running the project:

- [Java 21](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [Node.js 18+](https://nodejs.org/)
- [MongoDB](https://www.mongodb.com/try/download/community) running on `localhost:27017`
- [Ollama](https://ollama.com/) *(optional — LudoBot works without it using a fallback)*

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/ludoheritage.git
cd ludoheritage
```

### 2. Start the backend

```bash
mvn spring-boot:run
```

The server starts on **http://localhost:8080**. The game catalog is seeded into MongoDB automatically on first run.

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The app is available at **http://localhost:5173**.

### 4. (Optional) Enable LudoBot AI

Install a model with Ollama:

```bash
ollama pull llama3
```

LudoBot connects to Ollama automatically. If Ollama is not running, it falls back to catalog-based responses.

---

## Environment Variables

You can override defaults using a `.env` file at the project root:

```env
MONGODB_URI=mongodb://localhost:27017/ludoheritage
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama3
```

---

## Running the Tests

```bash
mvn test
```

**53 automated tests** — unit, integration, and stress — all pass in under 6 seconds.

```
Tests run: 53  |  Failures: 0  |  Errors: 0  |  BUILD SUCCESS
```

---

## Project Structure

```
ludoheritage/
├── src/
│   └── main/java/org/LudoHeritage/
│       ├── auth/          User accounts & authentication
│       ├── agent/         LudoBot & Ollama integration
│       ├── controller/    Game catalog REST API
│       ├── community/     Forum posts & comments
│       ├── quiz/          Quiz scores & leaderboard
│       ├── service/       Business logic & AI service
│       └── config/        CORS, security, error handling
├── frontend/
│   └── src/               React components & pages
├── rapport/               LaTeX project report
└── pom.xml
```

---

## The 20 Games

The catalog covers games from Africa, Asia, and Europe across three historical periods.

<details>
<summary>Show full list</summary>

| # | Game | Region | Period | Playable |
|---|---|---|---|---|
| 1 | Awale | West Africa | Ancient | ✅ |
| 2 | Mancala | East Africa | Ancient | — |
| 3 | Backgammon | Iran / Middle East | Ancient | — |
| 4 | Go | China | Ancient | — |
| 5 | Chess | India / Persia | Medieval | — |
| 6 | Draughts | France / Egypt | Medieval | — |
| 7 | Shogi | Japan | Medieval | — |
| 8 | Tablut | Scandinavia | Medieval | — |
| 9 | Royal Game of Ur | Mesopotamia | Ancient | — |
| 10 | Xiangqi | China | Medieval | — |
| 11 | Carrom | India / Sri Lanka | Modern | — |
| 12 | Fanorona | Madagascar | Medieval | — |
| 13 | Surakarta | Indonesia | Modern | — |
| 14 | Nine Men's Morris | Mediterranean | Ancient | — |
| 15 | Alquerque | Arab World | Medieval | — |
| 16 | Toguz Korgool | Kyrgyzstan | Ancient | — |
| 17 | Bagh Chal | Nepal | Ancient | — |
| 18 | Yote | West Africa | Ancient | — |
| 19 | Pachisi | India | Medieval | — |
| 20 | Hnefatafl | Scandinavia | Ancient | — |

</details>

---

## Authors

Developed by students of **Filière Ingénierie Informatique — ENSIAS Rabat**
Academic year 2024–2025
Supervised by **M. Hicham Salaheddine**
