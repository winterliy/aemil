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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
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
            Text("Fabric · 26.3", color = Color(0xFF92A09F), fontSize = 12.sp)
        }

        Column(Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 28.dp)) {
            when (page) {
                Page.PLAY -> PlayPage(language, selectedVersion, versions, versionsStatus) { selectedVersion = it }
                Page.MODS -> ModsPage(language)
                Page.SETTINGS -> SettingsPage(language) {
                    language = it
                    LauncherPreferences.setLanguage(it)
                }
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
    onVersionSelected: (String) -> Unit,
) {
    Text(label(language, "A Fabric client, ready to play", "Fabric 클라이언트를 실행하세요"), fontSize = 29.sp)
    Text(label(language, "Choose a Minecraft Java version", "Minecraft Java 버전을 선택하세요"), color = Color(0xFFAAB5B4))
    Spacer(Modifier.height(28.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2225))) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(label(language, "Profile · Aemil", "프로필 · Aemil"), fontSize = 19.sp)
            VersionPicker(language, versions, selectedVersion, onVersionSelected)
            Text(versionsStatus, color = Color(0xFF92A09F), fontSize = 12.sp)
            Text(label(language, "First development target: Minecraft 26.3", "첫 개발 대상: Minecraft 26.3"), color = Color(0xFFB5C2C0))
            Button(onClick = {}, enabled = false) { Text(label(language, "Launch setup in progress", "런처 연결 준비 중")) }
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
    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text("${label(language, "Minecraft Version", "Minecraft 버전")}: $selected  ▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            versions.take(120).forEach { version ->
                DropdownMenuItem(
                    text = { Text(version) },
                    onClick = { onSelected(version); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun ModsPage(language: AppLanguage) {
    Text(label(language, "Mods", "모드"), fontSize = 27.sp)
    Spacer(Modifier.height(18.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2225))) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Aemil Core · ${label(language, "Always active", "항상 활성")}", fontSize = 20.sp, color = Color(0xFF27D7B2))
            Text(label(language, "Integrated features cannot be toggled individually.", "내장 기능은 개별적으로 끌 수 없습니다."), color = Color(0xFFCBD4D2))
            Text(label(language, "Rendering · Memory · Networking · Background performance · Connected textures · Lighting", "렌더링 · 메모리 · 네트워크 · 백그라운드 성능 · 연결 텍스처 · 조명"), color = Color(0xFF9DA9A8))
        }
    }
    Spacer(Modifier.height(16.dp))
    Text(label(language, "Your Fabric Mods", "사용자 Fabric 모드"), fontSize = 19.sp)
    Text(label(language, "Only mods you add to the Aemil mods folder are listed and selectable here.", "Aemil mods 폴더에 추가한 모드만 여기에 표시하고 선택할 수 있습니다."), color = Color(0xFF9DA9A8))
    Spacer(Modifier.height(12.dp))
    OutlinedButton(onClick = { AemilPaths.openModsFolder() }) { Text(label(language, "Open Mods Folder", "mods 폴더 열기")) }
}

@Composable
private fun SettingsPage(language: AppLanguage, onLanguageSelected: (AppLanguage) -> Unit) {
    Text(label(language, "Client Settings", "클라이언트 설정"), fontSize = 27.sp)
    Spacer(Modifier.height(20.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2225))) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(label(language, "Language", "언어"), fontSize = 18.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { onLanguageSelected(AppLanguage.ENGLISH) }) { Text("English") }
                OutlinedButton(onClick = { onLanguageSelected(AppLanguage.KOREAN) }) { Text("한국어") }
            }
            Text(label(language, "Game directory", "게임 디렉터리"), fontSize = 18.sp)
            Text(AemilPaths.root.toString(), color = Color(0xFF9DA9A8))
            Text(label(language, "Microsoft sign-in will use the official account flow.", "Microsoft 로그인은 공식 계정 인증 흐름을 사용합니다."), color = Color(0xFF9DA9A8))
        }
    }
}

private fun label(language: AppLanguage, english: String, korean: String) =
    if (language == AppLanguage.KOREAN) korean else english
