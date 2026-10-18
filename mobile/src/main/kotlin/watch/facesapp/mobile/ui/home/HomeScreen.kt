package watch.facesapp.mobile.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import watch.facesapp.mobile.data.db.FaceEntity
import watch.facesapp.mobile.ui.components.BottomVignette
import watch.facesapp.mobile.ui.components.FacesPillButton
import watch.facesapp.mobile.ui.components.TopInfoPill
import watch.facesapp.mobile.ui.components.TopVignette
import watch.facesapp.mobile.ui.components.WatchFacePreview
import watch.facesapp.mobile.ui.customise.ComplicationsSheet
import watch.facesapp.mobile.ui.theme.LuxuryBlack
import watch.facesapp.mobile.ui.theme.PureWhite
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCamera: (String) -> Unit,
    onNavigateToCustomise: (String) -> Unit,
    deepLinkUri: android.net.Uri? = null,
    onClearDeepLink: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pagerStateData by viewModel.pagerStateData.collectAsState()
    val isAmbient by viewModel.isAmbient.collectAsState()
    val calendar by viewModel.calendar.collectAsState()
    val faceMode by viewModel.faceMode.collectAsState()
    val activeComplications by viewModel.activeComplications.collectAsState()
    val complicationState by viewModel.complicationState.collectAsState()

    var showComplicationsSheet by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val items = pagerStateData.items

    val pagerState = rememberPagerState(
        initialPage = pagerStateData.activeIndex.coerceAtLeast(0),
        pageCount = { items.size.coerceAtLeast(1) }
    )

    // Ensure scroll aligns to the active watch face on initial load,
    // and whenever the active watch face updates (e.g. after creating/saving a new face or wearing again).
    var lastActiveFaceId by remember { mutableStateOf<String?>(null) }
    var lastAlignedIndex by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(pagerStateData.activeFaceId, pagerStateData.activeIndex, items.isNotEmpty()) {
        val currentActiveId = pagerStateData.activeFaceId
        val targetIndex = pagerStateData.activeIndex
        if (currentActiveId != null && items.isNotEmpty() && targetIndex in items.indices) {
            if (currentActiveId != lastActiveFaceId) {
                lastActiveFaceId = currentActiveId
                lastAlignedIndex = targetIndex
                pagerState.scrollToPage(targetIndex)
            } else if (lastAlignedIndex == 0 && targetIndex > 0 && !pagerState.isScrollInProgress) {
                // When historical wear sessions finish loading from Room on startup, adjust to active index
                lastAlignedIndex = targetIndex
                pagerState.scrollToPage(targetIndex)
            }
        }
    }

    // Share modal state
    var shareTarget by remember { mutableStateOf<FaceEntity?>(null) }
    var importBannerMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(deepLinkUri) {
        if (deepLinkUri != null) {
            viewModel.importSharedFace(deepLinkUri) { imported ->
                importBannerMessage = "Imported '${imported.name}' to your wrist!"
                onClearDeepLink()
            }
        }
    }

    LaunchedEffect(importBannerMessage) {
        if (importBannerMessage != null) {
            delay(4000L.milliseconds)
            importBannerMessage = null
        }
    }

    // Timeline modal sheet state
    var timelineTarget by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(faceId, faceName)
    val wearLogs by remember(timelineTarget) {
        if (timelineTarget != null) {
            viewModel.getWearLogsForFace(timelineTarget!!.first)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())
    val faceStats by remember(timelineTarget) {
        if (timelineTarget != null) {
            viewModel.getFaceStats(timelineTarget!!.first)
        } else {
            flowOf(FaceStats("", 0, 0))
        }
    }.collectAsState(initial = FaceStats("", 0, 0))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        // 1. Horizontal Pager with 0.75x depth transformation
        if (items.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val item = items.getOrNull(page)
                if (item != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                if (pageOffset < -1f || pageOffset > 1f) {
                                    alpha = 0f
                                } else if (pageOffset <= 0f) {
                                    alpha = 1f
                                    scaleX = 1f
                                    scaleY = 1f
                                    translationX = 0f
                                } else {
                                    alpha = 1f - pageOffset
                                    val scale = 0.75f + (1f - 0.75f) * (1f - pageOffset)
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = size.width * -pageOffset
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        WatchFacePreview(
                            config = item.config,
                            calendar = calendar,
                            isAmbient = isAmbient,
                            faceMode = faceMode,
                            complicationState = complicationState,
                            modifier = Modifier
                                .fillMaxSize(0.85f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.cycleFaceMode(activeComplications)
                                }
                        )
                    }
                }
            }
        }

        // 2. Top Vignette Scrim & Controls
        TopVignette(modifier = Modifier.align(Alignment.TopCenter))

        val currentItem = items.getOrNull(pagerState.currentPage)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ambient / Screen On toggle
            IconButton(onClick = { viewModel.toggleAmbientMode() }) {
                Icon(
                    imageVector = if (isAmbient) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle Ambient Mode",
                    tint = PureWhite
                )
            }

            // Universal Complications configuration
            IconButton(onClick = { showComplicationsSheet = true }) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = "Tap Complications Sequence",
                    tint = PureWhite
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Center Pill: Displays "On yer wrist", relative time ago (e.g. "1 day ago"), or template name
            val pillText = currentItem?.title ?: "Faces"
            TopInfoPill(
                text = pillText,
                modifier = Modifier.clickable {
                    when (currentItem) {
                        is HomePagerItem.HistoricalFace -> {
                            timelineTarget = Pair(currentItem.entity.id, currentItem.entity.name)
                        }
                        is HomePagerItem.ActiveFace -> {
                            timelineTarget = Pair(currentItem.entity.id, currentItem.entity.name)
                        }
                        else -> Unit
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Action icons based on face type
            when (currentItem) {
                is HomePagerItem.HistoricalFace -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Favorite toggle
                        IconButton(
                            onClick = { viewModel.toggleFavorite(currentItem.entity.id, currentItem.entity.isFavorite) }
                        ) {
                            Icon(
                                imageVector = if (currentItem.entity.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite Face",
                                tint = if (currentItem.entity.isFavorite) Color(0xFFD93550) else PureWhite
                            )
                        }

                        // Share creation via QR code / link
                        IconButton(
                            onClick = { shareTarget = currentItem.entity }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Face",
                                tint = PureWhite
                            )
                        }

                        // Delete from history
                        IconButton(
                            onClick = { viewModel.deleteFace(currentItem.entity.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Face",
                                tint = PureWhite.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                is HomePagerItem.ActiveFace -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(currentItem.entity.id, currentItem.entity.isFavorite) }
                        ) {
                            Icon(
                                imageVector = if (currentItem.entity.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite Face",
                                tint = if (currentItem.entity.isFavorite) Color(0xFFD93550) else PureWhite
                            )
                        }

                        IconButton(
                            onClick = { shareTarget = currentItem.entity }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Face",
                                tint = PureWhite
                            )
                        }
                    }
                }
                else -> {
                    // Placeholder spacing for symmetrical layout
                    Box(modifier = Modifier.padding(24.dp))
                }
            }
        }

        // Deep Link Import Notification Banner
        if (importBannerMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 96.dp)
            ) {
                TopInfoPill(text = importBannerMessage!!)
            }
        }

        // 3. Bottom Vignette Scrim & Contextual Action Buttons
        BottomVignette(modifier = Modifier.align(Alignment.BottomCenter))

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .fillMaxWidth()
        ) {
            when (currentItem) {
                is HomePagerItem.HistoricalFace -> {
                    // Historical watches: Can only be assigned as active ("WEAR AGAIN")
                    FacesPillButton(
                        text = "Wear Again",
                        onClick = {
                            viewModel.wearAgain(currentItem.entity)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerStateData.activeIndex)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is HomePagerItem.ActiveFace -> {
                    // Watch is currently active on wrist — no customize or snap photo buttons shown
                }
                is HomePagerItem.TemplateFace -> {
                    // Base template: Select and customize or snap photo
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FacesPillButton(
                            text = "Customise",
                            onClick = {
                                onNavigateToCustomise(currentItem.style.id)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                        FacesPillButton(
                            text = "Snap Photo",
                            icon = Icons.Default.CameraAlt,
                            onClick = {
                                onNavigateToCamera(currentItem.style.id)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                null -> Unit
            }
        }

        // 4. Wear Timeline Bottom Sheet Modal
        if (timelineTarget != null) {
            WearTimelineSheet(
                faceName = timelineTarget!!.second,
                wearLogs = wearLogs,
                stats = faceStats,
                onDismiss = { timelineTarget = null }
            )
        }

        // 5. Share Watch Face Bottom Sheet Modal
        if (shareTarget != null) {
            ShareWatchFaceSheet(
                face = shareTarget!!,
                onDismiss = { shareTarget = null }
            )
        }

        // 6. Universal Complications Bottom Sheet Modal
        if (showComplicationsSheet) {
            ComplicationsSheet(
                activeComplications = activeComplications,
                onToggleComplication = { viewModel.toggleComplication(it) },
                onReorderComplications = { viewModel.updateComplicationsOrder(it) },
                onDismiss = { showComplicationsSheet = false }
            )
        }
    }
}
