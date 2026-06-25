# GitHub Workflow

## Branches in this repository

- **`main`** — your working branch. Every session's lab lands here.
- **`reference`** — the instructor's complete, working implementation of
  everything currently in scope (Sessions 1–2). Read-only for you.

## Using the `reference` branch when you're stuck

**Do not merge or `git checkout` the `reference` branch into `main`.** Your
learning comes from writing the code yourself. Use it only to compare.

Compare a whole module:

```bash
git fetch origin
git diff main origin/reference -- services/product-service/
```

View a single file in `reference` without switching branches:

```bash
git show origin/reference:services/product-service/src/main/java/com/microservices/pro/productservice/ProductService.java
```

## Commit workflow

```bash
git status
git diff
git add .
git commit -m "session-01: add-product-service-eureka-config"
git push origin main
```

## Commit message format (enforced)

```
session-NN: short-description-of-what-was-done
```

Trainees who submit without this format will be asked to re-commit before
grading.

## If your trainer uses pull-request submission

1. Fork this repository to your own account.
2. Push your `main` branch commits to your fork.
3. Open a pull request from `your-fork:main` to the classroom repository's
   designated submission branch (your trainer will confirm which one).
4. Do not open a PR against `reference`.
