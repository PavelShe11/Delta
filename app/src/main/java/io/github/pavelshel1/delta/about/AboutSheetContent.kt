package io.github.pavelshel1.delta.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pavelshel1.delta.ui.theme.AppColors

@Composable
fun AboutSheetContent(appInfo: AppInfo, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )

        // Sheet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(AppColors.Surface)
                .navigationBarsPadding()
                .padding(bottom = 30.dp),
        ) {
            // Handle
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppColors.OnSurfaceVar.copy(alpha = 0.35f)),
                )
            }

            // Close button
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(end = 16.dp).size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = AppColors.OnSurfaceVar,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }

            // Hero
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Δ",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Background,
                    )
                }

                Spacer(Modifier.height(18.dp))
                Text(appInfo.appName, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = AppColors.OnSurface)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = appInfo.description,
                    fontSize = 13.sp,
                    color = AppColors.OnSurfaceVar,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))

                // Version chip
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColors.PrimaryContainer)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AppColors.Primary),
                    )
                    Text(
                        text = "Версия ${appInfo.version}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp,
                        color = AppColors.OnPrimaryContainer,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Meta grid
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppColors.OnSurface.copy(alpha = 0.03f))
                    .border(1.dp, AppColors.OutlineVar, RoundedCornerShape(18.dp)),
            ) {
                listOf(
                    "Автор" to appInfo.author,
                    "Сборка" to appInfo.buildLabel,
                    "Лицензия" to appInfo.license,
                ).forEachIndexed { i, (label, value) ->
                    if (i != 0) VerticalDivider(modifier = Modifier.height(64.dp), color = AppColors.OutlineVar)
                    Column(
                        modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            text = label.uppercase(),
                            fontSize = 10.sp,
                            color = AppColors.OnSurfaceVar,
                            letterSpacing = 0.8.sp,
                        )
                        Text(
                            text = value,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.OnSurface,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // GitHub button
            val uriHandler = LocalUriHandler.current
            Button(
                onClick = { uriHandler.openUri(appInfo.githubRepoUrl) },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.OnSurface),
            ) {
                Text(
                    text = "Открыть исходный код",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Background,
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = appInfo.githubProfileUrl.removePrefix("https://"),
                fontSize = 11.sp,
                color = AppColors.OnSurfaceVar.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { uriHandler.openUri(appInfo.githubProfileUrl) },
                textAlign = TextAlign.Center,
            )
        }
    }
}
