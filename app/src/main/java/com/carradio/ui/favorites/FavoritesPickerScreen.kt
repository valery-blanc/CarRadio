@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.carradio.ui.favorites

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.carradio.R

private const val PAGES = 4
private const val SLOTS_PER_PAGE = 8
private const val TOTAL_SLOTS = PAGES * SLOTS_PER_PAGE

@Composable
fun FavoritesPickerScreen(
    onBack: () -> Unit,
    onAddFavorite: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val slotsMap = favorites.associateBy { it.position }

    var dialogSlot by remember { mutableStateOf<Int?>(null) }
    var selectedPosition by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_favorites)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (selectedPosition != null) {
                        IconButton(onClick = {
                            viewModel.removeAtPosition(selectedPosition!!)
                            selectedPosition = null
                        }) {
                            Icon(Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete))
                        }
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
            Text(
                text = stringResource(R.string.favorites_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            repeat(PAGES) { page ->
                Text(
                    text = stringResource(R.string.page_label, page + 1),
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
                                val position = page * SLOTS_PER_PAGE + row * 2 + col
                                val station = slotsMap[position]
                                val isSelected = selectedPosition == position
                                MiniSlot(
                                    stationName = station?.name,
                                    faviconUrl = station?.faviconUrl,
                                    isSelected = isSelected,
                                    onClick = {
                                        when {
                                            selectedPosition != null -> {
                                                val from = selectedPosition!!
                                                if (from != position) {
                                                    viewModel.swapFavorites(from, position)
                                                }
                                                selectedPosition = null
                                            }
                                            station != null -> dialogSlot = position
                                            else -> onAddFavorite(position)
                                        }
                                    },
                                    onLongClick = {
                                        if (station != null) {
                                            selectedPosition =
                                                if (selectedPosition == position) null else position
                                        }
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
                text = { Text(stringResource(R.string.station_action_prompt)) },
                confirmButton = {
                    TextButton(onClick = {
                        dialogSlot = null
                        onAddFavorite(pos)
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.modify))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.removeAtPosition(pos)
                        dialogSlot = null
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.delete))
                    }
                }
            )
        }
    }
}

@Composable
private fun MiniSlot(
    stationName: String?,
    faviconUrl: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val scale = if (isSelected) 1.04f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (stationName == null) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.add_station),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!faviconUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = faviconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp)),
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
