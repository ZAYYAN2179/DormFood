package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.R
import com.zayyan.dormfood.ui.viewmodel.RatingViewModel
import com.zayyan.dormfood.ui.viewmodel.SubmitReviewState
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed

private val StarSelected = Color(0xFFF5A623)
private val StarUnselected = Color(0xFFBDBDBD)
private val CardBg = Color(0xFFFFFFFF)
private val LabelColor = Color(0xFF1A1A1A)
private val PlaceholderColor = Color(0xFFBDBDBD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingUlasanMahasiswaScreen(
    navController: NavHostController,
    orderId: String,
    warungId: String,
    namaWarung: String,
    ratingViewModel: RatingViewModel = viewModel()
) {
    LaunchedEffect(Unit) { ratingViewModel.resetState() }

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
        RatingUlasanMahasiswaContent(
            paddingValues = paddingValues,
            orderId = orderId,
            warungId = warungId,
            namaWarung = namaWarung,
            ratingViewModel = ratingViewModel,
            onBackAfterSuccess = { navController.popBackStack() }
        )
    }
}

@Composable
fun RatingUlasanMahasiswaContent(
    paddingValues: PaddingValues,
    orderId: String,
    warungId: String,
    namaWarung: String,
    ratingViewModel: RatingViewModel,
    onBackAfterSuccess: () -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var ulasan by remember { mutableStateOf("") }

    val submitState by ratingViewModel.submitState.collectAsState()

    LaunchedEffect(submitState) {
        if (submitState is SubmitReviewState.Success) {
            kotlinx.coroutines.delay(1500)
            onBackAfterSuccess()
        }
    }

    val isInteractable = submitState !is SubmitReviewState.Loading &&
            submitState !is SubmitReviewState.Success

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = "Ulasan untuk: $namaWarung",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

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
                            .clickable(enabled = isInteractable) { rating = i }
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
                        text = "Ceritakan pengalamanmu...",
                        color = PlaceholderColor,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    disabledContainerColor = CardBg,
                    focusedBorderColor = DormRed,
                    unfocusedBorderColor = Color.Transparent,
                ),
                maxLines = 6,
                enabled = isInteractable
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                ratingViewModel.kirimUlasan(
                    orderId = orderId,
                    warungId = warungId,
                    namaWarung = namaWarung,
                    rating = rating,
                    ulasan = ulasan
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50),
            enabled = isInteractable,
            colors = ButtonDefaults.buttonColors(containerColor = DormRed)
        ) {
            if (submitState is SubmitReviewState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Kirim",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        when (submitState) {
            is SubmitReviewState.Success -> FeedbackBox(
                emoji = "✅",
                message = "Ulasan berhasil dikirim! Terima kasih.",
                bgColor = Color(0xFFEDF7ED),
                textColor = Color(0xFF2E7D32)
            )

            is SubmitReviewState.AlreadyReviewed -> FeedbackBox(
                emoji = "ℹ️",
                message = "Kamu sudah memberikan ulasan untuk pesanan ini.",
                bgColor = Color(0xFFE3F2FD),
                textColor = Color(0xFF1565C0)
            )

            is SubmitReviewState.Error -> FeedbackBox(
                emoji = "❌",
                message = (submitState as SubmitReviewState.Error).message,
                bgColor = Color(0xFFFFEBEE),
                textColor = Color(0xFFC62828)
            )

            else -> Unit
        }
    }
}

@Composable
private fun FeedbackBox(
    emoji: String,
    message: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$emoji $message",
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}