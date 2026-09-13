# RESEARCH NOTE

## 1. The Learner Problem

Practicing Low-Level Design (LLD) is fundamentally different from practicing Data Structures and Algorithms (DSA). While DSA platforms (like LeetCode or HackerRank) can evaluate solutions deterministically using unit tests and time-complexity benchmarks, LLD is highly subjective. When a learner designs a Parking Lot or a Vending Machine, there is no single "correct" answer. The challenge lies in evaluating trade-offs, class responsibilities, coupling, and extensibility. Currently, learners practice in isolation or rely on peer review, which is slow, inconsistent, and often unavailable.

## 2. Existing Approaches & Key Gaps

### Static Reference Solutions

Many platforms provide a single "ideal" solution.

**Gap:** This discourages creative problem-solving and fails to explain why the learner's alternate approach might also be valid or where it specifically fails.

### Unconstrained LLM Chatbots

Learners paste code into ChatGPT asking, "Is this good?"

**Gap:** The feedback is unstructured, highly variable, and often focuses on syntax rather than domain boundaries.

## 3. Product Direction (The MVP)

The MVP focuses purely on the core practice loop:

**Choose problem → Submit text design → Get structured AI feedback → Review history.**

Rather than forcing the user to use a clunky built-in diagramming tool, the MVP accepts structured text/pseudo-code. To solve the evaluation gap, the platform uses a Fixed Rubric Evaluation Engine. Instead of asking an LLM for an arbitrary score, the LLM is constrained to output strict JSON evaluating specific criteria (Coupling, Responsibilities, Extensibility) based on concrete evidence in the learner's text.