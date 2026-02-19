package com.example.pokedex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuScreen(
    selectedItem: MenuItem,
    onItemSelected: (MenuItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = "MENU",
            fontFamily = SixtyFourFont,
            fontSize = 10.sp,
            color = TerminalDimGreen,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        HorizontalDivider(color = TerminalDimGreen, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))

        MenuItem.entries.forEach { item ->
            val isSelected = item == selectedItem
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSelected) ">" else " ",
                    fontFamily = SixtyFourFont,
                    fontSize = 11.sp,
                    color = TerminalGreen,
                    modifier = Modifier.width(16.dp)
                )
                Text(
                    text = item.name,
                    fontFamily = SixtyFourFont,
                    fontSize = 11.sp,
                    color = if (isSelected) TerminalGreen else TerminalDimGreen
                )
            }
            HorizontalDivider(color = TerminalDivider, thickness = 0.5.dp)
        }
    }
}

@Composable
fun MenuPreviewScreen(item: MenuItem) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TerminalDividerLine(item.name)
            Spacer(modifier = Modifier.height(8.dp))

            when (item) {
                MenuItem.POKEDEX -> {
                    TerminalDetailRow("POKEMON", "1025")
                    TerminalDetailRow("REGIONS", "9")
                    TerminalDetailRow("GENERATIONS", "9")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "SEARCH BY NAME,\nTYPE, GEN,\nREGION, OR\nSTATUS",
                        fontFamily = SixtyFourFont,
                        fontSize = 8.sp,
                        color = TerminalDimGreen,
                        lineHeight = 14.sp
                    )
                }
                MenuItem.POKEWALKER -> {
                    TerminalDetailRow("DEVICE", "BANGLE.JS 2")
                    TerminalDetailRow("BLUETOOTH", "OFF")
                    TerminalDetailRow("STATUS", "NOT CONNECTED")
                    TerminalDetailRow("STEPS", "---")
                    TerminalDetailRow("POKEMON", "---")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "SYNC YOUR\nPOKEWALKER\nVIA BLUETOOTH",
                        fontFamily = SixtyFourFont,
                        fontSize = 8.sp,
                        color = TerminalDimGreen,
                        lineHeight = 14.sp
                    )
                }
                MenuItem.EMULATOR -> {
                    TerminalDetailRow("EMULATOR", "MELONDS")
                    TerminalDetailRow("STATUS", "NOT CONNECTED")
                    TerminalDetailRow("ROM", "---")
                    TerminalDetailRow("GAME", "---")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CONNECT TO\nMELONDS RUNNING\nHEARTGOLD OR\nSOULSILVER",
                        fontFamily = SixtyFourFont,
                        fontSize = 8.sp,
                        color = TerminalDimGreen,
                        lineHeight = 14.sp
                    )
                }
                MenuItem.SETTINGS -> {
                    TerminalDetailRow("THEME", "TERMINAL")
                    TerminalDetailRow("DEX SKIN", "GEN 1")
                    TerminalDetailRow("LANGUAGE", "EN")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CONFIGURE APP\nPREFERENCES AND\nCONNECTION\nSETTINGS",
                        fontFamily = SixtyFourFont,
                        fontSize = 8.sp,
                        color = TerminalDimGreen,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PokewalkerScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TerminalDividerLine("POKEWALKER")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "STATUS",
                fontFamily = SixtyFourFont,
                fontSize = 9.sp,
                color = TerminalDimGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NOT CONNECTED",
                fontFamily = SixtyFourFont,
                fontSize = 10.sp,
                color = Color(0xFFE74C3C)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "COMING SOON",
                fontFamily = SixtyFourFont,
                fontSize = 8.sp,
                color = TerminalDimGreen,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmulatorScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TerminalDividerLine("EMULATOR")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MELONDS STATUS",
                fontFamily = SixtyFourFont,
                fontSize = 9.sp,
                color = TerminalDimGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NOT CONNECTED",
                fontFamily = SixtyFourFont,
                fontSize = 10.sp,
                color = Color(0xFFE74C3C)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "COMING SOON",
                fontFamily = SixtyFourFont,
                fontSize = 8.sp,
                color = TerminalDimGreen,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TerminalDividerLine("SETTINGS")
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "COMING SOON",
                fontFamily = SixtyFourFont,
                fontSize = 8.sp,
                color = TerminalDimGreen,
                textAlign = TextAlign.Center
            )
        }
    }
}