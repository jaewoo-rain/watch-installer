package com.snown.watchinstaller

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.snown.watchinstaller.ui.InstallerScreen
import com.snown.watchinstaller.ui.InstallerTheme

class MainActivity : ComponentActivity() {

    private val vm: InstallerViewModel by viewModels()

    private val pickApk = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.onPicked(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InstallerTheme {
                InstallerScreen(
                    vm = vm,
                    onPickFile = {
                        // 일부 파일 앱은 APK 를 octet-stream 으로 알려 주므로 둘 다 받는다.
                        pickApk.launch(arrayOf("application/vnd.android.package-archive", "application/octet-stream"))
                    },
                    onAllowDownloads = {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                Uri.parse("package:$packageName"),
                            ),
                        )
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.startFinding()
    }

    override fun onResume() {
        super.onResume()
        vm.refreshApk()
    }

    override fun onStop() {
        vm.stopFinding()
        super.onStop()
    }
}
