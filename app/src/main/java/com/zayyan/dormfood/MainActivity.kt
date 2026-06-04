package com.zayyan.dormfood

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.zayyan.dormfood.ui.navigation.SetupNavGraph
import com.zayyan.dormfood.ui.theme.DormFoodTheme
import com.zayyan.dormfood.ui.utils.CloudinaryConfig
import com.zayyan.dormfood.ui.utils.NotificationHelper
import com.zayyan.dormfood.ui.viewmodel.PesananViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val pesananViewModel: PesananViewModel by viewModels()

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* lanjut meski ditolak */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CloudinaryConfig.init(this)

        NotificationHelper.createChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        lifecycleScope.launch {
            pesananViewModel.statusChangeEvent.collect { (namaWarung, statusBaru) ->
                NotificationHelper.showOrderStatusNotification(
                    context = this@MainActivity,
                    namaWarung = namaWarung,
                    statusBaru = statusBaru
                )
            }
        }

        lifecycleScope.launch {
            pesananViewModel.newOrderEvent.collect { order ->
                NotificationHelper.showNewOrderNotification(
                    context = this@MainActivity,
                    namaUser = order.namaUser,
                    totalHarga = order.totalHarga
                )
            }
        }

        lifecycleScope.launch {
            pesananViewModel.orderRejectedEvent.collect { namaWarung ->
                NotificationHelper.showOrderRejectedNotification(
                    context = this@MainActivity,
                    namaWarung = namaWarung
                )
            }
        }

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        enableEdgeToEdge()
        setContent {
            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                SetupNavGraph()
            }
        }
    }
}