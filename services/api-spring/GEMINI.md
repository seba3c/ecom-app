# GEMINI.md

## Instructional Context
This file provides foundational mandates for the **Gemini CLI** agent. These instructions take absolute precedence over general defaults.

## Project Details & Technical Reference
For comprehensive details on architecture, build commands, testing strategies, and coding conventions, **refer to [AGENTS.md](./AGENTS.md)**. Gemini CLI MUST read and adhere to the standards defined therein.

## Core Mandates & AI-Driven Workflow
- **AI-First Implementation:** This project aims for 100% AI-generated code. Gemini CLI is expected to autonomously implement features, fixes, and tests based on high-level directives.
- **Verification:** Always verify changes by running the relevant Gradle tasks (`./gradlew build`, `./gradlew test`) as specified in `AGENTS.md`.
- **Consistency:** Maintain the established N-tier architecture (Controller-Service-Repository) and DTO mapping patterns (ModelMapper).
- **Skills & Extensions:** This project uses specialized skills (see `.agents/skills/`). Gemini CLI should utilize `activate_skill` when relevant specialized guidance is available.
- **Documentation:** Keep `AGENTS.md` updated if any architectural changes or new global conventions are introduced.

---
*Note: This file is a bridge between Gemini CLI's core mandates and the project-specific technical documentation in `AGENTS.md`.*
