@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.carradio.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun FavoritesPickerScreen(
    onBack: () -> Unit,
    onAddFavorite: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val slotsMap = favorites.associateBy { it.position }

    var dialogSlot by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes favoris") },
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(2) { page ->
                Text(
                    text = "Page ${page + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(2) { col ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(4) { row ->
                                val position = page * 8 + row * 2 + col
                                val station = slotsMap[position]
                                MiniSlot(
                                    position = position,
                                    stationName = station?.name,
                                    faviconUrl = station?.faviconUrl,
                                    onClick = {
                                        if (station != null) dialogSlot = position
                                        else onAddFavorite(position)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    dialogSlot?.let { pos ->
        val station = slotsMap[pos]
        if (station != null) {
            AlertDialog(
                onDismissRequest = { dialogSlot = null },
                title = { Text(station.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                text = { Text("Que voulez-vous faire avec cette station ?") },
                confirmButton = {
                    TextButton(onClick = {
                        dialogSlot = null
                        onAddFavorite(pos)
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Modifier")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.removeAtPosition(pos)
                        dialogSlot = null
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Supprimer")
                    }
                }
            )
        }
    }
}

@Composable
private fun MiniSlot(
    position: Int,
    stationName: String?,
    faviconUrl: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (stationName == null) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Ajouter",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!faviconUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = faviconUrl,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = stationName,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
