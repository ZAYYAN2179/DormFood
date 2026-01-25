package com.zayyan.dormfood.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.zayyan.dormfood.ui.theme.DormRed
import com.zayyan.dormfood.R
import com.zayyan.dormfood.ui.navigation.Screen

@Composable
fun LandingPage(
    navController: NavHostController = rememberNavController(),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DormRed)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_dorm_food),
                    contentDescription = "Dorm Food Logo",
                    modifier = Modifier.size(150.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Dorm Food",
                color = White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (index == 1) 32.dp else 16.dp)
                            .background(
                                color = White,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {navController.navigate(Screen.UserRoleScreen.route)},
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = White)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = DormRed
                )
            }
        }
    }
}
