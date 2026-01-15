package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zayyan.dormfood.ui.component.TopAppBar
import com.zayyan.dormfood.ui.theme.Background

@Composable
fun BerandaMahasiswa() {
    Scaffold(
        topBar = {
            TopAppBar(title = "Dorm Food")
        },
        containerColor = Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
        }
    }
}
