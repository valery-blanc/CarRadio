@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.carradio.ui.settings

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onManageFavorites: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val favoritesCount by viewModel.favoritesCount.collectAsState()

    var backgroundPlayback by remember { mutableStateOf(viewModel.backgroundPlayback) }
    var screenAlwaysOn by remember { mutableStateOf(viewModel.screenAlwaysOn) }
    var preferredQuality by remember { mutableStateOf(viewModel.preferredQuality) }
    var showQualityDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
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
            SettingsSectionHeader("Mes favoris")
            ListItem(
                headlineContent = { Text("Gérer mes favoris") },
                supportingContent = { Text("$favoritesCount/16 stations configurées") },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable { onManageFavorites() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section Lecture
            SettingsSectionHeader("Lecture")
            ListItem(
                headlineContent = { Text("Continuer la lecture en arrière-plan") },
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
                headlineContent = { Text("Qualité préférée") },
                supportingContent = {
                    Text(when (preferredQuality) {
                        "high" -> "Haute (192k+)"
                        "low" -> "Basse (<128k)"
                        else -> "Normale (128k)"
                    })
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable { showQualityDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section Affichage
            SettingsSectionHeader("Affichage")
            ListItem(
                headlineContent = { Text("Écran toujours allumé") },
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

            // Section À propos
            SettingsSectionHeader("À propos")
            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("1.0") }
            )
            ListItem(
                headlineContent = { Text("Données stations") },
                supportingContent = { Text("radio-browser.info") }
            )
        }
    }

    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Qualité préférée") },
            text = {
                Column {
                    listOf("high" to "Haute (192k+)", "normal" to "Normale (128k)", "low" to "Basse (<128k)").forEach { (key, label) ->
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
