# Aemil native installers and Fabric compatibility

## Goal

Ship Aemil as a macOS DMG and Windows EXE installer, following MultiMC's emphasis on predictable game installations while keeping Aemil's existing launcher workflow. Bundle the Fabric runtime components and provide the standard Fabric Loader environment expected by user mods across selected Minecraft versions.

## Chosen approach

- Keep the Kotlin + Compose Desktop application and use Compose's `jpackage` integration for native installers.
- Build each native artifact on its target operating system in GitHub Actions: DMG on macOS and EXE on Windows. Keep a runnable JAR for development and fallback.
- Bundle a known-good Fabric Loader and Fabric API artifact for the current Minecraft 26.3 development target. Use Fabric's version metadata and Maven artifacts to select/download compatible loader and API artifacts for other selected Minecraft versions. Place Loader in `.minecraft/libraries` and profile metadata; place Fabric API exactly once as a runtime mod.
- Preserve the standard `.minecraft/mods` folder as an import/source for user mods. Add per-Minecraft-version Aemil instance directories for version-specific managed mods/configuration, avoiding incompatible jars being forced into every profile. The launcher UI should make the active version's Mods folder easy to open.
- Report dependency/version mismatches with actionable diagnostics. Describe broad Fabric compatibility accurately; individual mods can still be limited by Minecraft version, Loader/API version, Java version, side, or mod conflicts.
- Continue using the official Minecraft Launcher for Microsoft sign-in and gameplay.
- Keep the launcher in Kotlin/Compose for now. Consider C/C++/Rust/ASM only after profiling identifies a hot path whose native implementation offers a measurable user benefit.

## MultiMC reference

Use MultiMC as a product and behavior reference for explicit isolated instances and understandable profile ownership. Do not transplant its C++/Qt implementation or package format into the Compose launcher.

## Packaging details

- macOS: self-contained `.dmg` with Aemil.app and a bundled Java runtime. Initially target Apple Silicon; add Intel as a separate artifact if supported by the project's native dependency/toolchain matrix.
- Windows: per-user `.exe` installer with bundled Java runtime and stable upgrade identity.
- Release artifacts are generated on native OS runners. Code signing/notarization is out of scope until signing credentials are configured.
- Keep app data in the existing Minecraft `aemil` directory, outside the application installation folder, so upgrades preserve profiles and settings.

## Non-goals

- Do not promise literal compatibility with every Fabric mod. Make the standard Fabric ecosystem broadly compatible and expose clear incompatibility reasons.
- Do not move Microsoft authentication into Aemil.
- Do not port the launcher to C/C++ as part of this change.
- Do not put Fabric Loader's implementation JAR in `.minecraft/mods`; Loader is profile/runtime infrastructure, while Fabric API is a mod dependency.

## Acceptance criteria

- Local macOS `packageDmg` produces an installable Aemil DMG containing the application, Java runtime, and bundled runtime resources.
- Windows native CI `packageExe` produces a per-user installer and the two OS artifacts are attached to a tagged GitHub release.
- The launcher retains the development JAR task.
- Fabric Loader and Fabric API artifacts are present in the packaged application and installed/selected for the requested Minecraft version without duplication.
- User-managed mods can be isolated by Minecraft version while existing `.minecraft/mods` jars remain discoverable.
- README and launcher copy distinguish the broad compatibility goal from a guarantee for every upstream mod.
