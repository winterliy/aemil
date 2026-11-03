# Aemil

A Fabric-based Minecraft client and desktop launcher. The initial development target is Minecraft Java Edition 26.3 on macOS and Windows.

## Modules

- `launcher`: Kotlin + Compose Desktop launcher UI.
- `client`: Fabric Loom client mod, compiled for Java 25 and Minecraft 26.3.

Built-in Aemil features are part of the client and are not user-toggleable as separate mods. The normal Fabric `mods` directory is reserved for user-added mods. Fabric API and Fabric Language Kotlin are packaged as runtime dependencies for compatibility and Kotlin entrypoints.

## Build

Use a JDK 25 or newer and run:

```sh
./gradlew build
```

Run the launcher:

```sh
./gradlew :launcher:run
```

The launcher will use the standard Minecraft data root and store Aemil-managed data in its `aemil` subdirectory:

- macOS: `~/Library/Application Support/minecraft/aemil`
- Windows: `%APPDATA%\.minecraft\aemil`

The Aemil game directory (including its `mods`, `config`, and `logs` folders) is kept under this directory so user-installed Fabric mods are isolated from vanilla profiles.

The launcher settings store the English/Korean language choice in `launcher.properties`. Aemil's zoom settings are configured in-game and stored in the Fabric config directory.

## Current scope

- Initial client target: Minecraft 26.3.
- The version selector is planned to list Minecraft Java versions from Mojang's official version manifest; integrated Aemil features are initially developed against 26.3.
- Launcher language choices: English and Korean.
- Zoom: hold `C`, scroll while held to adjust magnification, release and press again to return to the configured default. Minimum and maximum values are stored in the Aemil client config.
- The in-game HUD remains vanilla. Aemil's own settings screen is separate from the HUD.
