package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBg
import com.example.ui.theme.ElectricTeal

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            title = "Make Every Step Count",
            subtitle = "Turn everyday movement into friendly competition with friends across any smartwatch brand.",
            icon = Icons.Default.DirectionsRun,
            accentColor = CyberLime
        ),
        OnboardingPage(
            title = "Smartwatch Freedom",
            subtitle = "Works seamlessly whether you use Samsung Galaxy Fit3, Fastrack Reflex SW2, Pixel Watch, or phone sensors via Health Connect.",
            icon = Icons.Default.Watch,
            accentColor = ElectricTeal
        ),
        OnboardingPage(
            title = "1-on-1 Showdowns",
            subtitle = "Challenge rivals to daily sprints, track live step differences, climb private group leaderboards, and celebrate consistency.",
            icon = Icons.Default.SportsScore,
            accentColor = CyberLime
        )
    )

    val currentPage = pages[step]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp)
            .testTag("onboarding_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onComplete,
                modifier = Modifier.testTag("skip_onboarding_button")
            ) {
                Text(
                    text = "Skip to App",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Center Hero Graphic & Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(currentPage.accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = currentPage.icon,
                    contentDescription = null,
                    tint = currentPage.accentColor,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = currentPage.title,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = currentPage.subtitle,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        // Bottom Controls: Page Dots + Next / Finish
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == step) 24.dp else 8.dp, 8.dp)
                            .background(
                                if (index == step) CyberLime else Color(0xFF222B3A),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (step < pages.size - 1) {
                        step++
                    } else {
                        onComplete()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberLime,
                    contentColor = DarkBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (step == pages.size - 1) "Get Started" else "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color
)
