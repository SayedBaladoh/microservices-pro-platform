# Contributing to microservices-pro-platform

This repository is a training platform, not an open-source project — but the
same discipline applies. Follow these rules for every session's work.

## Branch model

- `main` — your (the trainee's) working branch. Every session's lab work
  lands here.
- `reference` — the instructor's canonical implementation. **Do not merge
  or check this branch out into `main`.** Use it only as a read-only
  reference when you're stuck (see `docs/setup/github-workflow.md`).

## Before you commit

1. Run `mvn test` in every module you touched. All tests must pass.
2. Make sure you didn't leave any `TODO` unresolved that your lab asked you
   to complete.
3. Make sure you didn't accidentally add infrastructure or services from a
   future session. If you're not sure what's in scope, check
   `docs/architecture/platform-overview.md` — it lists exactly what has
   been taught so far.

## Commit message format (enforced)

```
session-NN: short-description-of-what-was-done
```

Trainees who submit without this format will be asked to re-commit before
grading.

## Code style

- Match the package structure already in the repo: `com.microservices.pro.<module>`
- Keep methods small and named after what they do — no cleverness contests.
- Don't add dependencies that aren't needed for the current session's lab.
  If Session 2's lab doesn't need it, it doesn't belong in this PR.

## Dev vs production config

Any configuration that's convenient for local development but wrong for
production must be commented as such, e.g.:

```yaml
enable-self-preservation: false  # DEV ONLY — see comment in file for why
```

This is a course-wide standard (CS-08), not a suggestion.
