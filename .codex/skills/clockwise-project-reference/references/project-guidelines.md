# Clockwise Project Guidelines

## Architecture

- Treat this as a Kotlin Multiplatform Compose app with Clean Architecture layers: `presentation`, `domain`, `data`, `platform`, and `di`.
- Keep dependencies pointing inward: presentation may call ViewModels and domain models/use cases; domain must not depend on data, presentation, Android, iOS, JVM, Compose, or Koin.
- Define business contracts in `domain/repository` and implement them in `data/repository`.
- Keep storage, alarms, notifications, preferences, and platform APIs behind interfaces or `expect`/`actual` declarations.
- Put pure scheduling, time, validation, and formatting rules that affect behavior in `domain` and cover meaningful cases with `commonTest`.
- Register implementations in Koin modules near the existing `appModule` and `platformModule` patterns.

## MVVM

- Expose screen state from ViewModels as immutable observable state, typically `StateFlow`.
- Let ViewModels own user intents, loading, validation, saving, deletion, and navigation mode state.
- Keep composables focused on rendering state and emitting callbacks; avoid repository, storage, platform, or scheduling calls from composables.
- Model UI state with stable data classes and enums instead of loose booleans or nullable strings when the state has clear variants.
- Keep ViewModel functions named as user intents or state transitions, such as `startCreate`, `saveEditor`, `showScheduleList`, or `updateEditorTitle`.

## Compose UI

- Prefer existing Material3 and Compose Multiplatform APIs already used by the app.
- Extract repeated UI into `presentation/components` or small private composables when reuse is local to one screen.
- Avoid growing a screen into a monolith; split content sections, list items, dialogs, pickers, and reusable controls.
- Keep composables stateless when possible. Hoist state to the caller or ViewModel unless the state is purely local UI input.
- Pass callbacks rather than ViewModels into lower-level components.
- Use existing icons and components before adding custom drawing or dependencies.
- Keep layout responsive with stable dimensions, clear spacing, and no overlapping text.

## Strings And Resources

- Extract all user-visible hardcoded UI strings.
- For common Kotlin Multiplatform UI, store strings in `composeApp/src/commonMain/composeResources/values/strings.xml` and use Compose Multiplatform resource access.
- For Android-only code, store strings in `composeApp/src/androidMain/res/values/strings.xml` and access them with Android resources.
- Keep non-user-facing constants in code when appropriate, including storage keys, preference names, notification channel IDs, serialization names, date/time format tokens, ad unit IDs, and test fixture values.
- Use resource names that describe the UI purpose, not the current English copy.
- Preserve interpolation by using placeholders or formatting helpers instead of concatenating resource lookups throughout UI code.

## Senior-Level Kotlin Practice

- Prefer simple, explicit Kotlin over clever abstractions.
- Make invalid states hard to represent when the model is stable enough to justify it.
- Handle parse, timezone, IO, and platform failures deliberately; do not hide important failures with broad empty defaults unless the UX calls for it.
- Keep coroutine work in the right scope and dispatcher. Avoid blocking UI code.
- Add or update tests when behavior, domain logic, persistence, or edge cases change.
- Keep public interfaces small and stable; change contracts only when the feature needs it.
- Preserve existing user changes in the worktree and avoid unrelated formatting churn.
