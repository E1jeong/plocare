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
- **Audience Identity**: Audience-specific worktrees must identify the running app in both the Desktop window title and Orca terminal title. Use `PloCare Partner` for Partner UI and `PloCare Consumer` for Consumer UI; never launch both under the ambiguous `PloCare` title.

## Change Gates

- **Debug-Only Host**: The Desktop app is an internal UI preview and debugging host. Do not package or distribute it; Android and iOS remain the product delivery targets.
- **Desktop Authentication Preview**: Keep the shared splash and role-aware login screen on Desktop. The Consumer Google button may use a Desktop-only debug success result so it opens Consumer Main without external Google authentication; Partner Login opens Partner Main through its existing UI-development bypass. Android and iOS retain their platform login flows.
- **Non-Disruptive Validation**: Routine AI verification uses background compile and test commands only. Do not launch the Desktop app, open or focus an Orca terminal, capture the visible desktop, or manipulate windows unless the user explicitly asks to see or interact with the running UI. If background screenshot capture is unavailable, report that visual verification was not performed instead of opening a visible window.
- **Explicit Desktop Run**: When the user explicitly asks to run, see, or debug the Desktop app, launch the Compose Hot Reload-enabled app through a focused Orca terminal and leave it running for live UI iteration and screenshots. Do not substitute the plain `:desktopApp:run` task:
  ```powershell
  orca terminal create --worktree active --title "PloCare Partner Hot Reload" --command ".\gradlew.bat :desktopApp:hotRun" --focus
  ```
- **User Execution**: Instruct the user to use Android Studio's `desktopApp` run configuration (Run ▶) or execute `.\gradlew.bat :desktopApp:run` directly in their terminal.

## Verify

```powershell
# Desktop module compile verification
.\gradlew.bat :desktopApp:compileKotlin
```
