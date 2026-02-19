package com.example.pokedex


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.foundation.gestures.waitForUpOrCancellation


// Screen positions as fractions of image dimensions (1812 x 2176)
private const val LEFT_SCREEN_LEFT   = 141f / 1812f
private const val LEFT_SCREEN_TOP    = 428f / 2176f
private const val LEFT_SCREEN_RIGHT  = 674f / 1812f
private const val LEFT_SCREEN_BOTTOM = 954f / 2176f

private const val RIGHT_SCREEN_LEFT   = 1076f / 1812f
private const val RIGHT_SCREEN_TOP    = 435f  / 2176f
private const val RIGHT_SCREEN_RIGHT  = 1675f / 1812f
private const val RIGHT_SCREEN_BOTTOM = 1204f / 2176f

private const val SMALL_LEFT_L  = 115f  / 1812f
private const val SMALL_LEFT_T  = 1913f / 2176f
private const val SMALL_LEFT_R  = 373f  / 1812f
private const val SMALL_LEFT_B  = 2011f / 2176f

private const val SMALL_MID_L   = 448f  / 1812f
private const val SMALL_MID_T   = 1913f / 2176f
private const val SMALL_MID_R   = 703f  / 1812f
private const val SMALL_MID_B   = 2011f / 2176f

private const val SMALL_RIGHT_L = 1421f / 1812f
private const val SMALL_RIGHT_T = 1886f / 2176f
private const val SMALL_RIGHT_R = 1698f / 1812f
private const val SMALL_RIGHT_B = 1984f / 2176f

private const val BTN_LEFT_L  = 483f / 1812f
private const val BTN_LEFT_T  = 1708f / 2176f
private const val BTN_LEFT_R  = 550f / 1812f
private const val BTN_LEFT_B  = 1787f / 2176f

private const val BTN_RIGHT_L = 571f / 1812f
private const val BTN_RIGHT_T = 1708f / 2176f
private const val BTN_RIGHT_R = 640f / 1812f
private const val BTN_RIGHT_B = 1787f / 2176f

private const val T9_ROW1_TOP    = 1322f / 2176f
private const val T9_ROW1_BOTTOM = 1387f / 2176f
private const val T9_ROW2_TOP    = 1396f / 2176f
private const val T9_ROW2_BOTTOM = 1459f / 2176f

private val T9_COL_STARTS = listOf(66f, 188f, 330f, 471f, 611f).map { it / 1812f }
private val T9_COL_ENDS   = listOf(188f, 330f, 471f, 611f, 730f).map { it / 1812f }

// Left screen nav buttons
private const val NAV_BTN_LEFT_L  = 128f / 1812f
private const val NAV_BTN_LEFT_T  = 1092f / 2176f
private const val NAV_BTN_LEFT_R  = 270f / 1812f
private const val NAV_BTN_LEFT_B  = 1115f / 2176f

private const val NAV_BTN_RIGHT_L = 335f / 1812f
private const val NAV_BTN_RIGHT_T = 1092f / 2176f
private const val NAV_BTN_RIGHT_R = 486f / 1812f
private const val NAV_BTN_RIGHT_B = 1115f / 2176f

// D-pad
private const val DPAD_UP_L    = 1168f / 1812f
private const val DPAD_UP_T    = 1798f / 2176f
private const val DPAD_UP_R    = 1220f / 1812f
private const val DPAD_UP_B    = 1873f / 2176f

private const val DPAD_DOWN_L  = 1168f / 1812f
private const val DPAD_DOWN_T  = 1947f / 2176f
private const val DPAD_DOWN_R  = 1220f / 1812f
private const val DPAD_DOWN_B  = 2019f / 2176f

private const val DPAD_LEFT_L  = 1101f / 1812f
private const val DPAD_LEFT_T  = 1881f / 2176f
private const val DPAD_LEFT_R  = 1162f / 1812f
private const val DPAD_LEFT_B  = 1937f / 2176f

private const val DPAD_RIGHT_L = 1229f / 1812f
private const val DPAD_RIGHT_T = 1885f / 2176f
private const val DPAD_RIGHT_R = 1291f / 1812f
private const val DPAD_RIGHT_B = 1937f / 2176f

private const val CIRC_LEFT_CX  = 578f / 1812f
private const val CIRC_RIGHT_CX = 633f / 1812f
private const val CIRC_CY       = 1108f / 2176f
private const val CIRC_R        = 16f / 1812f
private val T9_LABELS = listOf(
    listOf("1", "-★✦"),
    listOf("2", "ABC"),
    listOf("3", "DEF"),
    listOf("4", "GHI"),
    listOf("5", "JKL"),
    listOf("6", "MNO"),
    listOf("7", "PQRS"),
    listOf("8", "TUV"),
    listOf("9","WXYZ"),
    listOf("0", "SPC ⌫")
)

enum class TypeEffectivenessPage { WEAK, RESIST, IMMUNE }

@Composable
fun PokedexLayout(
    listContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit,
    smallLeftContent: @Composable () -> Unit = {},
    smallMidContent: @Composable (TypeEffectivenessPage) -> Unit = {},
    smallRightContent: @Composable () -> Unit = {},
    onT9Key: (Int) -> Unit = {},
    onT9Button10Press: () -> Unit = {},
    onT9Button10Release: () -> Unit = {},
    onNavUp: () -> Unit = {},
    onNavDown: () -> Unit = {},
    onDpadUp: () -> Unit = {},
    onDpadDown: () -> Unit = {},
    onDpadLeft: () -> Unit = {},
    onDpadRight: () -> Unit = {},
    onCircleLeft: () -> Unit = {},
    onCircleRight: () -> Unit = {}

) {
    var currentPage by remember { mutableStateOf(TypeEffectivenessPage.WEAK) }


    @Suppress("UnusedBoxWithConstraintsScope")
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val imgW = maxWidth
        val imgH = maxHeight

        Image(
            painter = painterResource(id = R.drawable.open_dex),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Left large screen
        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * LEFT_SCREEN_LEFT, y = imgH * LEFT_SCREEN_TOP)
                .width(imgW * (LEFT_SCREEN_RIGHT - LEFT_SCREEN_LEFT))
                .height(imgH * (LEFT_SCREEN_BOTTOM - LEFT_SCREEN_TOP))
                .clip(RoundedCornerShape(5.dp))
        ) { listContent() }

        // Right large screen
        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * RIGHT_SCREEN_LEFT, y = imgH * RIGHT_SCREEN_TOP)
                .width(imgW * (RIGHT_SCREEN_RIGHT - RIGHT_SCREEN_LEFT))
                .height(imgH * (RIGHT_SCREEN_BOTTOM - RIGHT_SCREEN_TOP))
                .clip(RoundedCornerShape(5.dp))
        ) { detailContent() }

        // Small screen — capture rate
        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * SMALL_LEFT_L, y = imgH * SMALL_LEFT_T)
                .width(imgW * (SMALL_LEFT_R - SMALL_LEFT_L))
                .height(imgH * (SMALL_LEFT_B - SMALL_LEFT_T))
                .clip(RoundedCornerShape(4.dp))
        ) { smallLeftContent() }

        // Small screen — type effectiveness
        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * SMALL_MID_L, y = imgH * SMALL_MID_T)
                .width(imgW * (SMALL_MID_R - SMALL_MID_L))
                .height(imgH * (SMALL_MID_B - SMALL_MID_T))
                .clip(RoundedCornerShape(4.dp))
        ) { smallMidContent(currentPage) }

        // Small screen — Pokémon ID
        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * SMALL_RIGHT_L, y = imgH * SMALL_RIGHT_T)
                .width(imgW * (SMALL_RIGHT_R - SMALL_RIGHT_L))
                .height(imgH * (SMALL_RIGHT_B - SMALL_RIGHT_T))
                .clip(RoundedCornerShape(4.dp))
        ) { smallRightContent() }

        // < button
        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * BTN_LEFT_L, y = imgH * BTN_LEFT_T)
                .width(imgW * (BTN_LEFT_R - BTN_LEFT_L))
                .height(imgH * (BTN_LEFT_B - BTN_LEFT_T))
                .clickable {
                    currentPage = when (currentPage) {
                        TypeEffectivenessPage.WEAK   -> TypeEffectivenessPage.IMMUNE
                        TypeEffectivenessPage.RESIST -> TypeEffectivenessPage.WEAK
                        TypeEffectivenessPage.IMMUNE -> TypeEffectivenessPage.RESIST
                    }
                }
        )

        // > button
        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * BTN_RIGHT_L, y = imgH * BTN_RIGHT_T)
                .width(imgW * (BTN_RIGHT_R - BTN_RIGHT_L))
                .height(imgH * (BTN_RIGHT_B - BTN_RIGHT_T))
                .clickable {
                    currentPage = when (currentPage) {
                        TypeEffectivenessPage.WEAK   -> TypeEffectivenessPage.RESIST
                        TypeEffectivenessPage.RESIST -> TypeEffectivenessPage.IMMUNE
                        TypeEffectivenessPage.IMMUNE -> TypeEffectivenessPage.WEAK
                    }
                }
        )

        // Left screen nav buttons
        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * NAV_BTN_LEFT_L, y = imgH * NAV_BTN_LEFT_T)
                .width(imgW * (NAV_BTN_LEFT_R - NAV_BTN_LEFT_L))
                .height(imgH * (NAV_BTN_LEFT_B - NAV_BTN_LEFT_T))
                .clickable { onNavUp() }
        )

        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * NAV_BTN_RIGHT_L, y = imgH * NAV_BTN_RIGHT_T)
                .width(imgW * (NAV_BTN_RIGHT_R - NAV_BTN_RIGHT_L))
                .height(imgH * (NAV_BTN_RIGHT_B - NAV_BTN_RIGHT_T))
                .clickable { onNavDown() }
        )

        // D-pad
        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * DPAD_UP_L, y = imgH * DPAD_UP_T)
                .width(imgW * (DPAD_UP_R - DPAD_UP_L))
                .height(imgH * (DPAD_UP_B - DPAD_UP_T))
                .clickable { onDpadUp() }
        )

        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * DPAD_DOWN_L, y = imgH * DPAD_DOWN_T)
                .width(imgW * (DPAD_DOWN_R - DPAD_DOWN_L))
                .height(imgH * (DPAD_DOWN_B - DPAD_DOWN_T))
                .clickable { onDpadDown() }
        )

        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * DPAD_LEFT_L, y = imgH * DPAD_LEFT_T)
                .width(imgW * (DPAD_LEFT_R - DPAD_LEFT_L))
                .height(imgH * (DPAD_LEFT_B - DPAD_LEFT_T))
                .clickable { onDpadLeft() }
        )

        Box(
            modifier = Modifier
                .absoluteOffset(x = imgW * DPAD_RIGHT_L, y = imgH * DPAD_RIGHT_T)
                .width(imgW * (DPAD_RIGHT_R - DPAD_RIGHT_L))
                .height(imgH * (DPAD_RIGHT_B - DPAD_RIGHT_T))
                .clickable { onDpadRight() }
        )

        // Left circular button — back/exit
        Box(
            modifier = Modifier
                .absoluteOffset(
                    x = imgW * (CIRC_LEFT_CX - CIRC_R),
                    y = imgH * (CIRC_CY - CIRC_R)
                )
                .width(imgW * CIRC_R * 2)
                .height(imgH * CIRC_R * 2)
                .clickable { onCircleLeft() }
        )

        // Right circular button — confirm/enter
        Box(
            modifier = Modifier
                .absoluteOffset(
                    x = imgW * (CIRC_RIGHT_CX - CIRC_R),
                    y = imgH * (CIRC_CY - CIRC_R)
                )
                .width(imgW * CIRC_R * 2)
                .height(imgH * CIRC_R * 2)
                .clickable { onCircleRight() }
        )

        // T9 grid
        repeat(10) { index ->
            val rowIndex = index / 5
            val colIndex = index % 5
            val buttonNumber = index + 1
            val rowTop = if (rowIndex == 0) T9_ROW1_TOP else T9_ROW2_TOP
            val rowBottom = if (rowIndex == 0) T9_ROW1_BOTTOM else T9_ROW2_BOTTOM
            val colLeft = T9_COL_STARTS[colIndex]
            val colRight = T9_COL_ENDS[colIndex]
            val isButton10 = buttonNumber == 10
            val labels = T9_LABELS[index]

            Box(
                modifier = Modifier
                    .absoluteOffset(x = imgW * colLeft, y = imgH * rowTop)
                    .width(imgW * (colRight - colLeft))
                    .height(imgH * (rowBottom - rowTop))
                    .then(
                        if (isButton10) {
                            Modifier.pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown().also { it.consume() }
                                    onT9Button10Press()
                                    waitForUpOrCancellation()
                                    onT9Button10Release()
                                }
                            }
                        } else {
                            Modifier.clickable { onT9Key(buttonNumber) }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = labels[0],
                        fontFamily = SixtyFourFont,
                        fontSize = 12.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    if (labels.size > 1) {
                        Text(
                            text = labels.drop(1).joinToString(" "),
                            fontFamily = SixtyFourFont,
                            fontSize = 8.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}