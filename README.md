# Aemil

A Fabric-based Minecraft client and desktop launcher. The initial development target is Minecraft Java Edition 26.3 on macOS and Windows.

## Modules

- `launcher`: Kotlin + Compose Desktop launcher UI.
- `client`: Fabric Loom client mod, compiled for Java 25 and Minecraft 26.3.

Built-in Aemil features are part of the client and are not user-toggleable as separate mods. The normal Fabric `mods` directory is reserved for user-added mods; the launcher can enable or disable those files. Fabric API and Fabric Language Kotlin are packaged as runtime dependencies for compatibility and Kotlin entrypoints.

This is the initial integration stage, not the completed port of all requested feature areas. Aemil's Zoom is implemented in the client. Dynamic FPS, Mod Menu, Cloth Config, FerriteCore, Lithium, Krypton, Continuity, Architectury API, Forge Config API Port, Sodium, and Iris are pinned as bundled Fabric components for Minecraft 26.3. Dynamic FPS, Continuity, Sodium, and Iris retain their in-game settings screens; built-in features cannot be toggled from the launcher. Phosphor 0.8.1 is installed only for its supported Minecraft 1.19.x versions, not 26.3.

## Build

Use a JDK 25 or newer and run:

```sh
./gradlew build
```

Run the launcher:

```sh
./gradlew :launcher:run
```

Build a macOS `.dmg` installer on macOS or a Windows `.exe` installer on Windows (these formats cannot be cross-built):

```sh
./gradlew :launcher:packageDmg
./gradlew :launcher:packageExe
```

The native installer includes a Java runtime, so users do not need to install Java separately. Native release builds run on their target operating system; GitHub release tags build an Apple Silicon macOS DMG and a Windows x64 EXE. The initial installers are not signed or notarized; macOS may block a downloaded app until the user approves it in Privacy & Security, and Windows may show SmartScreen. A runnable launcher JAR remains available for development:

```sh
./gradlew :launcher:uberJar
```

The JAR is written to `launcher/build/libs/aemil-launcher-0.1.0-alpha.7-all.jar` and includes the current OS's Compose Desktop runtime. Native C/C++/Rust/ASM work will be considered for measured performance hot spots after the packaged launcher is stable.

The launcher will use the standard Minecraft data root and store Aemil-managed data in its `aemil` subdirectory:

- macOS: `~/Library/Application Support/minecraft/aemil`
- Windows: `%APPDATA%\.minecraft\aemil`

Minecraft versions, libraries, assets, and Aemil Loader profiles use the standard `.minecraft` layout. Aemil's settings and per-version instances live under `.minecraft/aemil`; each instance has its own mods, config, saves, logs, and resource packs. The launcher also imports jars from the standard `.minecraft/mods` folder into a version when installing its profile. The installer creates an `Aemil Loader <version>` installation in the vanilla Minecraft Launcher; sign-in and gameplay stay in that launcher. Fabric Loader is installed as profile/runtime infrastructure, and Fabric API is installed once as a mod. For Minecraft 26.3, the launcher also places the integrated Aemil module in that instance's mods folder and hides its entry from Mod Menu. Users do not install the module separately. Other bundled mods retain their original interfaces.

The launcher settings store the English/Korean language choice in `launcher.properties`. Aemil's zoom settings are configured in-game and stored in the Fabric config directory.

## Current scope

- Initial client target: Minecraft 26.3.
- The launcher version selector searches Mojang's release and snapshot list. Install downloads Minecraft and loader metadata, assets, and libraries into the standard `.minecraft` directory, resolves the best published Fabric API build for the selected game version, then registers an isolated Aemil game directory in `launcher_profiles.json`.
- Close Minecraft Launcher during installation so it does not overwrite the profile registration. Reopen it and choose `Aemil Loader <version>` to sign in and play.
- Aemil Loader's integrated feature module currently targets Minecraft 26.3. Other selected versions receive an Aemil Loader profile, matching Fabric API, and user mods, without the 26.3 Aemil feature bundle.
- Launcher language choices: English and Korean.
- Zoom: listed in Minecraft `Options → Controls`; hold `C`, scroll while held to adjust magnification, release and press again to return to the configured default. Default/minimum/maximum values are stored in the Aemil config.
- Dynamic FPS uses its upstream 26.3 build and original configuration UI through Mod Menu and Cloth Config. These components are bundled and are not listed as user-toggleable mods. Mod Menu's update checker is disabled by default; Aemil has no Modrinth catalog or integration.
- FerriteCore, Lithium, and Krypton provide built-in memory, game logic, and networking optimizations. Hydrogen is excluded because it is archived and its upstream recommends FerriteCore as a replacement; its old patches are not copied into the current FerriteCore code.
- Continuity provides connected textures with its upstream configuration screen. It is enabled as a built-in component and is not a user-toggleable mod.
- Architectury API and Forge Config API Port are bundled as compatibility libraries for user-added mods.
- Sodium 0.9.2 and Iris 1.11.7 are bundled for Minecraft 26.3 and retain their in-game settings screens. Phosphor 0.8.1 is installed on Minecraft 1.19.x only. Hydrogen is excluded per project direction.
- Phosphor's lighting optimization logic will be preserved and adapted for every Minecraft version Aemil supports, including 26.3; the current 1.19.x artifact is a compatibility bridge while those version-specific ports are developed.
- Launcher settings hold language, Minecraft version, built-in feature information, and per-version user Fabric mod management. Existing `.minecraft/mods` jars remain available as shared imports.
- Aemil targets broad compatibility with ordinary Fabric mods by using Fabric Loader and Fabric API builds matched to the selected Minecraft version. No launcher can guarantee every Fabric mod: mods may require a different game version, Java release, loader/API level, client/server side, or other dependencies, or conflict with each other. Loader errors identify unmet requirements when a mod declares them.
- The HUD stays visible during play and remains configurable through Minecraft's in-game settings. Aemil-owned settings are placed in existing Minecraft settings screens; upstream components retain their original screens and navigation.
