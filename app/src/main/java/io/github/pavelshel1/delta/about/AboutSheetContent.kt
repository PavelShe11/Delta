package io.github.pavelshel1.delta.about

import android.os.SystemClock
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pavelshel1.delta.ui.theme.AppColors

private const val ADMIN_TAPS = 7
private const val ADMIN_TAP_GAP_MS = 1_000L

/** Тапы считаются серией, только если пауза между ними не больше [ADMIN_TAP_GAP_MS]; иначе серия начинается заново. */
internal fun nextAdminTapCount(count: Int, sinceLastTapMs: Long): Int =
    if (sinceLastTapMs > ADMIN_TAP_GAP_MS) 1 else count + 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSheetContent(component: AboutSheetComponent) {
    val appInfo = component.appInfo
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val animatedDismiss = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) component.onDismissRequested()
        }
        Unit
    }
    ModalBottomSheet(
        onDismissRequest = component.onDismissRequested,
        sheetState = sheetState,
        containerColor = AppColors.Surface,
        contentColor = AppColors.OnSurface,
        dragHandle = null,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-90).dp)
                    .size(width = 320.dp, height = 180.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x2E2FD9C0),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AppColors.OnSurfaceVar.copy(alpha = 0.35f)),
                    )
                }
                Spacer(Modifier.height(32.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val context = LocalContext.current
                val iconBitmap = remember {
                    val drawable = androidx.core.content.res.ResourcesCompat.getDrawable(
                        context.resources,
                        io.github.pavelshel1.delta.R.mipmap.ic_launcher,
                        context.theme,
                    )!!
                    val size = 192
                    val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                    val bmpCanvas = android.graphics.Canvas(bmp)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                        drawable is android.graphics.drawable.AdaptiveIconDrawable
                    ) {
                        val scaled = (size * 108f / 72f).toInt()
                        val offset = -((scaled - size) / 2)
                        val right = offset + scaled
                        drawable.background?.apply { setBounds(offset, offset, right, right); draw(bmpCanvas) }
                        drawable.foreground?.apply { setBounds(offset, offset, right, right); draw(bmpCanvas) }
                    } else {
                        drawable.setBounds(0, 0, size, size)
                        drawable.draw(bmpCanvas)
                    }
                    bmp.asImageBitmap()
                }
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, AppColors.ActiveGlow, RoundedCornerShape(20.dp)),
                ) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
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

                var versionTaps by remember { mutableIntStateOf(0) }
                var lastVersionTapMs by remember { mutableLongStateOf(0L) }
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColors.PrimaryContainer)
                        // ponytail: защита только скрытностью; если пользователи начнут менять настройки — добавить PIN
                        .clickable(interactionSource = null, indication = null) {
                            val now = SystemClock.uptimeMillis()
                            versionTaps = nextAdminTapCount(versionTaps, now - lastVersionTapMs)
                            lastVersionTapMs = now
                            if (versionTaps >= ADMIN_TAPS) {
                                versionTaps = 0
                                component.onSmtpSettingsRequested()
                            }
                        }
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

            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppColors.OnSurface.copy(alpha = 0.03f))
                    .border(1.dp, AppColors.OutlineVar, RoundedCornerShape(18.dp)),
            ) {
                buildList {
                    add("Автор" to appInfo.author)
                    add("Сборка от" to appInfo.buildLabel)
                    if (appInfo.license.isNotBlank()) add("Лицензия" to appInfo.license)
                }.forEachIndexed { i, (label, value) ->
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
                Icon(
                    painter = painterResource(io.github.pavelshel1.delta.R.drawable.ic_github),
                    contentDescription = null,
                    tint = AppColors.Background,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
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

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable(onClick = animatedDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = AppColors.OnSurfaceVar,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}
