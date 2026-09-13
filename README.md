# LLD Practice Platform MVP

A backend service that allows learners to submit Low-Level Design (LLD) solutions and receive asynchronous, structured AI feedback based on a fixed rubric.

## Prerequisites
* Java 21
* Maven
* PostgreSQL running on port 5432

## Setup Instructions

1. **Database Setup**
   Create a local PostgreSQL database:
   `CREATE DATABASE lld_practice;`

2. **Environment Variables**
   Before running the application, you must provide the following environment variables to your IDE or terminal:
   * `DB_USERNAME` (Your PostgreSQL username)
   * `DB_PASSWORD` (Your PostgreSQL password)
   * `OPENAI_API_KEY` (Your OpenAI API key)

  ### 3. Run the Application

```bash
mvn spring-boot:run
```
Note: Hibernate will automatically generate the required database tables on startup (ddl-auto=update).

**Seed Dummy Data**
Run the following SQL in your PostgreSQL database to create a dummy problem and an attempt so you have something to submit against:

```bash
INSERT INTO problems (id, title, description, rubric_payload) 
VALUES ('11111111-1111-1111-1111-111111111111', 'Parking Lot', 'Design a parking lot.', 'Fixed Rubric');

INSERT INTO attempts (id, user_id, problem_id, started_at) 
VALUES ('22222222-2222-2222-2222-222222222222', 'user-1', '11111111-1111-1111-1111-111111111111', NOW());
```

**Testing the API**
1. Submit a Solution
```bash
curl -X POST http://localhost:8080/api/v1/attempts/22222222-2222-2222-2222-222222222222/submissions \
-H "Content-Type: application/json" \
-d '{"content": "My design uses a Strategy pattern...", "format": "TEXT"}'
```
(Returns 202 Accepted with the Evaluation ID)

3. Check Evaluation Status & Feedback
```
curl http://localhost:8080/api/v1/evaluations/<EVALUATION_ID_FROM_PREVIOUS_STEP>
```
