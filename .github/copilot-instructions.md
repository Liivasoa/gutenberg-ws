# Copilot Repository Instructions

## Default Routing Policy

- For any non-trivial request, always delegate to `RootAgent` first.
- A request is considered non-trivial if it includes one or more of the following:
  - code changes
  - architecture decisions
  - refactoring
  - test strategy
  - migration or persistence work
  - multi-file analysis
- Always mention what agent you are delegating to when you do so, and provide a brief rationale for the delegation.
- After `Root Agent` is selected, it must delegate execution by task type and should not perform all implementation tasks itself:
  - `Test Agent` for test design, test implementation, and test strategy.
  - `Implementation Agent` for production code implementation.
  - `Refactoring Agent` for code analysis, architecture decisions, and refactoring.
  - `Db Agent` for Flyway migrations and persistence schema evolution.
- `Root Agent` may orchestrate, sequence, and validate, but execution work for the above task types must be delegated.

## Allowed Direct Handling (No Delegation)

- You may answer directly without delegating to `Root Agent` only when:
  - the user asks a short clarification question
  - the user asks for a very small informational answer
  - no code or file update is needed

## Fallback Behavior

- If `Root Agent` is unavailable, continue locally while still enforcing Root Agent rules and project constraints.
- When continuing locally, keep the same process discipline:
  - English-only outputs for project artifacts
  - strict TDD Red/Green/Refactor with test-first gate (no production code before failing tests)
  - mandatory stop after each TDD phase for user review/commit:
    - stop after Red
    - stop after Green
    - stop after Refactor
  - feature implementation order: Domain -> Application -> Persistence -> Controller (if needed)
  - commit discipline at each step

## Delegation Intent

- `Root Agent` is the default orchestrator for this repository.
- Specialized agents (`Implementation Agent`, `Test Agent`, `Refactoring Agent`, `Db Agent`) are selected by `Root Agent` by task type.
