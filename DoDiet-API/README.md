# DoDiet API Server

Spring Boot implementation of the AI Diet App backend.

## Tech Stack
- Java 17
- Spring Boot 3.2.5
- PostgreSQL (Production) / H2 (Test)
- Spring Security + JWT
- Spring Data JPA

## Setup

### Prerequisites
1. JDK 17+ installed.
2. PostgreSQL installed and running (for production/dev profile).
   - Create database: `dodiet`
   - Username/Password: `postgres`/`postgres` (or update `application.yml`)

### Configuration
- Check `src/main/resources/application.yml` for configuration.
- Set environment variables for security:
  - `JWT_SECRET`
  - `GEMINI_API_KEY`
  - `DB_PASSWORD`

### Running the Server
```bash
./gradlew bootRun
```

### Running Tests
```bash
./gradlew test
```

## API Endpoints

### Auth
- POST `/api/auth/signup`
- POST `/api/auth/login`

### Meals
- POST `/api/meals/analyze` (Upload image)
- POST `/api/meals` (Save meal)
- GET `/api/meals` (Get history)

## Directory Structure
- `uploads/`: Local storage for uploaded images.
