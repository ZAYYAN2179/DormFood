package com.zayyan.dormfood.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.zayyan.dormfood.R
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.theme.DormRed

@Composable
fun BottomBarWarung(
    navController: NavHostController,
    currentRoute: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {

        BottomBarItem(
            icon = Icons.Default.Home,
            label = "Beranda",
            selected = currentRoute == Screen.BerandaWarungScreen.route,
            onClick = {
                navController.navigate(Screen.BerandaWarungScreen.route)
            },
            modifier = Modifier.weight(1f)
        )

        BottomBarItem(
            icon = ImageVector.vectorResource(id = R.drawable.menu),
            label = "Menu",
            selected = currentRoute == Screen.MenuWarungScreen.route,
            onClick = {
                navController.navigate(Screen.MenuWarungScreen.route)
            },
            modifier = Modifier.weight(1f)
        )

        BottomBarItem(
            icon = ImageVector.vectorResource(id = R.drawable.pesanan),
            label = "Pesanan",
            selected = currentRoute == Screen.PesananWarungScreen.route,
            onClick = {
                navController.navigate(Screen.PesananWarungScreen.route)
            },
            modifier = Modifier.weight(1f)
        )

        BottomBarItem(
            icon = Icons.Default.Person,
            label = "Profil",
            selected = currentRoute == Screen.ProfilWarungScreen.route,
            onClick = {
                navController.navigate(Screen.ProfilWarungScreen.route)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) DormRed else Color.Gray

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}