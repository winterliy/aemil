# Native packaging and Fabric compatibility implementation plan

> **For implementation:** use the executing-plans skill to carry this plan out. Keep the existing vanilla launcher sign-in flow.

**Goal:** Produce macOS DMG and Windows EXE installers, bundle and install Fabric Loader/API correctly, and support version-specific user-mod environments.

**Architecture:** Configure Compose Desktop `nativeDistributions` with bundled runtime/JDK modules. Build distributables on native GitHub Actions runners. Add a Fabric artifact resolver that uses official metadata and Maven coordinates per Minecraft version; persist Loader in the normal version profile/libraries and API as one runtime mod. Extend Aemil's existing mod repository to associate managed mods with version-specific instance folders while importing standard `.minecraft/mods` content.

**Tech stack:** Kotlin/JVM 25, Compose Desktop 1.12.2, Gradle, jpackage, GitHub Actions, Fabric metadata/Maven.

### Task 1: Pin package identity and runtime resources

**Files:** `launcher/build.gradle.kts`, root `gradle.properties`

- Configure package name, app version compatible with jpackage, bundle ID, icon resources, Windows per-user install, upgrade UUID, and target formats.
- Ensure Fabric Loader and Fabric API artifacts for 26.3 are copied into named launcher resources and available through the application classloader.
- Retain `uberJar` as the current-OS development fallback.
- Verify Gradle task graph and local package prerequisites; do not run the test suite unless requested.

### Task 2: Add version-matched Fabric runtime resolution

**Files:** `launcher/src/main/kotlin/dev/aemil/launcher/MinecraftInstaller.kt`, new Fabric resolver source, `launcher/build.gradle.kts`

- Parse Fabric loader metadata and select a stable Loader for the selected Minecraft version.
- Resolve an API artifact that declares support for that Minecraft version; prefer the bundled 26.3 copy, download and verify other versions from official metadata/Maven.
- Put Loader in normal version libraries/profile metadata, never in `mods`.
- Install Fabric API once and track the managed file for safe cleanup/update.
- Surface missing compatible API or Loader versions with a version-specific message.

### Task 3: Add isolated per-version managed mod directories

**Files:** `launcher/src/main/kotlin/dev/aemil/launcher/AppPaths.kt`, `UserModsRepository.kt`, `Main.kt`

- Add an Aemil instance root for each game version and store Aemil-managed mod toggles there.
- Keep `.minecraft/mods` supported as an import/discovery source and as the default Minecraft Launcher-compatible folder.
- Synchronize each profile's selected mods to its game directory without deleting unrelated user jars.
- Add an “Open this version's Mods folder” control and clearly show the standard shared mods folder as an import option.

### Task 4: Add native release automation

**Files:** `.github/workflows/release.yml`, possibly `launcher/build.gradle.kts`

- Build DMG on an Apple Silicon macOS runner and EXE on a Windows x64 runner.
- Install/configure required JDK 25 and WiX toolchain where required.
- Upload both generated installers to the GitHub release matching the tag.
- Keep signing optional and document initial unsigned-release behavior.

### Task 5: Update product/readme language and verify artifacts

**Files:** `README.md`, launcher UI copy in `Main.kt`

- Replace the stale statement that native installers are future work.
- Explain version-specific mods, embedded Fabric runtime components, and practical compatibility limits.
- Build the DMG locally and inspect its app bundle/runtime/resources. Validate workflow YAML and EXE job configuration; do not claim the Windows artifact exists until a native Windows runner produces it.

## Verification checklist

- `./gradlew :launcher:packageDmg` completes on macOS and the DMG contains the launcher, bundled JVM, loader/API resources, and Aemil assets.
- Fabric artifact selection distinguishes supported API versions and installs only one API instance.
- Aemil profile library metadata includes Loader; `mods` contains Fabric API and selected user mods, not a second Loader jar.
- Two Minecraft versions have separate managed mod directories and do not overwrite each other's generated files.
- Release workflow produces and attaches `.dmg` and `.exe` on corresponding native runners.
- No `test` tasks are run unless specifically requested.
