# Third-party components

Aemil bundles the following upstream Fabric components for its Minecraft 26.3 client. These components are pinned, remain separate nested JARs, and retain their upstream screens and configuration behavior.

| Component | Version | License | Source |
| --- | --- | --- | --- |
| Dynamic FPS | 3.11.10 (Minecraft 26.3.0, Fabric) | MIT | https://github.com/juliand665/Dynamic-FPS |
| Mod Menu | 21.0.0 (Minecraft 26.3) | MIT | https://github.com/TerraformersMC/ModMenu |
| Cloth Config API | 26.3.159 (Fabric) | LGPL-3.0 | https://github.com/shedaniel/cloth-config |
| FerriteCore | 9.0.0 (Fabric, Minecraft 26.3) | MIT | https://github.com/malte0811/FerriteCore |
| Lithium | 0.26.3 (Fabric, Minecraft 26.3) | LGPL-3.0 | https://github.com/CaffeineMC/lithium |
| Krypton | 0.3.2 (Fabric, Minecraft 26.3) | LGPL-3.0 | https://github.com/astei/krypton |
| Continuity | 3.0.1+26.3 (Fabric) | LGPL-3.0 | https://github.com/PepperCode1/Continuity |
| Architectury API | 22.0.5 (Fabric, Minecraft 26.3) | LGPL-3.0 | https://github.com/architectury/architectury-api |
| Forge Config API Port | 26.3.1 (Fabric) | MPL-2.0 / LGPL-3.0 (component-specific) | https://github.com/Fuzss/forge-config-api-port |
| Sodium | 0.9.2 (Fabric, Minecraft 26.3) | PolyForm Shield 1.0.0 | https://github.com/CaffeineMC/sodium |
| Iris | 1.11.7 (Fabric, Minecraft 26.3) | LGPL-3.0 | https://github.com/IrisShaders/Iris |
| Phosphor | 0.8.1 (Fabric, Minecraft 1.19.x only) | LGPL-3.0 | https://github.com/CaffeineMC/phosphor-fabric |

Cloth Config, Krypton, and Continuity carry license files in their component JARs. The Dynamic FPS and FerriteCore JARs do not include their upstream MIT license files, and the Lithium JAR does not include its LGPL-3.0 text; the applicable license texts are included below and in `META-INF/licenses` in the client JAR. Aemil does not modify these bundled components.

## Dynamic FPS — MIT License

Copyright (c) 2019 - 2024 Julian Dunskus

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Lithium — LGPL-3.0

Copyright (C) 2007 Free Software Foundation, Inc. The full GNU Lesser General Public License version 3 text is included at `META-INF/licenses/LGPL-3.0.txt` and is available at https://www.gnu.org/licenses/lgpl-3.0.html.

Continuity's original configuration screen remains accessible through Mod Menu. Aemil does not replace or reimplement that screen.

Architectury API and Forge Config API Port are included as user-mod compatibility libraries. Forge Config API Port contains components under different licenses; see its upstream `LICENSE.md`, `LICENSE-NIGHT-CONFIG.md`, and `LICENSING.md` files for the per-component terms.

## Version-specific component

Phosphor is installed only when the selected Minecraft version is 1.19–1.19.4. Its archived upstream release does not support Minecraft 26.3, so it is deliberately absent from that profile.

Dynamic FPS upstream was archived on 2026-10-21. Aemil currently pins its 26.3 release and will review compatibility before changing that dependency.

## FerriteCore — MIT License

Copyright (c) 2020 malte0811

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
