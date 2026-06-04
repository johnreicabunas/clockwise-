---
name: clockwise-project-reference
description: Primary coding reference for the Clockwise Kotlin Multiplatform Compose project. Use when Codex plans, implements, reviews, refactors, tests, or explains code in this repo, especially work involving MVVM, Clean Architecture, Compose UI components, Koin wiring, repositories, use cases, ViewModels, platform-specific APIs, or extraction of user-visible strings into string resources.
---

# Clockwise Project Reference

## Overview

Use this skill as the default engineering standard for Clockwise code changes. Apply the repo's existing Kotlin Multiplatform, Compose, Koin, MVVM, and Clean Architecture patterns before introducing new patterns.

## Workflow

1. Inspect the current implementation before designing changes.
2. Read [project-guidelines.md](references/project-guidelines.md) before editing code, writing a plan, or reviewing a change.
3. Keep changes scoped to the requested behavior and aligned with the existing package structure.
4. Verify with the narrowest meaningful Gradle checks or tests for the touched area.

## Non-Negotiables

- Preserve MVVM + Clean Architecture boundaries.
- Prefer reusable Compose UI components over duplicated screen-local UI.
- Extract user-visible hardcoded strings into the correct string resource file.
- Keep domain logic platform-neutral and testable.
- Avoid broad rewrites, unrelated cleanup, and new dependencies unless the task requires them.
