@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.carradio.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.carradio.R
import com.carradio.domain.model.RadioStation

/**
 * Page de recherche intégrée dans le HorizontalPager (dernière page).
 * Gère la liste des stations par pays en inline (sans navigation externe).
 */
@Composable
fun SearchPageContent(
    favoriteUuids: Set<String>,
    currentStationUuid: String?,
    onPlayStation: (RadioStation) -> Unit,
    onStopPlayback: () -> Unit,
    onAddFavoriteStation: (RadioStation) -> Unit,
    onRemoveFavoriteStation: (RadioStation) -> Unit,
    viewModel: FavoritesViewModel  // Caller provides the ViewModel — no hidden hiltViewModel()
) {
    // BUG-1: inline country station list — no external navigation
    var selectedCountry by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(selectedCountry) {
        if (selectedCountry != null) {
            viewModel.setSearchQuery("")
            viewModel.loadStations(selectedCountry!!.first)
        }
    }

    if (selectedCountry != null) {
        InlineStationList(
            countryName = selectedCountry!!.second,
            favoriteUuids = favoriteUuids,
            currentStationUuid = currentStationUuid,
            onBack = { selectedCountry = null },
            onPlayStation = onPlayStation,
            onStopPlayback = onStopPlayback,
            onAddFavoriteStation = onAddFavoriteStation,
            onRemoveFavoriteStation = onRemoveFavoriteStation,
            viewModel = viewModel
        )
    } else {
        SearchContent(
            favoriteUuids = favoriteUuids,
            currentStationUuid = currentStationUuid,
            onPlayStation = onPlayStation,
            onStopPlayback = onStopPlayback,
            onAddFavoriteStation = onAddFavoriteStation,
            onRemoveFavoriteStation = onRemoveFavoriteStation,
            onCountrySelected = { iso, name -> selectedCountry = iso to name },
            viewModel = viewModel
        )
    }
}

@Composable
private fun SearchContent(
    favoriteUuids: Set<String>,
    currentStationUuid: String?,
    onPlayStation: (RadioStation) -> Unit,
    onStopPlayback: () -> Unit,
    onAddFavoriteStation: (RadioStation) -> Unit,
    onRemoveFavoriteStation: (RadioStation) -> Unit,
    onCountrySelected: (iso: String, name: String) -> Unit,
    viewModel: FavoritesViewModel
) {
    val countriesState by viewModel.countriesState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val nameSearchQuery by viewModel.nameSearchQuery.collectAsState()
    val nameSearchState by viewModel.nameSearchState.collectAsState()
    val tagSearchQuery by viewModel.tagSearchQuery.collectAsState()
    val tagSuggestions by viewModel.tagSuggestions.collectAsState()
    val tagSearchState by viewModel.tagSearchState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var tagDropdownExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(tagSuggestions) { tagDropdownExpanded = tagSuggestions.isNotEmpty() }

    LaunchedEffect(Unit) { viewModel.loadCountries() }

    // Hoist filtering outside LazyColumn — remember avoids recomputing on every recomposition
    val featured = viewModel.featuredIsos
    val filteredAll = remember(countriesState, searchQuery) {
        (countriesState as? UiState.Success)?.data?.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
        } ?: emptyList()
    }
    val featuredCountries = remember(filteredAll, searchQuery) {
        if (searchQuery.isBlank())
            filteredAll.filter { it.iso in featured }.sortedBy { featured.indexOf(it.iso) }
        else emptyList()
    }
    val otherCountries = remember(filteredAll, searchQuery) {
        filteredAll.filter { c -> searchQuery.isNotBlank() || c.iso !in featured }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        // ── Section 1: Recherche par nom ──────────────────────────────
        item {
            Text(
                stringResource(R.string.search_by_name),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
            )
        }
        item {
            OutlinedTextField(
                value = nameSearchQuery,
                onValueChange = { viewModel.setNameSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.station_name_placeholder)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    viewModel.searchByName()
                }),
                trailingIcon = {
                    if (nameSearchQuery.isNotBlank()) {
                        TextButton(onClick = { viewModel.clearNameSearch() }) {
                            Text(stringResource(R.string.clear))
                        }
                    }
                }
            )
        }

        when (val ns = nameSearchState) {
            is UiState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> item {
                Text(ns.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            is UiState.Success -> {
                if (ns.data.isEmpty()) {
                    item {
                        Text(stringResource(R.string.no_station_found),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                } else {
                    items(ns.data) { station ->
                        SearchStationRow(
                            station = station,
                            isFavorite = station.uuid in favoriteUuids,
                            isPlaying = station.uuid == currentStationUuid,
                            onPlay = { onPlayStation(station) },
                            onStop = onStopPlayback,
                            onToggleFavorite = {
                                if (station.uuid in favoriteUuids) onRemoveFavoriteStation(station)
                                else onAddFavoriteStation(station)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
            null -> {}
        }

        // ── Section 2: Recherche par tag ──────────────────────────────
        item {
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            Text(
                stringResource(R.string.search_by_tag),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
        }
        item {
            ExposedDropdownMenuBox(
                expanded = tagDropdownExpanded,
                onExpandedChange = { tagDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = tagSearchQuery,
                    onValueChange = { viewModel.setTagSearchQuery(it) },
                    placeholder = { Text(stringResource(R.string.tag_placeholder)) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        tagDropdownExpanded = false
                        keyboardController?.hide()
                        val q = tagSearchQuery.trim()
                        if (q.isNotBlank()) viewModel.searchByTag(q)
                    }),
                    trailingIcon = {
                        if (tagSearchQuery.isNotBlank()) {
                            TextButton(onClick = { viewModel.clearTagSearch() }) {
                                Text(stringResource(R.string.clear))
                            }
                        }
                    }
                )
                if (tagSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = tagDropdownExpanded,
                        onDismissRequest = { tagDropdownExpanded = false }
                    ) {
                        tagSuggestions.forEach { tag ->
                            DropdownMenuItem(
                                text = { Text(tag) },
                                onClick = {
                                    tagDropdownExpanded = false
                                    keyboardController?.hide()
                                    viewModel.searchByTag(tag)
                                }
                            )
                        }
                    }
                }
            }
        }

        when (val ts = tagSearchState) {
            is UiState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> item {
                Text(ts.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            is UiState.Success -> {
                if (ts.data.isEmpty()) {
                    item {
                        Text(stringResource(R.string.no_station_found),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                } else {
                    items(ts.data) { station ->
                        SearchStationRow(
                            station = station,
                            isFavorite = station.uuid in favoriteUuids,
                            isPlaying = station.uuid == currentStationUuid,
                            onPlay = { onPlayStation(station) },
                            onStop = onStopPlayback,
                            onToggleFavorite = {
                                if (station.uuid in favoriteUuids) onRemoveFavoriteStation(station)
                                else onAddFavoriteStation(station)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
            null -> {}
        }

        // ── Section 3: Recherche par pays ─────────────────────────────
        item {
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            Text(
                stringResource(R.string.search_by_country),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.filter_countries)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true
            )
        }

        when (val state = countriesState) {
            is UiState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadCountries() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            is UiState.Success -> {
                // Use pre-computed lists (remembered above LazyColumn)
                if (featuredCountries.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.featured),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp))
                    }
                    items(featuredCountries) { country ->
                        ListItem(
                            headlineContent = { Text(country.name) },
                            supportingContent = { Text(stringResource(R.string.stations_count, country.stationCount)) },
                            modifier = Modifier.clickable { onCountrySelected(country.iso, country.name) }
                        )
                        HorizontalDivider()
                    }
                    item {
                        Text(stringResource(R.string.all_countries),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp))
                    }
                }
                items(otherCountries) { country ->
                    ListItem(
                        headlineContent = { Text(country.name) },
                        supportingContent = { Text(stringResource(R.string.stations_count, country.stationCount)) },
                        modifier = Modifier.clickable { onCountrySelected(country.iso, country.name) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun InlineStationList(
    countryName: String,
    favoriteUuids: Set<String>,
    currentStationUuid: String?,
    onBack: () -> Unit,
    onPlayStation: (RadioStation) -> Unit,
    onStopPlayback: () -> Unit,
    onAddFavoriteStation: (RadioStation) -> Unit,
    onRemoveFavoriteStation: (RadioStation) -> Unit,
    viewModel: FavoritesViewModel
) {
    val stationsState by viewModel.stationsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header avec bouton retour
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back))
            }
            Text(
                text = countryName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            singleLine = true
        )

        when (val state = stationsState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message)
                        Spacer(Modifier.height(8.dp))
                        // retry not available inline (no countryIso here); user can go back and retry
                    }
                }
            }
            is UiState.Success -> {
                val filtered = state.data.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered) { station ->
                        InlineStationRow(
                            station = station,
                            isFavorite = station.uuid in favoriteUuids,
                            isPlaying = station.uuid == currentStationUuid,
                            onPlay = { onPlayStation(station) },
                            onStop = onStopPlayback,
                            onToggleFavorite = {
                                if (station.uuid in favoriteUuids) onRemoveFavoriteStation(station)
                                else onAddFavoriteStation(station)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
internal fun SearchStationRow(
    station: RadioStation,
    isFavorite: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    ListItem(
        headlineContent = { Text(station.name, maxLines = 1) },
        supportingContent = {
            val parts = buildList {
                if (station.codec.isNotBlank()) add("${station.codec} · ${station.bitrate}k")
                if (station.country.isNotBlank()) add(station.country)
            }
            if (parts.isNotEmpty()) {
                Text(parts.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = if (isPlaying) onStop else onPlay) {
                    Icon(
                        if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = stringResource(if (isPlaying) R.string.stop else R.string.play),
                        tint = if (isPlaying) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.add_to_favorites),
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    )
}

@Composable
private fun InlineStationRow(
    station: RadioStation,
    isFavorite: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    ListItem(
        headlineContent = { Text(station.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            val parts = buildList {
                if (station.codec.isNotBlank()) add("${station.codec} · ${station.bitrate}k")
                if (station.subdivision.isNotBlank()) add(station.subdivision)
                if (station.country.isNotBlank()) add(station.country)
                if (station.languages.isNotBlank()) add(station.languages)
            }
            if (parts.isNotEmpty()) {
                Text(parts.joinToString(" · "),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        },
        leadingContent = {
            if (!station.faviconUrl.isNullOrBlank()) {
                AsyncImage(
                    model = station.faviconUrl,
                    contentDescription = station.name,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text(station.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = if (isPlaying) onStop else onPlay) {
                    Icon(
                        if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = stringResource(if (isPlaying) R.string.stop else R.string.play),
                        tint = if (isPlaying) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.add_to_favorites),
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    )
}
