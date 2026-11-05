# Aemil architecture

## Runtime directories

The launcher uses the platform Minecraft data root. Vanilla-compatible versions, libraries, assets, logs, configs, saves, and the runtime `mods` folder remain in the standard `.minecraft` layout. Aemil's own settings and source user mods live in `.minecraft/aemil`; the launcher tracks which files it copies into `.minecraft/mods` and does not remove unrelated files. For Minecraft 26.3, it automatically places the integrated Aemil Loader JAR in `.minecraft/mods` and hides the entry from Mod Menu.

The installer resolves official Mojang version metadata and a stable Fabric-based Aemil Loader profile, downloads files with SHA-1 checks, registers an `Aemil Loader <version>` installation in `launcher_profiles.json`, and reports progress in the launcher. Minecraft authentication and game startup are handled by the vanilla Minecraft Launcher; Aemil does not call Microsoft authentication APIs.

## Built-in features and user mods

Integrated Aemil features are compiled into the Aemil Loader module and are always active; Mod Menu hides the internal module entry and built-in features cannot be toggled separately. The launcher scans `.minecraft/aemil/mods` and lets users enable/disable only those user-added Fabric mod files. Enabled files are synchronized to the standard `.minecraft/mods` folder for the vanilla launcher.

Upstream features with established configuration screens are bundled as nested Fabric components while retaining their upstream UI, navigation, localization, and configuration files. They are always loaded and are not exposed as launcher toggles. Dynamic FPS 3.11.10 for Minecraft 26.3 uses Mod Menu 21.0.0 and Cloth Config 26.3.159 for its original configuration flow. Sodium 0.9.2 and Iris 1.11.7 are bundled for rendering and shaders with their in-game video settings. The Dynamic FPS upstream repository was archived in October 2026, so this version is pinned rather than tracking a moving branch. Mod Menu's update checker is disabled by default; Aemil does not include a Modrinth catalog or client integration.

Automatic optimization components are also bundled as nested Fabric components: FerriteCore 9.0.0, Lithium 0.26.3, and Krypton 0.3.2, all pinned to Minecraft 26.3-compatible releases. They run without user-facing toggles or new screens. Continuity 3.0.1+26.3 is bundled for connected textures and retains its original Mod Menu configuration screen. Hydrogen is not bundled: its upstream repository was archived in 2022 and describes its patches as relying on risky internal hacks; its maintainers point to FerriteCore as the maintained memory optimization replacement. Aemil does not copy Hydrogen's legacy implementation into FerriteCore.

## Pending requested features

Sodium 0.9.2 and Iris 1.11.7 are bundled for Minecraft 26.3. Sodium is pinned to the stable 26.3 release identified for Iris compatibility rather than the newer Sodium beta. Phosphor 0.8.1 is bundled in the launcher and installed only for Minecraft 1.19–1.19.4, the versions named by its upstream release; it is not loaded on 26.3. Feather hosting and PvP features remain out of scope, and Modrinth integration remains excluded.

Fabric API remains a platform compatibility dependency, and Fabric Language Kotlin is packaged as an implementation dependency for Kotlin entrypoints and Kotlin-authored user mods. Architectury API 22.0.5 and Forge Config API Port 26.3.1 are also packaged as always-on compatibility dependencies for supported user mods. They are runtime support components, not user-selectable Aemil feature modules.

Aemil-specific settings, such as Zoom, are added to the closest existing vanilla settings screen. Features without a user-facing screen remain automatic and do not receive invented settings pages. Third-party notices and pinned coordinates are maintained with the project.

## Version policy

Minecraft 26.3 is the first development target. The launcher version catalog can expose Minecraft Java releases and snapshots, but the presence of a version in the selector does not imply that Aemil's integrated feature implementation has been ported to it. Compatibility metadata must identify the versions for which Aemil features and external Fabric mods are supported.

## Settings placement

The HUD stays visible in-game and its settings remain available while playing. Aemil adds its Zoom key mapping under Minecraft `Options → Controls`, plus a Zoom Settings entry there for default/minimum/maximum magnification. Sodium and Iris use their in-game video settings; other upstream feature screens stay in their original menus. Language, version selection, built-in feature information, and user-mod management live in the desktop launcher.

## Zoom behavior

The client zoom feature uses a configurable default magnification. Pressing `C` starts zooming; scrolling while held adjusts magnification within the configured minimum and maximum; releasing `C` ends zooming; the next press resets to the default before zooming again. The Minecraft HUD is not changed.
