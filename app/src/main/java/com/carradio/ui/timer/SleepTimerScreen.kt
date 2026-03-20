@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.carradio.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import com.carradio.R

@Composable
fun SleepTimerScreen(
    viewModel: SleepTimerViewModel,
    onBack: () -> Unit
) {
    val hours by viewModel.hours.collectAsState()
    val minutes by viewModel.minutes.collectAsState()
    val seconds by viewModel.seconds.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sleep_timer_title)) },
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPicker(count = 24, selectedIndex = hours, onIndexSelected = viewModel::setHours, label = "h")
                Text(text = ":", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 20.dp))
                WheelPicker(count = 60, selectedIndex = minutes, onIndexSelected = viewModel::setMinutes, label = "min")
                Text(text = ":", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 20.dp))
                WheelPicker(count = 60, selectedIndex = seconds, onIndexSelected = viewModel::setSeconds, label = "sec")
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { viewModel.startTimer(); onBack() },
                modifier = Modifier.fillMaxWidth(0.6f).height(56.dp)
            ) {
                Text(stringResource(R.string.start), style = MaterialTheme.typography.titleMedium)
            }

            if (isRunning) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = viewModel::cancelTimer,
                    modifier = Modifier.fillMaxWidth(0.6f).height(56.dp)
                ) {
                    Text(stringResource(R.string.cancel_timer), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun WheelPicker(
    count: Int,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val itemHeightDp = 56.dp
    val visibleCount = 5
    val halfVisible = visibleCount / 2

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(selectedIndex) {
        if (listState.firstVisibleItemIndex != selectedIndex) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .collect {
                val newIndex = listState.firstVisibleItemIndex.coerceIn(0, count - 1)
                onIndexSelected(newIndex)
            }
    }

    val centerIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(modifier = Modifier.width(80.dp).height(itemHeightDp * visibleCount)) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(itemHeightDp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            )
            LazyColumn(
                state = listState,
                flingBehavior = snapBehavior,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                userScrollEnabled = true
            ) {
                items(halfVisible) { Box(modifier = Modifier.height(itemHeightDp).width(80.dp)) }
                items(count) { index ->
                    val isCurrent = index == centerIndex
                    Box(
                        modifier = Modifier.height(itemHeightDp).width(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%02d".format(index),
                            style = if (isCurrent) MaterialTheme.typography.headlineMedium
                            else MaterialTheme.typography.bodyLarge,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                }
                items(halfVisible) { Box(modifier = Modifier.height(itemHeightDp).width(80.dp)) }
            }
        }
    }
}
