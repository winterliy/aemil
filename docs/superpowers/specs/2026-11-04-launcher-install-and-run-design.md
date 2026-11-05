# Vanilla Launcher Integration Design

## Intent

Ship Aemil first as a runnable desktop JAR. Aemil installs a selected Minecraft release with Fabric into the standard Minecraft data directory and registers a profile in the official Minecraft Launcher. The official launcher remains responsible for Microsoft sign-in and gameplay. Once this JAR workflow is stable, evaluate platform installers and a future native C/C++ app.

## Requirements

- Keep the Mojang release/snapshot version selector.
- Put game versions, libraries, assets, profiles, configs, saves, and runtime mods in the standard `.minecraft` layout.
- Keep Aemil preferences and source user mods under `.minecraft/aemil`.
- Register `Aemil Loader <version>` in `launcher_profiles.json` while preserving other profiles and settings.
- Warn users to close Minecraft Launcher while installing, because it may overwrite profile registration when open.
- Sync only Aemil-managed mods into `.minecraft/mods`; preserve files Aemil does not manage.
- Built-in features remain always active, and the launcher toggle list contains user-added mods only.
- Minecraft 26.3 is the current Aemil client target. Other selected versions get an Aemil Loader profile and user mods without the 26.3 Aemil client JAR.
- Sodium and Iris are included for 26.3. Phosphor 0.8.1 is installed only on its upstream-supported Minecraft 1.19.x versions.
- Keep Hydrogen excluded and Continuity included with its original settings UI.
- The HUD stays in-game and configurable in game. Do not move gameplay settings into a custom launcher UI.
- Microsoft authentication APIs and direct game launch are out of scope; the vanilla Minecraft Launcher handles them.
- Build a runnable JAR first. Defer `.dmg` and `.exe` packages until the JAR works reliably.

## Implementation

- Use Mojang and Fabric metadata to download version files and write the Aemil Loader version JSON under `.minecraft/versions`.
- Verify downloaded files against SHA-1 hashes where Mojang provides them.
- Update `launcher_profiles.json` atomically and preserve unrelated JSON fields.
- Track the filenames Aemil copies into `.minecraft/mods`; remove only files recorded as Aemil-managed when profiles change.
- Package the current platform's Compose Desktop runtime into `:launcher:uberJar`, with `Main-Class` set for `java -jar`.

## Acceptance

- The launcher JAR builds and starts on the build host.
- Installing a selected version leaves an Aemil Loader profile selectable in the official Minecraft Launcher.
- The install uses the standard Minecraft folders and does not remove unrelated `.minecraft/mods` files.
- The app contains no Microsoft OAuth settings or custom game launch path.
- Sodium and Iris are nested in the 26.3 client. Phosphor only enters the standard mods folder for 1.19–1.19.4.
- In-game feature settings remain reachable from Minecraft's settings screens and upstream mod screens.
