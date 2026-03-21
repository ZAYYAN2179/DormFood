package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.res.painterResource
import com.zayyan.dormfood.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed

private val StarSelected   = Color(0xFFF5A623)
private val StarUnselected = Color(0xFFBDBDBD)
private val CardBg         = Color(0xFFFFFFFF)
private val LabelColor     = Color(0xFF1A1A1A)
private val PlaceholderColor = Color(0xFFBDBDBD)
private val ButtonColor    = Color(0xFF6B0F0F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingUlasanMahasiswaScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Form Penilaian dan Ulasan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DormRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        RatingUlasanMahasiswaContent(paddingValues = paddingValues)
    }
}

@Composable
fun RatingUlasanMahasiswaContent(paddingValues: PaddingValues) {

    var rating    by remember { mutableIntStateOf(0) }
    var ulasan    by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Berikan rating bintang",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = LabelColor
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    val filled = i <= rating
                    Icon(
                        painter = painterResource(
                            id = if (filled) R.drawable.star else R.drawable.starborder
                        ),
                        contentDescription = "Bintang $i",
                        tint = if (filled) StarSelected else StarUnselected,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { rating = i }
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Tulis Ulasan",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = LabelColor
            )

            OutlinedTextField(
                value = ulasan,
                onValueChange = { ulasan = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = {
                    Text(
                        text = "Mantapppp dan The Bestttt",
                        color = PlaceholderColor,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = CardBg,
                    unfocusedContainerColor = CardBg,
                    disabledContainerColor  = CardBg,
                    focusedBorderColor      = ButtonColor,
                    unfocusedBorderColor    = Color.Transparent,
                ),
                maxLines = 6
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { if (rating > 0) submitted = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = DormRed
            )
        ) {
            Text(
                text = "Kirim",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        if (submitted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEDF7ED), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✅ Ulasan berhasil dikirim! Terima kasih.",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}