# Task Management Guidelines

This project uses GitHub Issues, Labels, Milestones, and Projects to structure work like in a professional environment. Every issue should be **clear, scoped, and traceable** — even if authored solo.

---

## Labels

Labels are used to quickly understand the nature and importance of an issue. Every issue must include at least:

- One **Priority**
- One **Type**
- (Optional) One or more **Component(s)**

### Priority Labels

| Label          | Meaning                                |
|----------------|----------------------------------------|
| `priority: high`   | Must be done ASAP, blocks other work |
| `priority: medium` | Important, but not urgent            |
| `priority: low`    | Nice to have / polish work           |

### Type Labels

| Label           | Description                          |
|------------------|--------------------------------------|
| `bug`            | Something is broken                  |
| `enhancement`    | New feature or improvement           |
| `task`           | Internal work (CI, refactor, etc.)   |
| `docs`           | Documentation-related issue          |

### Component Labels

Custom to your project. Examples:

| Label            | Description                         |
|------------------|-------------------------------------|
| `auth`           | Login, tokens, permissions          |
| `ui`             | Frontend or visual components       |
| `api`            | REST endpoints or controllers       |
| `infra`          | CI/CD, Docker, deployments          |

---

## Milestones & Sprints

### Milestones

- Represent **versions or releases**
- Group issues that belong to a certain delivery target
- Named like: `v1.0.0`, `v1.1.0`, `MVP`, `Beta`

Each merged feature/bug should close an issue linked to a milestone.

##  Workflow Summary

1. **Create an issue** using the proper template
2. **Add labels**:
    - Priority (high/med/low)
    - Type (bug, enhancement, task)
    - Component (auth, api, etc.)
3. **Assign milestone** (e.g. `v1.0.0`)
4. **Add to active sprint/project board**
5. **Link to PR** using `Closes #issue`

## Example Issue
```md
Title: [Bug] Token not refreshed after login
Labels: bug, priority: high, auth
Milestone: v1.0.0
Sprint: Sprint 03

This would be worked on in a `bug/token-refresh` branch and closed by a PR that says:  
`fix(auth): refresh token logic corrected (#45)`
```
