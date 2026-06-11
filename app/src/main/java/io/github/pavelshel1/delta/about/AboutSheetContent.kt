package io.github.pavelshel1.delta.about

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pavelshel1.delta.ui.theme.AppColors

private val GitHubIcon: ImageVector = ImageVector.Builder(
    name = "GitHub",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(AppColors.Background), pathFillType = PathFillType.NonZero) {
        moveTo(12f, 2f)
        curveTo(6.477f, 2f, 2f, 6.477f, 2f, 12f)
        curveToRelative(0f, 4.418f, 2.865f, 8.166f, 6.839f, 9.489f)
        curveToRelative(0.5f, 0.092f, 0.682f, -0.217f, 0.682f, -0.482f)
        curveToRelative(0f, -0.237f, -0.009f, -0.868f, -0.013f, -1.703f)
        curveToRelative(-2.782f, 0.605f, -3.369f, -1.342f, -3.369f, -1.342f)
        curveToRelative(-0.454f, -1.155f, -1.11f, -1.463f, -1.11f, -1.463f)
        curveToRelative(-0.908f, -0.62f, 0.069f, -0.608f, 0.069f, -0.608f)
        curveToRelative(1.003f, 0.07f, 1.531f, 1.032f, 1.531f, 1.032f)
        curveToRelative(0.892f, 1.53f, 2.341f, 1.088f, 2.91f, 0.832f)
        curveToRelative(0.092f, -0.647f, 0.35f, -1.088f, 0.636f, -1.338f)
        curveToRelative(-2.22f, -0.253f, -4.555f, -1.113f, -4.555f, -4.951f)
        curveToRelative(0f, -1.093f, 0.39f, -1.988f, 1.029f, -2.688f)
        curveToRelative(-0.103f, -0.253f, -0.446f, -1.272f, 0.098f, -2.65f)
        curveToRelative(0f, 0f, 0.84f, -0.27f, 2.75f, 1.026f)
        arcTo(9.564f, 9.564f, 0f, false, true, 12f, 6.844f)
        arcToRelative(9.59f, 9.59f, 0f, false, true, 2.504f, 0.337f)
        curveToRelative(1.909f, -1.296f, 2.747f, -1.027f, 2.747f, -1.027f)
        curveToRelative(0.546f, 1.379f, 0.202f, 2.398f, 0.1f, 2.651f)
        curveToRelative(0.64f, 0.7f, 1.028f, 1.595f, 1.028f, 2.688f)
        curveToRelative(0f, 3.848f, -2.339f, 4.695f, -4.566f, 4.943f)
        curveToRelative(0.359f, 0.309f, 0.678f, 0.92f, 0.678f, 1.855f)
        curveToRelative(0f, 1.338f, -0.012f, 2.419f, -0.012f, 2.745f)
        curveToRelative(0f, 0.268f, 0.18f, 0.58f, 0.688f, 0.482f)
        arcTo(10.02f, 10.02f, 0f, false, false, 22f, 12f)
        curveToRelative(0f, -5.523f, -4.477f, -10f, -10f, -10f)
        close()
    }
}.build()

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
            if (!sheetState.isVisible) component.onDismiss()
        }
        Unit
    }
    ModalBottomSheet(
        onDismissRequest = component::onDismiss,
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
                Icon(
                    imageVector = GitHubIcon,
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

            // Close button
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
