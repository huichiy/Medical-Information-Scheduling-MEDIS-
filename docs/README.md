# MEDIS — Documentation Index

All project documentation for the Hospital Management System, organized by purpose.

---

## 📖 Start here

| Document | What it's for |
|----------|---------------|
| [HOW-TO-RUN.md](HOW-TO-RUN.md) | Build, run, login, and test the app — step by step |
| [CODE-EXPLANATION.md](CODE-EXPLANATION.md) | Full walkthrough of every class + OOP concepts (video Section 5 script) |
| [FOLDER-STRUCTURE.md](FOLDER-STRUCTURE.md) | What every folder and file is for, in plain language |

---

## 🏗️ `design/` — how the system is built

| Document | Contents |
|----------|----------|
| [architecture-approaches.md](design/architecture-approaches.md) | MVC+DAO vs layered+Service — why we chose Approach 1 |
| [data-flow.md](design/data-flow.md) | End-to-end traces: login, book appointment, generate report |
| [error-handling.md](design/error-handling.md) | The `Result` pattern + 3-layer defense |
| [diagrams.md](design/diagrams.md) | Briefs for all 6 UML diagrams (Use Case, Class, 4 Sequence) |

---

## 📋 `project/` — planning & management

| Document | Contents |
|----------|----------|
| [planning.md](project/planning.md) | Overall plan, tech stack, timeline, risks |
| [work-distribution.md](project/work-distribution.md) | Member A/B/C/D task split |
| [todolist.md](project/todolist.md) | Live progress tracker (what's done / pending) |
| [testing-strategy.md](project/testing-strategy.md) | 15-step manual test + JUnit plan |
| [question.md](project/question.md) | Q&A on architecture concepts (folders, DAO, packages) |

---

## 📑 Reference

| Item | Notes |
|------|-------|
| `Lab Exercise 2026.pdf` | The original assignment brief |
| `superpowers/specs/` | Internal design spec (Claude-generated) |
| `superpowers/plans/` | Internal implementation plan (Claude-generated) |

---

## Submission deliverables checklist

- [x] Java Swing source code (`src/`, organized into packages)
- [x] Database integration (SQLite, `db/schema.sql`)
- [x] Working system (5 modules, verified — see HOW-TO-RUN §6)
- [ ] UML diagrams — Use Case, Class, 4 Sequence (see `design/diagrams.md`)
- [ ] Presentation video (10–15 min, rubric order)
- [ ] Final ZIP: `StudentID-Name.zip`

---

## Folder structure

```
docs/
├── README.md                 ← you are here
├── HOW-TO-RUN.md             ← run & test guide
├── CODE-EXPLANATION.md       ← code walkthrough
├── FOLDER-STRUCTURE.md       ← what each folder is for
├── Lab Exercise 2026.pdf     ← assignment brief
├── design/                   ← architecture & design docs
├── project/                  ← planning & management docs
└── superpowers/              ← internal spec + plan
```
