# DesktopApp Module Guide

## Scope

PloCare Compose Multiplatform desktop host application module (Windows, macOS, Linux).
Houses window creation and desktop runtime lifecycle; business logic and common UI reside in `shared/`.

Report plans and results in Korean.

## Orient First

- Entrypoint: `desktopApp/src/main/kotlin/com/senplo/plocare/main.kt`
- Build configuration: `desktopApp/build.gradle.kts`

## Boundary & Architecture Constraints

- **Thin Host**: Keep layout, navigation, and business logic in `shared/`; `desktopApp` only hosts `App()` inside a desktop window.
- **Window Size**: `main.kt` must maintain explicit window dimensions (`width = 440.dp, height = 860.dp`) via `rememberWindowState` to prevent zero-size collapse.

## Change Gates

- **AI Execution Gate**: The AI agent's subprocess shell runs in an isolated virtual desktop (`exebox`), which prevents GUI windows from appearing on the user's monitor. When launching the desktop app for the user, agents **must use the Orca CLI** to open a focused terminal tab:
  ```powershell
  orca terminal create --title "PloCare App" --command ".\gradlew.bat :desktopApp:run" --focus
  ```
- **User Execution**: Instruct the user to use Android Studio's `desktopApp` run configuration (Run ▶) or execute `.\gradlew.bat :desktopApp:run` directly in their terminal.

## Verify

```powershell
# Desktop module compile verification
.\gradlew.bat :desktopApp:compileKotlin

# Package Windows MSI installer
.\gradlew.bat :desktopApp:packageMsi

# Create distributable package
.\gradlew.bat :desktopApp:createDistributable
```
