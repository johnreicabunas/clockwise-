package com.johnreicabunas.clockwise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.johnreicabunas.clockwise.data.repository.GooglePlayBillingRepository
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val billingRepository: GooglePlayBillingRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        setContent {
            App()
        }
    }

    override fun onStart() {
        super.onStart()
        billingRepository.attachActivity(this)
    }

    override fun onStop() {
        billingRepository.detachActivity(this)
        super.onStop()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
