# AGENTS.md

## General Rules

- Think in English, answer the user in Russian.
- Keep answers brief and practical.
- Read the existing code before making changes and follow current project patterns.
- Do not make unrelated refactors.

## Project Purpose

Simple GNSS Status is a minimalist Android app for viewing GNSS navigation status.

The main UI goal is to quickly show whether coordinates are available, how accurate they are, how many satellites are used, and what their signal levels are.

## Technology

- Native Android.
- Kotlin.
- Jetpack Compose.
- Gradle Kotlin DSL.
- Unit tests for pure domain logic and formatting.

## UX Principles

- Keep the interface minimalist.
- Show only data that helps evaluate navigation status and accuracy.
- Do not add marketing screens, help panels, or decorative elements.
- Dark theme is the primary theme.
- The satellite table must be dense and readable on a phone.
- The table header must be visible even before satellites appear.
- Empty states should be calm and concise.

## GNSS Logic

- Calculate average signal level only from satellites used in the fix (`usedInFix`).
- Do not show satellites with zero C/N0.
- The table should show satellite ID, system flag, usage status, and signal level.
- Show GPS with the US flag, GLONASS with the Russian flag, Galileo with the EU flag, and BeiDou with the Chinese flag.
- Usage status: `Да` in green, `Нет` in orange.
- Do not bring back A-GPS/XTRA features without a separate explicit request.

## Permissions and Background Modes

- Do not request unnecessary permissions.
- Do not use a foreground service without a separate justification.
- Do not request notification permission unless the app has user-facing notifications.
- The app's main permission is precise location for reading GNSS status.
- Keeping the screen on while the app is open is acceptable.

## Code

- Keep UI logic in Compose components.
- Keep access to Android GNSS APIs separate from UI.
- Move formatting and calculations to the domain layer so they can be tested.
- Use clear Russian labels in the user interface.
- Do not add dependencies unless necessary.

## Verification

Run after changes:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

If a phone is connected, also verify installation:

```powershell
.\gradlew.bat :app:installDebug
```

A physical Android phone is required for real GNSS status. An emulator is not a reliable check for the satellite list or signal level.
