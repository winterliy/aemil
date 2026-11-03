# Aemil architecture

## Runtime directories

The launcher uses the platform Minecraft data root and creates an `aemil` directory inside it. Aemil-managed updates, profiles, settings, logs, and the Fabric game directory live there. The Fabric game directory owns the conventional `mods`, `config`, `resourcepacks`, and `logs` folders.

## Built-in features and user mods

Integrated Aemil features are compiled into the Aemil client and are always active; they do not appear as independently toggleable built-in mods. User-added Fabric mods are discovered from the selected Aemil game directory's `mods` folder and may be enabled or disabled by managing their installed files or through future client UI.

Fabric API remains a platform compatibility dependency, and Fabric Language Kotlin is packaged as an implementation dependency for Kotlin entrypoints and Kotlin-authored user mods. These are runtime support components, not user-selectable Aemil feature modules.

## Version policy

Minecraft 26.3 is the first development target. The launcher version catalog can expose Minecraft Java releases and snapshots, but the presence of a version in the selector does not imply that Aemil's integrated feature implementation has been ported to it. Compatibility metadata must identify the versions for which Aemil features and external Fabric mods are supported.

## Zoom behavior

The client zoom feature uses a configurable default magnification. Pressing `C` starts zooming; scrolling while held adjusts magnification within the configured minimum and maximum; releasing `C` ends zooming; the next press resets to the default before zooming again. The Minecraft HUD is not changed.
