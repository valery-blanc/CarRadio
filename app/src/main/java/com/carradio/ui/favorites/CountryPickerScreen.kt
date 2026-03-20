@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.carradio.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carradio.R
import com.carradio.domain.model.RadioStation

@Composable
fun CountryPickerScreen(
    slotPosition: Int,
    onBack: () -> Unit,
    onCountrySelected: (iso: String, name: String) -> Unit,
    onStationDirectlySelected: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
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

    LaunchedEffect(Unit) {
        viewModel.loadCountries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_station_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Section 1: Recherche par nom ──────────────────────────────
            item {
                Text(
                    stringResource(R.string.search_by_name),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = nameSearchQuery,
                    onValueChange = { viewModel.setNameSearchQuery(it) },
                    placeholder = { Text(stringResource(R.string.station_name_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
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
                is UiState.Loading -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(ns.message, color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp))
                    }
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
                            StationItem(station = station, onClick = {
                                viewModel.addFavorite(station, slotPosition) { onStationDirectlySelected() }
                            })
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
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
                is UiState.Loading -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(ts.message, color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp))
                    }
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
                            TagStationItem(station = station, onClick = {
                                viewModel.addFavorite(station, slotPosition) { onStationDirectlySelected() }
                            })
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    singleLine = true
                )
            }

            when (val state = countriesState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }
                is UiState.Error -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message)
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { viewModel.loadCountries() }) {
                                    Text(stringResource(R.string.retry))
                                }
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val featured = viewModel.featuredIsos
                    val all = state.data.filter {
                        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                    }
                    val featuredCountries = if (searchQuery.isBlank())
                        all.filter { it.iso in featured }.sortedBy { featured.indexOf(it.iso) }
                    else emptyList()
                    val otherCountries = all.filter { c ->
                        searchQuery.isNotBlank() || c.iso !in featured
                    }

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
                                supportingContent = {
                                    Text(stringResource(R.string.stations_count, country.stationCount))
                                },
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
                            supportingContent = {
                                Text(stringResource(R.string.stations_count, country.stationCount))
                            },
                            modifier = Modifier.clickable { onCountrySelected(country.iso, country.name) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun TagStationItem(station: RadioStation, onClick: () -> Unit) {
    val parts = buildList {
        if (station.country.isNotBlank()) add(station.country)
        if (station.tags.isNotBlank()) add(station.tags.split(",").take(3).joinToString(", "))
    }
    ListItem(
        headlineContent = { Text(station.name, maxLines = 1) },
        supportingContent = {
            if (parts.isNotEmpty()) {
                Text(parts.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}
