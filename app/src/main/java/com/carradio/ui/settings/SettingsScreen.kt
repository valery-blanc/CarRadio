@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.carradio.ui.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carradio.R

private data class Language(val code: String, val flag: String, val name: String)

private val LANGUAGES = listOf(
    Language("en", "🇬🇧", "English"),
    Language("fr", "🇫🇷", "Français"),
    Language("es", "🇪🇸", "Español"),
    Language("pt", "🇵🇹", "Português"),
    Language("de", "🇩🇪", "Deutsch"),
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onManageFavorites: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val favoritesCount by viewModel.favoritesCount.collectAsState()

    var backgroundPlayback by remember { mutableStateOf(viewModel.backgroundPlayback) }
    var screenAlwaysOn by remember { mutableStateOf(viewModel.screenAlwaysOn) }
    var preferredQuality by remember { mutableStateOf(viewModel.preferredQuality) }
    var currentLanguage by remember { mutableStateOf(viewModel.appLanguage) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Section Favoris
            SettingsSectionHeader(stringResource(R.string.section_favorites))
            ListItem(
                headlineContent = { Text(stringResource(R.string.manage_favorites)) },
                supportingContent = {
                    Text(stringResource(R.string.favorites_count, favoritesCount))
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable { onManageFavorites() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section Lecture
            SettingsSectionHeader(stringResource(R.string.playback_section))
            ListItem(
                headlineContent = { Text(stringResource(R.string.background_playback)) },
                trailingContent = {
                    Switch(
                        checked = backgroundPlayback,
                        onCheckedChange = {
                            backgroundPlayback = it
                            viewModel.backgroundPlayback = it
                        }
                    )
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.preferred_quality)) },
                supportingContent = {
                    Text(when (preferredQuality) {
                        "high" -> stringResource(R.string.quality_high)
                        "low" -> stringResource(R.string.quality_low)
                        else -> stringResource(R.string.quality_normal)
                    })
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable { showQualityDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section Affichage
            SettingsSectionHeader(stringResource(R.string.display_section))
            ListItem(
                headlineContent = { Text(stringResource(R.string.screen_always_on)) },
                trailingContent = {
                    Switch(
                        checked = screenAlwaysOn,
                        onCheckedChange = {
                            screenAlwaysOn = it
                            viewModel.screenAlwaysOn = it
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section Langue
            SettingsSectionHeader(stringResource(R.string.language_section))
            val selectedLang = LANGUAGES.find { it.code == currentLanguage } ?: LANGUAGES[0]
            ListItem(
                headlineContent = { Text(stringResource(R.string.language_label)) },
                supportingContent = { Text("${selectedLang.flag}  ${selectedLang.name}") },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable { showLanguageDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section À propos
            SettingsSectionHeader(stringResource(R.string.about_section))
            ListItem(
                headlineContent = { Text(stringResource(R.string.version_label)) },
                supportingContent = { Text("1.0") }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.about_data_label)) },
                supportingContent = { Text("radio-browser.info") }
            )
        }
    }

    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text(stringResource(R.string.preferred_quality)) },
            text = {
                Column {
                    listOf(
                        "high" to stringResource(R.string.quality_high),
                        "normal" to stringResource(R.string.quality_normal),
                        "low" to stringResource(R.string.quality_low)
                    ).forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    preferredQuality = key
                                    viewModel.preferredQuality = key
                                    showQualityDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = preferredQuality == key, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language_label)) },
            text = {
                Column {
                    LANGUAGES.forEach { lang ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentLanguage = lang.code
                                    viewModel.appLanguage = lang.code
                                    showLanguageDialog = false
                                    (context as? Activity)?.recreate()
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = currentLanguage == lang.code, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${lang.flag}  ${lang.name}")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}
