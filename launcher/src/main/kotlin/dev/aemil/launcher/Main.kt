package dev.aemil.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Aemil") {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF18CBAA),
                onPrimary = Color(0xFF061613),
                background = Color(0xFF101416),
                surface = Color(0xFF1B2225),
                onSurface = Color(0xFFF1F5F4),
            ),
        ) {
            AemilApp()
        }
    }
}

private enum class Page { PLAY, MODS, SETTINGS }
internal enum class AppLanguage { ENGLISH, KOREAN }

@Composable
private fun AemilApp() {
    LaunchedEffect(Unit) { withContext(Dispatchers.IO) { AemilPaths.ensureLayout() } }
    var language by remember { mutableStateOf(LauncherPreferences.language()) }
    var page by remember { mutableStateOf(Page.PLAY) }
    var versions by remember { mutableStateOf(listOf("26.3")) }
    var versionsStatus by remember { mutableStateOf("Loading Minecraft versions…") }
    var selectedVersion by remember { mutableStateOf("26.3") }
    var installing by remember { mutableStateOf(false) }
    var installStatus by remember { mutableStateOf("") }
    var installProgress by remember { mutableStateOf(0f) }
    var installedProfile by remember { mutableStateOf<InstalledProfile?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        runCatching { withContext(Dispatchers.IO) { MinecraftVersions.fetch() } }
            .onSuccess { fetched ->
                versions = fetched.ifEmpty { listOf("26.3") }
                versionsStatus = "${versions.size} Minecraft Java versions"
            }
            .onFailure { versionsStatus = "Offline — showing the 26.3 development profile" }
    }

    Row(Modifier.fillMaxSize().background(Color(0xFF101416))) {
        Column(
            Modifier.width(190.dp).fillMaxHeight().background(Color(0xFF151B1E)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("AEMIL", fontSize = 25.sp, color = Color.White)
            Spacer(Modifier.height(20.dp))
            NavigationButton(label(language, "Play", "플레이"), page == Page.PLAY) { page = Page.PLAY }
            NavigationButton(label(language, "Mods", "모드"), page == Page.MODS) { page = Page.MODS }
            NavigationButton(label(language, "Settings", "설정"), page == Page.SETTINGS) { page = Page.SETTINGS }
            Spacer(Modifier.weight(1f))
            Text("Aemil Loader · Minecraft Launcher", color = Color(0xFF92A09F), fontSize = 12.sp)
        }

        Column(Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 28.dp)) {
            when (page) {
                Page.PLAY -> PlayPage(
                    language = language,
                    selectedVersion = selectedVersion,
                    versions = versions,
                    versionsStatus = versionsStatus,
                    installing = installing,
                    installStatus = installStatus,
                    installProgress = installProgress,
                    installedProfile = installedProfile,
                    onVersionSelected = { selectedVersion = it; installedProfile = null },
                    onInstall = {
                        installing = true
                        installStatus = label(language, "Preparing installation…", "설치를 준비하는 중…")
                        installProgress = 0f
                        scope.launch {
                            try {
                                installedProfile = withContext(Dispatchers.IO) {
                                    MinecraftInstaller.install(selectedVersion) { message, completed, total ->
                                        scope.launch {
                                            installStatus = message
                                            installProgress = if (total == 0) 0f else completed.toFloat() / total
                                        }
                                    }
                                }
                                installStatus = label(language, "Installed. Select Aemil Loader ${installedProfile?.gameVersion} in Minecraft Launcher.", "설치 완료. Minecraft Launcher에서 Aemil Loader ${installedProfile?.gameVersion} 프로필을 선택하세요.")
                            } catch (failure: Exception) {
                                installStatus = failure.message ?: label(language, "Installation failed", "설치 실패")
                            } finally {
                                installing = false
                            }
                        }
                    },
                )
                Page.MODS -> ModsPage(language, selectedVersion)
                Page.SETTINGS -> SettingsPage(
                    language = language,
                    onLanguageSelected = {
                        language = it
                        LauncherPreferences.setLanguage(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun NavigationButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = colors) { Text(text) }
}

@Composable
private fun PlayPage(
    language: AppLanguage,
    selectedVersion: String,
    versions: List<String>,
    versionsStatus: String,
    installing: Boolean,
    installStatus: String,
    installProgress: Float,
    installedProfile: InstalledProfile?,
    onVersionSelected: (String) -> Unit,
    onInstall: () -> Unit,
) {
    Text(label(language, "Install Aemil into Minecraft Launcher", "Aemil을 Minecraft Launcher에 설치하세요"), fontSize = 29.sp, color = Color.White)
    Text(label(language, "Choose a Minecraft Java version", "Minecraft Java 버전을 선택하세요"), color = Color(0xFFAAB5B4))
    Spacer(Modifier.height(28.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2225))) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(label(language, "Profile · Aemil", "프로필 · Aemil"), fontSize = 19.sp)
            VersionPicker(language, versions, selectedVersion, onVersionSelected)
            Text(versionsStatus, color = Color(0xFF92A09F), fontSize = 12.sp)
            Text(label(language, "Aemil built-in features target Minecraft 26.3.", "Aemil 내장 기능의 우선 지원 버전은 Minecraft 26.3입니다."), color = Color(0xFFB5C2C0))
            Text(label(language, "Aemil built-in features currently load on 26.3; other versions use Aemil Loader and your own mods.", "Aemil 내장 기능은 현재 26.3에서 동작하며, 다른 버전은 Aemil Loader와 사용자 모드로 실행합니다."), color = Color(0xFF9DA9A8), fontSize = 12.sp)
            Text(label(language, "Close Minecraft Launcher before installing so it can pick up the new profile.", "새 프로필을 인식할 수 있도록 설치 전에 Minecraft Launcher를 종료하세요."), color = Color(0xFF9DA9A8), fontSize = 12.sp)
            Button(onClick = onInstall, enabled = !installing) {
                Text(if (installing) label(language, "Installing…", "설치 중…") else label(language, "Install to Minecraft Launcher", "Minecraft Launcher에 설치"))
            }
            if (installing) {
                androidx.compose.material3.LinearProgressIndicator(progress = { installProgress }, modifier = Modifier.fillMaxWidth())
            }
            if (installStatus.isNotBlank()) Text(installStatus, color = Color(0xFFB5C2C0), fontSize = 12.sp)
            installedProfile?.let { profile ->
                Text(
                    label(language, "Installed: Minecraft ${profile.gameVersion} · Aemil Loader ${profile.fabricVersion}", "설치됨: Minecraft ${profile.gameVersion} · Aemil Loader ${profile.fabricVersion}"),
                    color = Color(0xFF27D7B2),
                )
                Text(
                    label(language, "Open Minecraft Launcher, choose Aemil Loader ${profile.gameVersion}, then click Play.", "Minecraft Launcher에서 Aemil Loader ${profile.gameVersion}을 선택한 뒤 플레이를 누르세요."),
                    color = Color(0xFFB5C2C0),
                )
            }
        }
    }
    Spacer(Modifier.height(18.dp))
    Text(
        label(language, "Aemil features are built into the client. User Fabric mods stay in the Aemil mods folder.", "Aemil 기능은 클라이언트에 내장됩니다. 사용자 Fabric 모드는 Aemil mods 폴더에서 관리합니다."),
        color = Color(0xFF92A09F),
    )
}

@Composable
private fun VersionPicker(
    language: AppLanguage,
    versions: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("") }
    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text("${label(language, "Minecraft Version", "Minecraft 버전")}: $selected  ▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false; filter = "" }) {
            OutlinedTextField(
                value = filter,
                onValueChange = { filter = it },
                label = { Text(label(language, "Search versions", "버전 검색")) },
                singleLine = true,
                modifier = Modifier.width(280.dp).padding(horizontal = 12.dp, vertical = 6.dp),
            )
            versions.filter { it.contains(filter, ignoreCase = true) }.forEach { version ->
                DropdownMenuItem(
                    text = { Text(version) },
                    onClick = { onSelected(version); expanded = false; filter = "" },
                )
            }
        }
    }
}

@Composable
private fun ModsPage(language: AppLanguage, selectedVersion: String) {
    var userMods by remember(selectedVersion) { mutableStateOf(UserModsRepository.list(selectedVersion)) }
    Text(label(language, "Mods", "모드"), fontSize = 27.sp)
    Spacer(Modifier.height(18.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2225))) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Aemil Core · ${label(language, "Always active", "항상 활성")}", fontSize = 20.sp, color = Color(0xFF27D7B2))
            Text(label(language, "Integrated features cannot be toggled individually.", "내장 기능은 개별적으로 끌 수 없습니다."), color = Color(0xFFCBD4D2))
            Text(label(language, "Zoom · Rendering · Memory · Game logic · Networking · Background performance · Connected textures", "확대 · 렌더링 · 메모리 · 게임 로직 · 네트워크 · 백그라운드 성능 · 연결 텍스처"), color = Color(0xFF9DA9A8))
            Text(label(language, "Fabric API, Kotlin, Architectury, and Forge Config API Port support compatible user mods.", "Fabric API, Kotlin, Architectury, Forge Config API Port는 호환되는 사용자 모드를 지원합니다."), color = Color(0xFF9DA9A8), fontSize = 12.sp)
        }
    }
    Spacer(Modifier.height(16.dp))
    Text(label(language, "Your Fabric Mods", "사용자 Fabric 모드"), fontSize = 19.sp)
    Text(label(language, "Minecraft $selectedVersion · Add mods to this version's folder or import shared Minecraft mods.", "Minecraft $selectedVersion · 이 버전 폴더에 모드를 추가하거나 공용 Minecraft 모드를 가져오세요."), color = Color(0xFF9DA9A8))
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { AemilPaths.openModsFolder(selectedVersion); userMods = UserModsRepository.list(selectedVersion) }) {
            Text(label(language, "Open Version Mods", "버전별 mods 폴더 열기"))
        }
        OutlinedButton(onClick = { AemilPaths.openSharedMinecraftModsFolder() }) {
            Text(label(language, "Open Shared Minecraft Mods", "공용 Minecraft mods 열기"))
        }
        OutlinedButton(onClick = { userMods = UserModsRepository.list(selectedVersion) }) {
            Text(label(language, "Refresh", "새로고침"))
        }
    }
    Spacer(Modifier.height(8.dp))
    if (userMods.isEmpty()) {
        Text(label(language, "No mods in this version's folder. Shared Minecraft mods are copied in when installing this profile.", "이 버전 폴더에 모드가 없습니다. 공용 Minecraft 모드는 프로필 설치 시 복사됩니다."), color = Color(0xFF9DA9A8))
    } else {
        userMods.forEach { mod ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = mod.enabled,
                    onCheckedChange = { enabled ->
                        UserModsRepository.setEnabled(mod, enabled)
                        AemilPaths.synchronizeUserMods(selectedVersion)
                        userMods = UserModsRepository.list(selectedVersion)
                    },
                )
                Text(mod.displayName, color = Color(0xFFE5ECEA))
            }
        }
        Text(label(language, "Changes apply the next time Minecraft starts.", "변경 사항은 Minecraft를 다시 실행할 때 적용됩니다."), color = Color(0xFF9DA9A8), fontSize = 12.sp)
    }
}

@Composable
private fun SettingsPage(
    language: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    Text(label(language, "Client Settings", "클라이언트 설정"), fontSize = 27.sp)
    Spacer(Modifier.height(20.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2225))) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(label(language, "Language", "언어"), fontSize = 18.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { onLanguageSelected(AppLanguage.ENGLISH) }) { Text("English") }
                OutlinedButton(onClick = { onLanguageSelected(AppLanguage.KOREAN) }) { Text("한국어") }
            }
            Text(label(language, "Aemil data directory", "Aemil 데이터 디렉터리"), fontSize = 18.sp)
            Text(AemilPaths.root.toString(), color = Color(0xFF9DA9A8))
            Text(
                label(language, "Minecraft versions and libraries install into the standard .minecraft directory. Sign in and launch from Minecraft Launcher.", "Minecraft 버전과 라이브러리는 표준 .minecraft 폴더에 설치됩니다. Minecraft Launcher에서 로그인하고 실행하세요."),
                color = Color(0xFF9DA9A8),
                fontSize = 12.sp,
            )
        }
    }
}

private fun label(language: AppLanguage, english: String, korean: String) =
    if (language == AppLanguage.KOREAN) korean else english
