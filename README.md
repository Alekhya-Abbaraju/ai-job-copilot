# AI Career Assistant 🚀

An AI-powered career management platform designed to help job seekers manage applications, analyze resumes, identify skill gaps, and improve their job search using Generative AI.

## Overview

AI Career Assistant is a full-stack application built using React, Java Spring Boot, PostgreSQL, and Generative AI. It uses a micro-frontend architecture to organize authentication, job management, and resume management into independent frontend modules.

The platform integrates AI-powered resume analysis to provide insights into resume content, skills, and potential improvements.

## Key Features

- **User Authentication:** Secure registration and login using JWT-based authentication and BCrypt password hashing.
- **Resume Management:** Upload and manage resumes with backend document text extraction using Apache Tika.
- **AI Resume Analysis:** Analyze resume content using Spring AI and locally hosted Ollama models.
- **Job Management:** Track job applications and manage their progress through different application statuses.
- **Job Matching:** Support job matching workflows to help users assess their suitability for opportunities.
- **Asynchronous Processing:** Use Apache Kafka for asynchronous resume-analysis processing.
- **Micro-Frontend Architecture:** Separate frontend modules for authentication, jobs, and resumes.

## Tech Stack

### Frontend
- React.js
- JavaScript / TypeScript
- Tailwind CSS
- Vite
- Module Federation

### Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- Spring AI
- Apache Kafka
- Apache Tika
- REST APIs

### Database & AI
- PostgreSQL
- Ollama
- Qwen 2.5 (3B)

### Security & Tools
- JWT Authentication
- BCrypt Password Hashing
- Git & GitHub
- Swagger / OpenAPI

## Architecture

The application follows a micro-frontend architecture with independently organized frontend modules communicating with Spring Boot backend services through REST APIs.

```text
ai-job-copilot/
│
├── backend/
│   ├── ai-job-copilot-backend/
│   ├── auth-service/
│   └── resume-service/
│
├── frontend/
│   ├── auth-mfe/
│   ├── frontend/
│   ├── jobs-mfe/
│   └── resume-mfe/
│
├── .gitignore
└── README.md
```

### Backend Services

| Service | Responsibility |
|---|---|
| Auth Service | User registration, login, and authentication |
| Resume Service | Resume uploads, text extraction, and AI analysis |
| Main Backend | Core application and job management functionality |

### Frontend Modules

| Module | Responsibility |
|---|---|
| Auth MFE | Authentication interface |
| Jobs MFE | Job application management |
| Resume MFE | Resume management and analysis |
| Frontend Host | Main application shell |

## Application Workflow

1. Users register or log in securely.
2. Users upload their resumes.
3. The backend extracts resume text using Apache Tika.
4. Resume analysis requests are processed asynchronously using Kafka.
5. Spring AI integrates with Ollama to generate resume insights.
6. Users can manage job applications and explore job-matching functionality.

## Getting Started

### Prerequisites

Make sure the following are installed:

- Java 21
- Maven
- Node.js and npm
- PostgreSQL
- Apache Kafka
- Ollama

### 1. Clone the Repository

```bash
git clone https://github.com/Alekhya-Abbaraju/ai-job-copilot.git
cd ai-job-copilot
```

### 2. Configure Environment Variables

Configure the required environment variables for the backend services.

Example:

```env
DB_PASSWORD=your_database_password
JWT_SECRET=your_jwt_secret
```

Use the actual database and JWT configuration required by each service. Never commit real credentials or secret values.

### 3. Start Ollama

Install Ollama and pull the model:

```bash
ollama pull qwen2.5:3b
```

Start the Ollama service before using AI-powered resume analysis.

### 4. Start PostgreSQL and Kafka

Create the application database and configure PostgreSQL and Kafka connection settings for the backend services.

### 5. Run Backend Services

Navigate to each Spring Boot service directory and run:

```bash
mvn spring-boot:run
```

Configure the service-specific ports and environment variables according to the application properties.

### 6. Run Frontend Modules

Navigate to each frontend module and install dependencies:

```bash
npm install
```

Start the frontend applications using their configured development scripts:

```bash
npm run dev
```

Refer to each module's `package.json` for its available scripts and port configuration.

## Future Enhancements

- Advanced AI-powered job recommendations.
- Improved resume-to-job matching.
- Skill-gap analysis and personalized learning recommendations.
- Enhanced automated testing.
- Docker-based deployment and AWS integration.

## Author

**Alekhya Abbaraju**

- GitHub: [Alekhya-Abbaraju](https://github.com/Alekhya-Abbaraju)
- Project Repository: [AI Career Assistant](https://github.com/Alekhya-Abbaraju/ai-job-copilot)

---

*Built to simplify job searching through full-stack engineering and Generative AI.*
