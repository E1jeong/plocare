# PloCare AI Guide

## Context

This is a code-navigation and safety guide, not project history. Product context, planning, data-model detail, and decisions live in the vault-relative `Dev/Project/Personal/plocare` wiki; resolve it through `_meta/routing-tables.md` or `obsidian-wiki-sync`, then follow the vault root `AGENTS.md`.

Report plans and results in Korean.

## Code Map

| Module | Responsibility | Orient first | Local guide |
| --- | --- | --- | --- |
| `shared/` | Shared Compose UI, domain logic, filter calculation, IoT/Bluetooth integration, and platform abstractions | `shared/src/commonMain/kotlin/` | None |
| `androidApp/` | Android application shell | `androidApp/src/main/` | None |
| `desktopApp/` | Desktop application shell | `desktopApp/src/desktopMain/` | None |
| `iosApp/` | iOS SwiftUI host application | `iosApp/iosApp/` | None |

## Change Gates

- Keep business logic, filter calculation algorithms, and shared UI in `shared/`; platform applications remain thin hosts.
- Maintain platform-specific features (BLE, Kakao Map, Wi-Fi provisioning) behind `expect`/`actual` abstractions or dedicated interfaces.
- Preserve separation between customer visibility flows and manager/technician service flows.

## Verify

Run the narrowest relevant command:

```powershell
.\gradlew.bat :shared:test
.\gradlew.bat assembleDebug
.\gradlew.bat check
```

Use `./gradlew` with the same tasks on Linux, WSL, or macOS.
