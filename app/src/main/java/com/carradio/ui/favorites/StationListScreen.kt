@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.carradio.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.carradio.R
import com.carradio.domain.model.RadioStation

@Composable
fun StationListScreen(
    countryIso: String,
    countryName: String,
    slotPosition: Int,
    onBack: () -> Unit,
    onStationSelected: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val stationsState by viewModel.stationsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(countryIso) {
        viewModel.setSearchQuery("")
        viewModel.loadStations(countryIso)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(countryName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            Button(onClick = { viewModel.loadStations(countryIso) }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val filtered = state.data.filter {
                        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered) { station ->
                            StationItem(
                                station = station,
                                onClick = {
                                    viewModel.addFavorite(station, slotPosition) {
                                        onStationSelected()
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun StationItem(station: RadioStation, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(station.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            val parts = buildList {
                if (station.codec.isNotBlank()) add("${station.codec} · ${station.bitrate}k")
                if (station.subdivision.isNotBlank()) add(station.subdivision)
                if (station.country.isNotBlank()) add(station.country)
                if (station.languages.isNotBlank()) add(station.languages)
            }
            if (parts.isNotEmpty()) {
                Text(
                    parts.joinToString(" · "),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        leadingContent = {
            if (!station.faviconUrl.isNullOrBlank()) {
                AsyncImage(
                    model = station.faviconUrl,
                    contentDescription = station.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = station.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}
