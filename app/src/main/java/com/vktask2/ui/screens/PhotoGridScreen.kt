package com.vktask2.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vktask2.UiState
import com.vktask2.R
import com.vktask2.data.Photo

@Composable
fun PhotoGridScreen(
    state: UiState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onPhotoClick: (Photo, Int) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            state.isInitialLoading -> LoadingState()
            state.error -> ErrorState(onRetry = onRetry)
            else -> PhotoGridContent(
                state = state,
                onLoadMore = onLoadMore,
                onPhotoClick = onPhotoClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGridContent(
    state: UiState,
    onLoadMore: () -> Unit,
    onPhotoClick: (Photo, Int) -> Unit
) {
    val listState: LazyGridState = rememberLazyGridState()

    val shouldLoadMore by derivedStateOf {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val totalCount = listState.layoutInfo.totalItemsCount
        totalCount > 0 && lastVisible >= totalCount - 4
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !state.isPaging && !state.endReached && !state.isInitialLoading) {
            onLoadMore()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
        ) {
            itemsIndexed(
                items = state.items,
                key = { _, item -> item.id }
            ) { index, photo ->
                PhotoCard(
                    photo = photo,
                    position = index,
                    onPhotoClick = onPhotoClick
                )
            }

            if (state.isPaging) {
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    PaginationLoading()
                }
            }

            if (state.pagingError) {
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    PaginationError(onRetry = onLoadMore)
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.error_message),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
private fun PhotoCard(
    photo: Photo,
    position: Int,
    onPhotoClick: (Photo, Int) -> Unit
) {
    val context = LocalContext.current
    val request = ImageRequest.Builder(context)
        .data(photo.downloadUrl)
        .crossfade(true)
        .build()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPhotoClick(photo, position) }
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = request,
            contentDescription = photo.author,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(photo.aspectRatio)
        )
        Text(
            text = photo.author,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
    }
}

@Composable
private fun PaginationLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}

@Composable
private fun PaginationError(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.pagination_error),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(id = R.string.try_again))
        }
    }
}

