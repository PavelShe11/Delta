package io.github.pavelshel1.delta.formula

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme

private const val TEAL = "#80FAF0"
private const val LAV = "#C4BAFF"
private const val BLANK_CLR = "#8FA3BA"

private const val ABSTRACT_LATEX =
    """\textcolor{$LAV}{\Delta P}=\dfrac{100}{\textcolor{$TEAL}{t}}\times\!\left[1-\dfrac{\textcolor{$TEAL}{P_{\text{кон}}}\times\textcolor{$TEAL}{T_{\text{нач}}}}{\textcolor{$TEAL}{P_{\text{нач}}}\times\textcolor{$TEAL}{T_{\text{кон}}}}\right]"""

// Exposed for LaTeX pre-measurement — not for display
internal val deltaAbstractLatex: String get() = ABSTRACT_LATEX

internal fun deltaSubstitutedWithResultLatex(
    t: String,
    pStart: String,
    pEnd: String,
    tStartK: String,
    tEndK: String,
    result: Double,
): String {
    fun fVal(s: String) = if (s.isNotEmpty()) """\textcolor{$TEAL}{$s}""" else """\textcolor{$BLANK_CLR}{\text{—}}"""
    val base = """\textcolor{$LAV}{\Delta P}=\dfrac{100}{${fVal(t)}}\times\!\left[1-\dfrac{${fVal(pEnd)}\times ${fVal(tStartK)}}{${fVal(pStart)}\times ${fVal(tEndK)}}\right]"""
    val resultStr = result.toBigDecimal().stripTrailingZeros().toPlainString()
    return "$base\\;=\\;\\textcolor{$TEAL}{$resultStr}\\;\\text{\\%/ч}"
}

@Composable
fun DeltaPAbstractFormula(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fixedHeight: Dp? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (fixedHeight != null) Modifier.height(fixedHeight)
                else Modifier.heightIn(min = 52.dp)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Latex(
            latex = ABSTRACT_LATEX,
            config = LatexConfig(fontSize = fontSize, theme = LatexTheme.dark()),
        )
    }
}

@Composable
fun DeltaPFormulaWithValues(
    t: String,
    pStart: String,
    pEnd: String,
    tStartK: String,
    tEndK: String,
    modifier: Modifier = Modifier,
    result: Double? = null,
    fontSize: TextUnit = 16.sp,
    fixedHeight: Dp? = null,
) {
    fun fVal(s: String) = if (s.isNotEmpty())
        """\textcolor{$TEAL}{$s}"""
    else
        """\textcolor{$BLANK_CLR}{\text{—}}"""

    val base = """\textcolor{$LAV}{\Delta P}=\dfrac{100}{${fVal(t)}}\times\!\left[1-\dfrac{${fVal(pEnd)}\times ${fVal(tStartK)}}{${fVal(pStart)}\times ${fVal(tEndK)}}\right]"""
    val latex = if (result != null) {
        val resultStr = result.toBigDecimal().stripTrailingZeros().toPlainString()
        "$base\\;=\\;\\textcolor{$TEAL}{$resultStr}\\;\\text{\\%/ч}"
    } else {
        base
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (fixedHeight != null) Modifier.height(fixedHeight)
                else Modifier.heightIn(min = 40.dp)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Latex(
            latex = latex,
            config = LatexConfig(fontSize = fontSize, theme = LatexTheme.dark()),
        )
    }
}
