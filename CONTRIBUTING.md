# Contributing Guidelines

Welcome! This project is a personal initiative to explore professional software development practices in a homelab
environment. Even though it's a solo project, contributions (even from myself) follow a clean, structured process — just
like in a real-world team.

---

## Issue Types

Before starting any work, create or link an appropriate issue. The project uses three types of issues:

| Type    | Purpose                                                  |
|---------|----------------------------------------------------------|
| Bug     | A defect or unexpected behavior in the system            |
| Feature | A new capability or improvement that adds value          |
| Task    | Technical or internal work not directly visible to users |

You can start with an issue template using the **"New Issue"** button on GitHub.

---

## Creating an Issue

When opening an issue, please:

- Use the **appropriate template**
- Be as clear and concise as possible
- Add **labels** such as `bug`, `enhancement`, `task`
- Link related issues or PRs when applicable

---

## Working on an Issue

Each piece of work should follow this flow:

1. **Create/assign the issue** with a clear title
2. **Create a new branch** from `main`, named like:  
   `bug/fix-login` • `feature/add-auth` • `task/refactor-service`
3. **Commit referencing the issue**:  
   `git commit -m "Fix login error handling (#12)"`
4. **Open a Pull Request** with a clear description and link to the issue  
   (_use `Closes #12` to auto-close it on merge_)
5. **Move the issue card** on the GitHub Project board through its stages:  
   `To Do → In Progress → In Review → Done`

---

## Code & Commit Style

- Write clear, atomic commits — one purpose per commit
- Use conventional prefixes when possible: `fix:`, `feat:`, `refactor:`, `chore:`
- Keep the code clean, well-documented, and tested if applicable

---

## Pull Requests

Every PR should:

- Reference the issue it's related to
- Describe **what, why, and how**
- Contain minimal, focused changes
- Be easy to read and review

---

## Final Note

This repository reflects my goal to maintain the same quality standards found in team environments — clean code,
traceable decisions, and a documented process.

Thanks for reading — now go build something awesome.  