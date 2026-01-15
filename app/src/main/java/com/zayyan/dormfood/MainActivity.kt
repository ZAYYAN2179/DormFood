package com.zayyan.dormfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zayyan.dormfood.ui.navigation.SetupNavGraph
import com.zayyan.dormfood.ui.theme.DormFoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DormFoodTheme {
                SetupNavGraph()
            }
        }
    }
}