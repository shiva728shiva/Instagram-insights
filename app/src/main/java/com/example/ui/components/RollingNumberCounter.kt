package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IgTextPrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RollingNumberCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    startOffset: Int = 9,
    durationMillis: Int = 900,
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = IgTextPrimary
) {
    // Start value is offset slightly below (e.g. 1,370 when target is 1,379)
    val initialStart = remember(targetValue) {
        if (targetValue > startOffset) (targetValue - startOffset) else 0
    }
    var currentTarget by remember(targetValue) { mutableIntStateOf(initialStart) }

    LaunchedEffect(targetValue) {
        currentTarget = initialStart
        kotlinx.coroutines.delay(120)
        currentTarget = targetValue
    }

    val animatedValue by animateIntAsState(
        targetValue = currentTarget,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "viewsRollingAnim"
    )

    val formatted = remember(animatedValue) {
        NumberFormat.getNumberInstance(Locale.US).format(animatedValue)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        formatted.forEachIndexed { index, char ->
            if (char.isDigit()) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = tween(140),
                            initialOffsetY = { it }
                        ) togetherWith slideOutVertically(
                            animationSpec = tween(140),
                            targetOffsetY = { -it }
                        )
                    },
                    label = "charAnim_$index"
                ) { targetChar ->
                    Text(
                        text = targetChar.toString(),
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        color = color,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            } else {
                Text(
                    text = char.toString(),
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = color,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}
