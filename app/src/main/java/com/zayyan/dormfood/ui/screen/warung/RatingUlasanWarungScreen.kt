package com.zayyan.dormfood.ui.screen.warung

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.model.RatingViewModel
import com.zayyan.dormfood.ui.model.ReviewItem
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import java.text.SimpleDateFormat
import java.util.Locale
import com.zayyan.dormfood.R

// ─── Colors ───────────────────────────────────────────────────────────────────
private val StarColor    = Color(0xFFF5A623)
private val CardBg       = Color(0xFFFFFFFF)
private val AvatarColors = listOf(
    Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFFF57C00),
    Color(0xFF7B1FA2), Color(0xFFD32F2F), Color(0xFF0097A7)
)

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingUlasanWarungScreen(
    navController: NavHostController,
    // warungId dari navigasi — bisa dari warung sendiri (via FirebaseAuth) atau
    // dari mahasiswa yang tap "Lihat Ulasan" di MenuMahasiswaScreen
    warungId: String,
    ratingViewModel: RatingViewModel = viewModel()
) {
    // Load reviews saat screen pertama dibuka atau warungId berubah
    LaunchedEffect(warungId) {
        if (warungId.isNotBlank()) {
            ratingViewModel.loadWarungReviews(warungId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Penilaian dan Ulasan",
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
        RatingUlasanWarungContent(
            paddingValues   = paddingValues,
            ratingViewModel = ratingViewModel
        )
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────
@Composable
fun RatingUlasanWarungContent(
    paddingValues: PaddingValues,
    ratingViewModel: RatingViewModel = viewModel()
) {
    val reviews       by ratingViewModel.warungReviews.collectAsState()
    val averageRating by ratingViewModel.averageRating.collectAsState()
    val isLoading     by ratingViewModel.isLoadingReviews.collectAsState()

    // Hitung distribusi bintang (5 → 1)
    val distribusi = remember(reviews) {
        (5 downTo 1).map { bintang ->
            bintang to reviews.count { it.rating == bintang }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DormRed)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Ringkasan Rating ─────────────────────────────────────────────────
        item {
            RatingSummaryCard(
                averageRating = averageRating,
                totalReviews  = reviews.size,
                distribusi    = distribusi
            )
        }

        // ── Header list ulasan ───────────────────────────────────────────────
        if (reviews.isNotEmpty()) {
            item {
                Text(
                    text = "Semua Ulasan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            items(reviews, key = { it.reviewId }) { review ->
                ReviewCard(review = review)
            }
        } else {
            // ── Empty state ──────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⭐", fontSize = 48.sp)
                        Text(
                            text = "Belum ada ulasan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Ulasan dari pelanggan akan muncul di sini",
                            fontSize = 13.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─── Kartu Ringkasan Rating ───────────────────────────────────────────────────
@Composable
private fun RatingSummaryCard(
    averageRating: Double,
    totalReviews: Int,
    distribusi: List<Pair<Int, Int>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Angka rata-rata ──────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(90.dp)
                ) {
                    Text(
                        text = if (averageRating == 0.0) "-"
                        else String.format(Locale.getDefault(), "%.1f", averageRating),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A0A00)
                    )
                    StarRow(
                        rating  = averageRating.toInt(),
                        size    = 16.dp,
                        spacing = 2.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalReviews ulasan",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))
                VerticalDivider(
                    modifier = Modifier.height(100.dp),
                    color = Color(0xFFEEEEEE),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.width(20.dp))

                // ── Bar distribusi bintang ───────────────────────────────────
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    distribusi.forEach { (bintang, jumlah) ->
                        val persen = if (totalReviews > 0)
                            jumlah / totalReviews.toFloat() else 0f

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$bintang",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.width(12.dp)
                            )
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = StarColor,
                                modifier = Modifier.size(12.dp)
                            )
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFF0F0F0))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(persen)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(StarColor)
                                )
                            }
                            Text(
                                text = "$jumlah",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.width(16.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Kartu satu review ───────────────────────────────────────────────────────
@Composable
private fun ReviewCard(review: ReviewItem) {
    // Warna avatar unik per user berdasarkan hash nama
    val avatarColor = AvatarColors[
        (review.namaUser.hashCode() and Int.MAX_VALUE) % AvatarColors.size
    ]
    val inisial = review.namaUser
        .trim()
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }

    val tanggal = review.createdAt?.toDate()?.let {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(it)
    } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header: avatar + nama + tanggal ─────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar inisial
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = inisial,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.namaUser.ifBlank { "Pengguna" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A0A00)
                    )
                    Text(
                        text = tanggal,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Bintang rating di kanan
                StarRow(rating = review.rating, size = 14.dp, spacing = 1.dp)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(10.dp))

            // ── Teks ulasan ──────────────────────────────────────────────────
            if (review.ulasan.isNotBlank()) {
                Text(
                    text = review.ulasan,
                    fontSize = 14.sp,
                    color = Color(0xFF424242),
                    lineHeight = 20.sp
                )
            } else {
                Text(
                    text = "Tidak ada komentar.",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

// ─── Reusable: baris bintang ──────────────────────────────────────────────────
@Composable
private fun StarRow(
    rating: Int,
    size: androidx.compose.ui.unit.Dp,
    spacing: androidx.compose.ui.unit.Dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        for (i in 1..5) {
            Icon(
                painter = painterResource(
                    id = if (i <= rating) R.drawable.star else R.drawable.starborder
                ),
                contentDescription = null,
                tint = if (i <= rating) StarColor else Color(0xFFDDDDDD),
                modifier = Modifier.size(size)
            )
        }
    }
}