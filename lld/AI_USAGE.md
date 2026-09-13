# AI Usage and Design Decisions

This document outlines the meaningful architectural decisions made with the assistance of AI during the development of the LLD Practice Platform, including one suggestion that was actively rejected to better align with the MVP constraints.

### 1. Managing LLM Output Format (Accepted)
* **What the AI suggested:** Using OpenAI's native `response_format: { "type": "json_object" }` combined with a strict JSON schema in the system prompt.
* **Why I accepted it:** The assignment guide recommended structured feedback (criterion → score → evidence → concern). Asking the LLM for a free-form string and trying to parse it via regex is brittle. Forcing strict JSON at the API level ensures the `AiEvaluatorImpl` can deserialize the response safely into the internal DTO records without parsing errors.

### 2. Decoupling Services via Application Events (Rejected)
* **What the AI suggested:** Using Spring Application Events (`@EventListener`) to decouple the `SubmissionService` from the `EvaluationService` when triggering the background evaluation.
* **Why I rejected it:** While elegant, it adds unnecessary abstraction for a 2-day MVP. The guide explicitly warns against adding design patterns just to show pattern knowledge. A direct method call to an `@Async` service keeps the code traceable and perfectly fulfills the asynchronous requirement without over-engineering.

### 3. Async Thread Pool Configuration (Accepted)
* **What the AI suggested:** Overriding Spring's default `@Async` behavior with a custom, bounded `ThreadPoolTaskExecutor` (Core pool: 2, Max: 10).
* **Why I accepted it:** Spring's default `SimpleAsyncTaskExecutor` creates a new thread for every task and does not reuse them. If multiple users submit concurrently, the system would quickly run out of memory or overwhelm the OpenAI API rate limits. Implementing a bounded queue demonstrates practical scaling judgment and resource protection.

### 4. Database Transaction Boundaries (Accepted)
* **What the AI suggested:** Passing only the `evaluationId` to the background thread instead of the full JPA entity, and avoiding `@Transactional` on the long-running LLM network call.
* **Why I accepted it:** Keeping a database transaction open while waiting 10-20 seconds for an OpenAI response will rapidly exhaust the database connection pool. By completely removing `@Transactional` from the async method and using short, isolated repository saves, the database remains highly available during long evaluations.
