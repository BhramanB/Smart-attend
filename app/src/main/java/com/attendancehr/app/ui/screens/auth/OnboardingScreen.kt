package com.attendancehr.app.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.attendancehr.app.ui.components.PrimaryButton
import com.attendancehr.app.ui.theme.Blue600
import com.attendancehr.app.ui.theme.Navy950
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val desc: String,
)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            title = "Track Attendance Easily",
            desc = "Check in/out in seconds with a clean daily view.",
        ),
        OnboardingPage(
            title = "Manage Leaves & Holidays",
            desc = "Apply leave, get approvals, and stay updated instantly.",
        ),
        OnboardingPage(
            title = "Team Visibility",
            desc = "See who’s present, absent, or on leave at a glance.",
        ),
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Navy950, Navy950.copy(alpha = 0.92f), Navy950),
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp),
            ) { page ->
                val p = pages[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OnboardingIllustration(modifier = Modifier.padding(top = 12.dp))
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = p.title,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = p.desc,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .padding(bottom = 14.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pages.size) { idx ->
                    val selected = idx == pagerState.currentPage
                    val alpha = animateFloatAsState(if (selected) 1f else 0.35f, label = "dotAlpha")
                    Box(
                        modifier = Modifier
                            .size(if (selected) 10.dp else 8.dp)
                            .alpha(alpha.value)
                            .background(Blue600, CircleShape),
                    )
                }
            }

            val isLast = pagerState.currentPage == pages.lastIndex
            PrimaryButton(
                text = if (isLast) "Get Started" else "Next",
                onClick = {
                    if (isLast) onGetStarted()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
            )
        }
    }
}

@Composable
private fun OnboardingIllustration(modifier: Modifier = Modifier) {
    // Minimal premium illustration placeholder (abstract cards + glow)
    Box(
        modifier = modifier
            .size(width = 320.dp, height = 280.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(Blue600.copy(alpha = 0.45f), Color.Transparent),
                ),
                shape = MaterialTheme.shapes.extraLarge,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 150.dp)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large),
        )
        Box(
            modifier = Modifier
                .padding(top = 44.dp)
                .size(width = 260.dp, height = 150.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.large),
        )
    }
}

