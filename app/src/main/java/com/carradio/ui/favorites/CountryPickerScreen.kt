@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.carradio.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CountryPickerScreen(
    slotPosition: Int,
    onBack: () -> Unit,
    onCountrySelected: (iso: String, name: String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val countriesState by viewModel.countriesState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCountries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choisir un pays") },
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Rechercher…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            when (val state = countriesState) {
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
                            Button(onClick = { viewModel.loadCountries() }) {
                                Text("Réessayer")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val featured = viewModel.featuredIsos
                    val all = state.data.filter {
                        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                    }

                    // Split: featured first (only if no search), then rest
                    val featuredCountries = if (searchQuery.isBlank())
                        all.filter { it.iso in featured }
                            .sortedBy { featured.indexOf(it.iso) }
                    else emptyList()

                    val otherCountries = all.filter { c ->
                        searchQuery.isNotBlank() || c.iso !in featured
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (featuredCountries.isNotEmpty()) {
                            item {
                                Text(
                                    "Recommandés",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(featuredCountries) { country ->
                                ListItem(
                                    headlineContent = { Text(country.name) },
                                    supportingContent = { Text("${country.stationCount} stations") },
                                    modifier = Modifier.clickable {
                                        onCountrySelected(country.iso, country.name)
                                    }
                                )
                                HorizontalDivider()
                            }
                            item {
                                Text(
                                    "Tous les pays",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                                )
                            }
                        }
                        items(otherCountries) { country ->
                            ListItem(
                                headlineContent = { Text(country.name) },
                                supportingContent = { Text("${country.stationCount} stations") },
                                modifier = Modifier.clickable {
                                    onCountrySelected(country.iso, country.name)
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
