package io.github.pavelshel1.delta.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme
import io.github.pavelshel1.delta.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date

private const val TEAL = "#80FAF0"
private const val LAV  = "#C4BAFF"

private const val FORMULA_ABSTRACT =
    """\textcolor{$LAV}{\Delta P}=\dfrac{100}{\textcolor{$TEAL}{t}}\times\!\left[1-\dfrac{\textcolor{$TEAL}{P_{\text{кон}}}\times\textcolor{$TEAL}{T_{\text{нач}}}}{\textcolor{$TEAL}{P_{\text{нач}}}\times\textcolor{$TEAL}{T_{\text{кон}}}}\right]"""

@Composable
fun HistoryCard(
    entry: HistoryEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val config = LatexConfig(fontSize = 14.sp, theme = LatexTheme.dark())
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current).get(0) ?: java.util.Locale.ROOT
    val ts = remember(entry.timestampMs, locale) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", locale).format(Date(entry.timestampMs))
    }
    val formulaWithResult = "${entry.latex}\\;=\\;\\textcolor{$TEAL}{${entry.resultLatex}}\\;\\text{\\%/ч}"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.SurfaceHighest),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = AppColors.OnSurfaceVar,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = ts,
                    fontSize = 11.sp,
                    color = AppColors.OnSurfaceVar,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "ΔP",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.RVar,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColors.RVar.copy(alpha = 0.12f))
                        .border(1.dp, AppColors.RVar.copy(alpha = 0.30f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Удалить",
                        tint = AppColors.OnSurface.copy(alpha = 0.45f),
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }

        HorizontalDivider(color = AppColors.OutlineVar)

        // Body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Latex(latex = FORMULA_ABSTRACT, config = config)

            HorizontalDivider(
                color = AppColors.OutlineVar,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            )

            Latex(latex = formulaWithResult, config = config)
        }
    }
}
