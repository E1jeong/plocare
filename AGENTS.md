# PloCare AI Guide

## Context

This is a code-navigation and safety guide, not project history. Product context, planning, data-model detail, and decisions live in the vault-relative `Dev/Project/Personal/plocare` wiki; resolve it through `_meta/routing-tables.md` or `obsidian-wiki-sync`, then follow the vault root `AGENTS.md`.

Report plans and results in Korean.

## Code Map

| Module | Responsibility | Orient first | Local guide |
| --- | --- | --- | --- |
| `shared/` | Shared Compose UI, domain logic, filter calculation, IoT/Bluetooth integration, and platform abstractions | `shared/src/commonMain/kotlin/` | None |
| `androidApp/` | Android application shell | `androidApp/src/main/` | None |
| `desktopApp/` | Desktop application shell | `desktopApp/src/desktopMain/` | `desktopApp/AGENTS.md` |
| `iosApp/` | iOS SwiftUI host application | `iosApp/iosApp/` | None |

## Change Gates

- Keep business logic, filter calculation algorithms, and shared UI in `shared/`; platform applications remain thin hosts.
- Maintain platform-specific features (BLE, Kakao Map, Wi-Fi provisioning) behind `expect`/`actual` abstractions or dedicated interfaces.
- Preserve separation between customer visibility flows and manager/technician service flows.
- Keep consumer and partner palettes in `shared/src/commonMain/kotlin/com/senplo/plocare/ui/theme/` and switch them through `AppAudience`; screens read `PloCareColor` tokens.

## Verify

Run the narrowest relevant command:

```powershell
.\gradlew.bat :shared:jvmTest
.\gradlew.bat assembleDebug
.\gradlew.bat check
```

For desktop app execution and packaging rules, see [`desktopApp/AGENTS.md`](file:///C:/Users/Unionbiometrics/Desktop/dev/1.project/plocare/desktopApp/AGENTS.md).

Use `./gradlew` with the same tasks on Linux, WSL, or macOS.
