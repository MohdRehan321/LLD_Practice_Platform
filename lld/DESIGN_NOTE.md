# DESIGN NOTE

## 1. MVP Scope & User Flow

The MVP is a modular monolith built with Spring Boot and PostgreSQL. The user flow is asynchronous to ensure high availability:

- User submits a text-based LLD solution via POST /submissions.
- System saves the submission, returns a 202 Accepted, and queues a background task.
- User polls GET /evaluations/{id} to see state transitions (PENDING → EVALUATING → COMPLETED) and eventually retrieves the structured AI feedback.

## 2. Important Classes & Interfaces

**Submission vs. Evaluation:** These are strictly separated entities. Submission owns the learner's payload, while Evaluation owns the state and feedback. This ensures one component's failure does not corrupt the other.

**Evaluator (Interface):** Exposes EvaluationResult evaluate(Submission, Problem, Rubric).

**AiEvaluatorImpl:** Implements the Evaluator interface using Java's native HttpClient to call OpenAI.

## 3. Evaluation Approach

We enforce deterministic AI feedback by utilizing a fixed rubric and OpenAI's json_object response format. The prompt explicitly demands an array of feedback items, each containing a score, evidence, concern, and suggestion.

## 4. Key Trade-offs & Engineering Decisions

**Native HttpClient vs. AI Frameworks:** Traded the convenience of LangChain4j/Spring AI for Java's native HttpClient to minimize dependencies and retain absolute control over the JSON schema mapping.

**Asynchronous Processing:** Traded synchronous simplicity for an @Async bounded ThreadPoolTaskExecutor. This prevents the application from blocking HTTP threads while waiting 15 seconds for an OpenAI response.

**Database Transaction Isolation:** To avoid connection pool exhaustion, the background thread fetches data, closes the transaction, calls the LLM, and opens a new transaction to save the results.