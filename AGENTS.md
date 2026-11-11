## Core Directives

- Always apply humanize style: minimal words, zero fluff, zero emojis, zero em dashes.
- On every prompt, call skill tool with ID `humanize` before answering.
- Every subagent prompt must call or enforce `humanize` from the start.
- Always use my git and github account for git and github operations.
- Give important links in response when necessary.

## The Canonical 5-Step Lifecycle

Every task (features, bug fixes, refactors, chores) strictly follows this 5-step flow. No alternate or skipped flows:
`scout` -> `spec` -> `build` -> `review` -> `launch`

### 1. scout (Main Thread)
- Inspect codebase first. Never ask questions the code already answers.
- Interview user via `question` tool with high-signal questions.
- Multi-choice with `(Recommended)` on the top option. User can type custom answers.
- Settle destination, trade-offs, and scope boundaries before moving forward.

### 2. spec (Subagent)
- Runs in a child subagent (`general`) with `humanize` enforced.
- Produces lean technical specification and vertical slice DAG.
- Displays plan to user in main thread; refines with user feedback until approved.

### 3. build (Subagents)
- Runs in child subagents (`general`) per slice with `humanize` enforced.
- Implements minimal code, writes test-first checks at seams, creates atomic commits.
- Main thread stays clean: outputs only 1 line per slice (`Slice complete: <Name> (<SHA>)`).

### 4. review (Subagent)
- Runs in a child subagent (`general`) immediately after build with `humanize` enforced.
- Combines test verification (seams, edge cases, green suite) and code review (quality, performance, security).
- Loops with build autonomously on findings. Hard cap of 15 iterations to prevent runaway infinite loops.

### 5. launch (Subagent)
- Runs in a child subagent (`general`) once review passes with 0 blockers.
- Verifies tests and git status, pushes branch, manages PR via `.pr_description.md` (UTF-8).
- Reports only the final PR link to the main thread.

## Core Engineering Principles (Karpathy Guidelines)

1. **Think Before Coding:** Never assume or hide confusion. Surface trade-offs in `scout` before writing code.
2. **Simplicity First:** Minimum code that solves the problem. No speculative abstractions, no unrequested configurability.
3. **Surgical Changes:** Touch only what you must. No drive-by refactoring of unrelated code. Clean up your own mess.
4. **Goal-Driven Execution:** Transform tasks into verifiable criteria. Test first and loop autonomously until verified.

## Code

- Android app in Kotlin and Jetpack Compose, split into Gradle modules (`app`, `domain`, `data`,
  `presentation-core`, `source-api`, ...).
- Comment only a non-obvious why: a constraint, a workaround or a decision the code can't show, as short as it can
  be. Short KDoc is fine on any declaration whose purpose its name doesn't make clear. No comments that restate the
  code, no section banners, no TODOs without an issue.
- No speculative abstractions, settings or "just in case" error handling.
- Every file holding substantial code copied from elsewhere names its source, copyright holders and licence in
  its header, wherever the code lands: `// From <repo url>` for a verbatim copy or `// Adapted from <repo url>` for
  code that was modified, split or taken partially, followed by `// Copyright <holders>, <licence>`. A source file
  that already has such a header keeps it. Licence texts reach users through the app's licences screen, not the
  repo.

## Scope

- Stay on the task and match the surrounding code. Report unrelated issues instead of fixing them.
- Don't touch `gradle/` unless asked.

## Rules

### 1. Main Thread Cleanliness
- The primary thread is strictly an interactive orchestrator.
- Heavy investigation, planning, implementation, review, and PR publishing run in child subagents.

### 2. Branching & Worktree
- Every task belongs to one working branch.
- Use semantic kebab-case branch names (`feat/*`, `fix/*`, `chore/*`, `refactor/*`, `test/*`, `docs/*`).
- Never prefix branches with `t3/*` or tool prefixes. Never work on default branches.
- Stay on the branch until the task is complete.

### 3. Commits & Verification
- Atomic commits per slice: `<type>(<scope>): <what this enables>`.
- One concern per commit; each compiles on its own.
- Zero emojis, zero em dashes or en dashes.
- Verify changes with real tests or builds. Never claim a check passed without running it.
- Run `./gradlew check` before committing and fix what fails; never skip or disable a check. If you can't run it,
  stop and tell the user.

### 4. PR Lifecycle & PowerShell Safety
- Write PR descriptions to `.pr_description.md` (UTF-8) before calling `gh pr create` or `gh pr edit`.
- Format: `## Overview`, `## Key Changes`, `## Verification`, `## Notes`.
- End the description with a collapsed summary of the prompt: what the user asked for and the decisions they made
  along the way.
- Delete `.pr_description.md` immediately after PR creation or update.

### 5. Process & Port Safety
- 9router runs on port 20128 (`node`). Never terminate port 20128 or its process.
- Never run blanket process kills (`Stop-Process -Name node`, `pkill node`). Kill only specific hung PIDs.

### 6. UI & Design System
- Strictly adhere to `DESIGN.md` (dark charcoal `#08090b` canvas, `#121316` cards, solid matte surfaces, tabular numbers, zero vanity fluff).

## AI disclosure

Attribute every commit by where its ideas came from, not who typed or reviewed it. A change has four elements:

- **Cause**: what is wrong, or the concrete behavior needed. Restating the request doesn't count.
- **Approach**: how the change solves it.
- **Design**: structure and where code lives.
- **Correctness**: bugs, edge cases or constraints caught before the change was final.

The user supplied an element only if they gave its content or the reason that decides it, e.g. "put it in the
cache, not the provider". Requests, approvals, rejections without a reason, and naming or style nits don't count.

| Tier | When | Author | Trailer |
| --- | --- | --- | --- |
| 1 | User supplied the cause or the approach | User | none |
| 2 | Agent supplied both; user supplied another element | Agent | `Co-authored-by: <user> <email>` |
| 3 | User supplied no element | Agent | none |

- Agent author: `git commit --author="<agent name> <agent noreply email>"`, with your own name and no-reply
  address. Never use the user's identity as author for tiers 2 and 3.
- Decide the tier yourself and apply it; don't announce it or ask the user to confirm it. If unsure, use the lower
  one. Re-check it when amending. Never amend pushed commits.
