@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
package com.carradio.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.carradio.R
import com.carradio.data.db.FavoriteStation
import com.carradio.player.PlayerState
import com.carradio.ui.favorites.SearchPageContent
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.launch

private fun Int.formatAsTimer(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToTimer: () -> Unit,
    isTimerRunning: Boolean = false,
    remainingSeconds: Int? = null
) {
    val favorites by viewModel.favorites.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()
    val favoritePageCount by viewModel.favoritePageCount.collectAsState()

    val favoriteUuids = remember(favorites) { favorites.map { it.uuid }.toSet() }

    val totalPages = favoritePageCount + 1  // last page = search
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    val slots: List<FavoriteStation?> = remember(favorites, favoritePageCount) {
        val map = favorites.associateBy { it.position }
        (0 until favoritePageCount * 8).map { map[it] }
    }

    // Move mode state
    var selectedForMove by remember { mutableStateOf<Int?>(null) }
    // Long press → show dialog
    var longPressPosition by remember { mutableStateOf<Int?>(null) }
    // TopAppBar menu
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.app_name))
                    }
                },
                actions = {
                    // Timer countdown (cliquable)
                    if (isTimerRunning && remainingSeconds != null) {
                        TextButton(onClick = onNavigateToTimer) {
                            Icon(
                                Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = remainingSeconds.formatAsTimer(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // Stop button
                    if (currentStation != null) {
                        IconButton(onClick = { viewModel.stopPlayback() }) {
                            Icon(Icons.Default.StopCircle,
                                contentDescription = stringResource(R.string.stop))
                        }
                    }
                    // Menu (⋮)
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings)) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.timer)) },
                                leadingIcon = {
                                    Icon(
                                        if (isTimerRunning) Icons.Default.HourglassTop else Icons.Default.HourglassEmpty,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToTimer()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_favorites_page)) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.addBlankPage()
                                }
                            )
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
        ) {
            // Move mode hint bar
            if (selectedForMove != null) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.move_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                if (page < favoritePageCount) {
                    // Favorites page
                    val pageSlots = slots.subList(page * 8, minOf(page * 8 + 8, slots.size))
                        .let { list ->
                            // Ensure exactly 8 elements
                            list + List(maxOf(0, 8 - list.size)) { null }
                        }
                    TileGrid(
                        slots = pageSlots,
                        pageOffset = page * 8,
                        currentStationUuid = currentStation?.uuid,
                        playerState = playerState,
                        selectedForMove = selectedForMove,
                        onTileTap = { position ->
                            when {
                                selectedForMove != null -> {
                                    // Move mode: swap selected with tapped position
                                    if (selectedForMove != position) {
                                        viewModel.swapFavorites(selectedForMove!!, position)
                                    }
                                    selectedForMove = null
                                }
                                slots.getOrNull(position) != null -> {
                                    // Play/stop station
                                    val station = slots[position]!!.toDomain()
                                    viewModel.onTileTapped(station)
                                }
                                else -> {
                                    // Empty tile → scroll to search page
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(favoritePageCount)
                                    }
                                }
                            }
                        },
                        onTileLongPress = { position ->
                            if (slots.getOrNull(position) != null) {
                                selectedForMove = null
                                longPressPosition = position
                            }
                        }
                    )
                } else {
                    // Search page (last page)
                    SearchPageContent(
                        favoriteUuids = favoriteUuids,
                        currentStationUuid = currentStation?.uuid,
                        onPlayStation = { viewModel.playStation(it) },
                        onStopPlayback = { viewModel.stopPlayback() },
                        onAddFavoriteStation = { viewModel.addFavoriteToNextAvailableSlot(it) },
                        onRemoveFavoriteStation = { viewModel.removeFavorite(it.uuid) }
                    )
                }
            }

            // Page indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(totalPages) { index ->
                    val selected = pagerState.currentPage == index
                    val isSearchPage = index == favoritePageCount
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.small,
                            color = when {
                                selected && isSearchPage -> MaterialTheme.colorScheme.secondary
                                selected -> MaterialTheme.colorScheme.primary
                                isSearchPage -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            }
                        ) {}
                    }
                }
            }

            // AdMob banner
            AdBanner()
        }
    }

    // Long press dialog
    longPressPosition?.let { pos ->
        val station = slots.getOrNull(pos)
        if (station != null) {
            AlertDialog(
                onDismissRequest = { longPressPosition = null },
                title = { Text(station.name, maxLines = 1) },
                text = null,
                confirmButton = {
                    TextButton(onClick = {
                        longPressPosition = null
                        selectedForMove = pos
                    }) {
                        Text(stringResource(R.string.move))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.removeFavoriteAtPosition(pos)
                        longPressPosition = null
                    }) {
                        Text(stringResource(R.string.delete))
                    }
                }
            )
        } else {
            longPressPosition = null
        }
    }
}

@Composable
private fun AdBanner() {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val adWidthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(adSize.height.dp),
        factory = { ctx ->
            AdView(ctx).apply {
                adUnitId = "ca-app-pub-6625569938836723/3454755420"
                setAdSize(adSize)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
private fun TileGrid(
    slots: List<FavoriteStation?>,
    pageOffset: Int,
    currentStationUuid: String?,
    playerState: PlayerState,
    selectedForMove: Int?,
    onTileTap: (Int) -> Unit,
    onTileLongPress: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until 2) {
                    val slotIndex = row * 2 + col
                    val globalPosition = pageOffset + slotIndex
                    val station = slots.getOrNull(slotIndex)
                    RadioTile(
                        station = station,
                        isActive = station != null && station.uuid == currentStationUuid,
                        playerState = playerState,
                        isSelectedForMove = selectedForMove == globalPosition,
                        onTap = { onTileTap(globalPosition) },
                        onLongPress = if (station != null) {
                            { onTileLongPress(globalPosition) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
