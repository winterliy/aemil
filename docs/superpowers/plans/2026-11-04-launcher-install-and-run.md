# Vanilla Launcher JAR Implementation Plan

**Goal:** Build a runnable Aemil JAR that installs Aemil Loader profiles into the standard Minecraft directory, then lets the official Minecraft Launcher handle sign-in and gameplay.

**Design:** `docs/superpowers/specs/2026-11-04-launcher-install-and-run-design.md`

## Tasks

1. **Vanilla profile installation**
   - [x] Install version JSON and client JAR under `.minecraft/versions`.
   - [x] Download libraries and assets into standard locations.
   - [x] Register an `Aemil Loader <version>` launcher profile while preserving other entries.
   - [x] Keep Aemil-owned settings and source mods under `.minecraft/aemil`.
2. **Mod file boundaries**
   - [x] Copy enabled user mods into `.minecraft/mods` without clearing unrelated files.
   - [x] Track Aemil-managed destination files and remove only those on sync.
   - [x] Install the 26.3 Aemil client only on 26.3.
   - [x] Install Phosphor 0.8.1 only for its supported 1.19.x versions.
3. **Authentication and gameplay handoff**
   - [x] Remove custom Microsoft/Xbox token exchange and account settings.
   - [x] Remove direct game process launch; direct users to select the profile in Minecraft Launcher.
   - [x] Remind users to close Minecraft Launcher during profile installation.
4. **Runnable launcher JAR**
   - [x] Add `:launcher:uberJar` with `Main-Class` and current-platform Compose dependencies.
   - [x] Build the JAR and inspect the embedded Fabric components and launcher resources.
   - [ ] Run the JAR on the build host and exercise a version install into a disposable Minecraft directory.
5. **Next platform phase**
   - [ ] Stabilize the runnable JAR workflow.
   - [ ] Then add `.dmg` and `.exe` packaging.
   - [ ] Consider a native C/C++ application after the JVM launcher is stable.
