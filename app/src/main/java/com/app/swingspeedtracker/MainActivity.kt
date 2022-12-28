package com.app.swingspeedtracker

import android.Manifest
import android.bluetooth.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import com.app.swingspeedtracker.ui.screens.MainScreen
import java.util.*

private const val TAG = "SwingSpeedTracker"

@RequiresApi(Build.VERSION_CODES.Q)
class MainActivity : ComponentActivity() {
    private lateinit var getPermission: ActivityResultLauncher<String>
    private lateinit var getBluetoothIntentResult: ActivityResultLauncher<Intent>

    private val isBluetoothPermissionGranted
        get() = hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission ->
            if (!hasPermission) {
                Toast.makeText(applicationContext, "No permission to perform this action", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        getBluetoothIntentResult  = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(applicationContext, "Bluetooth turned on", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(applicationContext, "Bluetooth turned off", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        checkBluetoothPermissions()
        initializeBluetooth()

        setContent {
            MainScreen()
        }
    }

    private fun checkBluetoothPermissions() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            getPermission.launch(Manifest.permission.BLUETOOTH_SCAN)
        }

        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            getPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    private fun initializeBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            getPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
            if (isBluetoothPermissionGranted) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                getBluetoothIntentResult.launch(enableBtIntent)
            }
        }
    }

    private fun hasPermission(permissionType: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED
    }
}