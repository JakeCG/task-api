# Task Management System

A full-stack task management application built with Spring Boot (backend) and TypeScript/Express/Nunjucks (frontend), following GOV.UK Design System patterns.

## Table of Contents

- [Architecture](#architecture)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Docker Compose Setup](#docker-compose-setup)
- [Quick Start](#quick-start)
- [Development Setup](#development-setup)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Environment Variables](#environment-variables)
- [Testing](#testing)
- [Code Quality](#code-quality)
- [Contributing](#contributing)

## Architecture

This is a monolithic application with clear separation between frontend and backend components:

- **Backend**: Spring Boot REST API (Java 21) with OpenAPI 3.0 documentation
- **Frontend**: Express.js with TypeScript and Nunjucks templates
- **Database**: PostgreSQL
- **API Documentation**: Swagger UI with interactive testing
- **Containerization**: Docker & Docker Compose

## Features

- Create, read, update, and delete tasks
- Task properties: title, description, status, due date/time
- RESTful API design with comprehensive OpenAPI documentation
- Interactive API documentation with Swagger UI
- GOV.UK Design System compliance
- Responsive user interface
- PostgreSQL database with Flyway migrations
- Docker containerization
- Comprehensive error handling with RFC 7807 Problem Details

## Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Node.js 18+ (for local development)
- PostgreSQL (for local development)

## Docker Compose Setup

The entire application stack can be built and run using Docker Compose from the root directory. This is the recommended approach for getting started quickly.

### Build and Run All Services

From the root directory of the project:

```bash
docker-compose up --build
```

This command will:
- Build the backend Spring Boot application
- Build the frontend TypeScript/Express application
- Start a PostgreSQL database
- Set up networking between all services
- Make the application available on the configured ports

### What Gets Started

When you run `docker-compose up --build`, the following services will be started:

- **PostgreSQL Database**: Internal database service
- **Backend API**: Spring Boot application on port 8080
- **Frontend Web App**: Express.js application on port 3000

### Stopping Services

To stop all services:

```bash
docker-compose down
```

To stop and remove all containers, networks, and volumes:

```bash
docker-compose down -v
```

## Quick Start

## Quick Start

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd task-management
   ```

2. **Start all services with Docker Compose:**
   ```bash
   docker-compose up --build
   ```

3. **Access the application:**
  - **Frontend**: http://localhost:3000
  - **Backend API**: http://localhost:8080/api
  - **API Documentation**: http://localhost:8080/swagger-ui.html
  - **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## Development Setup

### Backend Development

1. **Navigate to the backend directory:**
   ```bash
   cd backend
   ```

2. **Build the application:**
   ```bash
   ./gradlew build
   ```

3. **Run tests:**
   ```bash
   ./gradlew test
   ```

4. **Run integration tests:**
   ```bash
   ./gradlew integration
   ```

5. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

### Frontend Development

1. **Navigate to the frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   yarn install
   ```

3. **Build assets:**
   ```bash
   yarn build
   ```

4. **Run in development mode:**
   ```bash
   yarn start:dev
   ```

5. **Run tests:**
   ```bash
   yarn test
   ```

## API Documentation

The API is fully documented using OpenAPI 3.0 specification. Access the interactive documentation at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Available Endpoints

| Method   | Endpoint                      | Description                              |
|----------|-------------------------------|------------------------------------------|
| `POST`   | `/api/tasks/create-task`      | Create a new task                        |
| `GET`    | `/api/tasks/get-all-tasks`    | Get all tasks (ordered by creation date) |
| `GET`    | `/api/tasks/{id}/get-task`    | Get a specific task by ID                |
| `PUT`    | `/api/tasks/{id}/update-task` | Update all fields of a task              |
| `PATCH`  | `/api/tasks/{id}/status`      | Update only the task status              |
| `DELETE` | `/api/tasks/{id}/delete-task` | Delete a task                            |

> **Note**: The API uses explicit action-based endpoints (e.g., `/create-task`, `/get-task`) rather than RESTful resource-based endpoints. This approach provides clearer intent for a smaller project such as this.

### Example API Usage

**Create a task:**
```bash
curl -X POST http://localhost:8080/api/tasks/create-task \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Review case documents",
    "description": "Review all documents for case #12345",
    "status": "TODO",
    "dueDateTime": "2024-12-31T17:00:00"
  }'
```

**Update task status:**
```bash
curl -X PATCH http://localhost:8080/api/tasks/1/status?status=IN_PROGRESS
```

## Project Structure

```
task-management/
├── backend/                 # Spring Boot API with OpenAPI documentation
│   ├── src/
│   ├── build.gradle
│   └── Dockerfile
├── frontend/               # TypeScript/Express frontend
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml
└── README.md
```

## Environment Variables

### Backend

| Variable      | Description       | Default     |
|---------------|-------------------|-------------|
| `DB_HOST`     | Database host     | `localhost` |
| `DB_PORT`     | Database port     | `5432`      |
| `DB_NAME`     | Database name     | `taskdb`    |
| `DB_USER`     | Database user     | `postgres`  |
| `DB_PASSWORD` | Database password | `postgres`  |

### Frontend

| Variable       | Description     | Default                   |
|----------------|-----------------|---------------------------|
| `API_BASE_URL` | Backend API URL | `http://backend:8080/api` |
| `PORT`         | Frontend port   | `3000`                    |

## Testing

### Backend Tests

```bash
cd backend
./gradlew test            # Unit tests
./gradlew integration     # Integration tests
```

### Frontend Tests

```bash
cd frontend
yarn test                 # Unit tests
yarn test:routes         # Route tests
yarn test:coverage       # Test coverage
```

### API Testing with Swagger UI

1. Start the application using Docker Compose
2. Navigate to http://localhost:8080/swagger-ui.html
3. Use the interactive interface to:
  - Explore all available endpoints
  - View request/response schemas
  - Test API calls directly from the browser
  - See example requests and responses

## Code Quality

### Backend

```bash
cd backend
./gradlew check          # Run all checks
./gradlew jacocoTestReport  # Generate coverage report
```

### Frontend

```bash
cd frontend
yarn lint                # Lint code
yarn lint:fix           # Fix linting issues
```

## Contributing

1. Create a feature branch
2. Make your changes
3. Run tests and linting
4. Submit a pull request

---
